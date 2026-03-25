#!/bin/bash
# 部署共用函数
# 供 dev-deploy.sh, test-deploy.sh, prod-deploy.sh 使用

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."

# 颜色输出
color_info() { echo "\033[0;32m✓ $*\033[0m"; }
color_warn() { echo "\033[0;33m⚠ $*\033[0m"; }
color_error() { echo "\033[0;31m✗ $*\033[0m"; }

# 打印分隔线
print_header() {
    echo ""
    echo "=========================================="
    echo "   $1"
    echo "=========================================="
}

# 获取环境配置
get_env_config() {
    local env=$1
    case $env in
        dev)
            echo ".env.dev"
            ;;
        test)
            echo ".env.test"
            ;;
        prod)
            echo ".env.prod"
            ;;
        *)
            echo "Unknown environment: $env" >&2
            exit 1
            ;;
    esac
}

# 1. 运行全部测试
run_tests() {
    echo ""
    print_header "[1/5] 运行全部测试"
    "$SCRIPT_DIR/../test/run.sh" --all
}

# 2. 打包构建
run_build() {
    echo ""
    print_header "[2/5] 打包构建"

    echo "  → 构建后端..."
    "$SCRIPT_DIR/../build/backend.sh"

    echo "  → 构建前端..."
    "$SCRIPT_DIR/../build/frontend.sh"

    color_info "构建完成"
}

# 3. 准备部署包
prepare_deploy_package() {
    local env=$1
    local deploy_dir="$PROJECT_ROOT/.deploy-$env"

    echo ""
    print_header "[3/5] 准备部署包"

    rm -rf "$deploy_dir"
    mkdir -p "$deploy_dir"
    mkdir -p "$deploy_dir/backend"
    mkdir -p "$deploy_dir/web"
    mkdir -p "$deploy_dir/admin"

    # 复制 Docker Compose 配置
    cp "$PROJECT_ROOT/docker-compose.$env.yml" "$deploy_dir/docker-compose.yml"

    # 复制后端构建产物
    cp -r "$PROJECT_ROOT/server/build/libs" "$deploy_dir/backend/"

    # 复制前端构建产物
    cp -r "$PROJECT_ROOT/web/.next" "$deploy_dir/web/"
    cp -r "$PROJECT_ROOT/admin/.next" "$deploy_dir/admin/"

    # 复制 Docker 配置
    cp "$PROJECT_ROOT/docker-bake.hcl" "$deploy_dir/"
    cp "$PROJECT_ROOT/docker/backend/Dockerfile" "$deploy_dir/backend/"
    cp "$PROJECT_ROOT/docker/frontend/Dockerfile.web" "$deploy_dir/web/"
    cp "$PROJECT_ROOT/docker/frontend/Dockerfile.admin" "$deploy_dir/admin/"

    # 生成服务器端脚本
    cat > "$deploy_dir/build.sh" << 'EOF'
#!/bin/bash
set -e
echo "构建 Docker 镜像..."
docker buildx bake -f docker-bake.hcl --load backend web admin
echo "✓ 镜像构建完成"
EOF

    cat > "$deploy_dir/start.sh" << 'EOF'
#!/bin/bash
set -e
echo "启动服务..."
docker-compose -f docker-compose.yml down 2>/dev/null || true
docker-compose -f docker-compose.yml up -d
echo "✓ 服务已启动"
docker-compose -f docker-compose.yml ps
EOF

    cat > "$deploy_dir/stop.sh" << 'EOF'
#!/bin/bash
set -e
read -p "确认停止服务？(yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "已取消"
    exit 0
fi
echo "停止服务..."
docker-compose -f docker-compose.yml down
echo "✓ 服务已停止"
EOF

    chmod +x "$deploy_dir"/*.sh

    color_info "部署包已准备: $deploy_dir"
    echo "$deploy_dir"
}

# 4. 上传到服务器
upload_to_server() {
    local env=$1
    local deploy_dir="$PROJECT_ROOT/.deploy-$env"
    local env_file="$PROJECT_ROOT/$(get_env_config $env)"

    # 加载环境变量
    if [ -f "$env_file" ]; then
        export $(cat "$env_file" | grep -v '^#' | grep -v '^$' | xargs)
    else
        color_error "环境文件不存在: $env_file"
        exit 1
    fi

    local server_user=${SERVER_USER:-root}
    local server_host=${SERVER_HOST:-localhost}
    local server_port=${SERVER_PORT:-22}
    local server_deploy_dir=${SERVER_DEPLOY_DIR:-/opt/aiedu}

    echo ""
    print_header "[4/5] 上传到服务器"
    echo "服务器: $server_user@$server_host:$server_port"
    echo "部署目录: $server_deploy_dir"

    if [ -n "$SERVER_PASSWORD" ]; then
        sshpass -p "$SERVER_PASSWORD" scp -P "$server_port" -r "$deploy_dir"/* \
            "$server_user@$server_host:$server_deploy_dir/"
    else
        scp -P "$server_port" -r "$deploy_dir"/* \
            "$server_user@$server_host:$server_deploy_dir/"
    fi

    color_info "文件已上传到服务器"
}

# 5. 远程构建镜像
remote_build() {
    local env=$1
    local server_user=${SERVER_USER:-root}
    local server_host=${SERVER_HOST:-localhost}
    local server_port=${SERVER_PORT:-22}
    local server_deploy_dir=${SERVER_DEPLOY_DIR:-/opt/aiedu}

    echo ""
    print_header "[5/5] 远程构建 Docker 镜像"

    if [ -n "$SERVER_PASSWORD" ]; then
        sshpass -p "$SERVER_PASSWORD" ssh -p "$server_port" \
            "$server_user@$server_host" "cd $server_deploy_dir && ./build.sh"
    else
        ssh -p "$server_port" \
            "$server_user@$server_host" "cd $server_deploy_dir && ./build.sh"
    fi

    color_info "Docker 镜像构建完成"
}

# 6. 远程启动服务
remote_start() {
    local server_user=${SERVER_USER:-root}
    local server_host=${SERVER_HOST:-localhost}
    local server_port=${SERVER_PORT:-22}
    local server_deploy_dir=${SERVER_DEPLOY_DIR:-/opt/aiedu}

    echo ""
    read -p "是否在服务器上启动服务？(y/n): " confirm
    if [ "$confirm" != "y" ]; then
        return
    fi

    if [ -n "$SERVER_PASSWORD" ]; then
        sshpass -p "$SERVER_PASSWORD" ssh -p "$server_port" \
            "$server_user@$server_host" "cd $server_deploy_dir && ./start.sh"
    else
        ssh -p "$server_port" \
            "$server_user@$server_host" "cd $server_deploy_dir && ./start.sh"
    fi

    color_info "服务已启动"
}
