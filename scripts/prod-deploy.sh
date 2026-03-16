#!/bin/bash
# 生产环境打包发布（本地 → 服务器）
# 用法: ./scripts/prod-deploy.sh [server_user] [server_host]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
ENV_FILE="$PROJECT_ROOT/.env.prod"
DEPLOY_DIR="$PROJECT_ROOT/.deploy-prod"

# 服务器配置（支持参数或默认值）
SERVER_USER=${1:-${SERVER_USER:-root}}
SERVER_HOST=${2:-${SERVER_HOST:-localhost}}
SERVER_PORT=${SERVER_PORT:-22}
SERVER_DEPLOY_DIR=${SERVER_DEPLOY_DIR:-/opt/aiedu}
SERVER_PASSWORD=${SERVER_PASSWORD:-}

# 加载共性函数
source "$SCRIPT_DIR/lib/common.sh"

print_header "生产环境打包发布 🚨"

# 检查 .env.prod
check_file_exists "$ENV_FILE" ".env.prod"

# 加载环境变量
export $(cat "$ENV_FILE" | grep -v '^#' | grep -v '^$' | xargs)

echo "服务器: $SERVER_USER@$SERVER_HOST:$SERVER_PORT"
echo "部署目录: $SERVER_DEPLOY_DIR"
echo "数据库: $SPRING_DATASOURCE_URL"
echo ""

# 最后确认
read -p "确认发布到生产环境？(yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "已取消"
    exit 0
fi

# 1. 验证
run_validate "$SCRIPT_DIR"

# 2. 构建
run_build_backend "$SCRIPT_DIR"
run_build_frontend "$SCRIPT_DIR"

# 3. 准备部署包
echo ""
echo "→ 准备部署包..."
rm -rf "$DEPLOY_DIR"
mkdir -p "$DEPLOY_DIR"
mkdir -p "$DEPLOY_DIR/config"

# 复制环境配置（敏感信息）
cp "$ENV_FILE" "$DEPLOY_DIR/.env"

# 复制 Docker Compose 配置
cp "$PROJECT_ROOT/docker-compose.prod.yml" "$DEPLOY_DIR/docker-compose.yml"

# 复制后端构建产物
cp -r "$PROJECT_ROOT/server/build/libs" "$DEPLOY_DIR/backend"

# 复制前端构建产物
cp -r "$PROJECT_ROOT/web/.next" "$DEPLOY_DIR/web"
cp -r "$PROJECT_ROOT/admin/.next" "$DEPLOY_DIR/admin"

# 复制 Docker 配置
cp "$PROJECT_ROOT/docker-bake.hcl" "$DEPLOY_DIR/"
cp "$PROJECT_ROOT/docker/backend/Dockerfile" "$DEPLOY_DIR/backend/"
cp "$PROJECT_ROOT/docker/frontend/Dockerfile.web" "$DEPLOY_DIR/web/"
cp "$PROJECT_ROOT/docker/frontend/Dockerfile.admin" "$DEPLOY_DIR/admin/"

# 生成服务器端脚本
cat > "$DEPLOY_DIR/build.sh" << 'EOF'
#!/bin/bash
set -e
# 加载环境变量
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
fi

echo "构建 Docker 镜像..."
docker buildx bake -f docker-bake.hcl --load backend web admin
echo "✓ 镜像构建完成"
EOF

cat > "$DEPLOY_DIR/start.sh" << 'EOF'
#!/bin/bash
set -e
# 加载环境变量
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
fi

echo "启动服务..."
docker-compose -f docker-compose.yml down 2>/dev/null || true
docker-compose -f docker-compose.yml up -d
echo "✓ 服务已启动"
docker-compose -f docker-compose.yml ps
EOF

cat > "$DEPLOY_DIR/stop.sh" << 'EOF'
#!/bin/bash
set -e
# 加载环境变量
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
fi

read -p "确认停止生产服务？(yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "已取消"
    exit 0
fi

echo "停止服务..."
docker-compose -f docker-compose.yml down
echo "✓ 服务已停止"
EOF

chmod +x "$DEPLOY_DIR"/*.sh

color_info "部署包已准备: $DEPLOY_DIR"

# 4. 拷贝到服务器
echo ""
echo "→ 拷贝文件到服务器..."

if [ -n "$SERVER_PASSWORD" ]; then
    sshpass -p "$SERVER_PASSWORD" scp -P "$SERVER_PORT" -r "$DEPLOY_DIR"/* \
        "$SERVER_USER@$SERVER_HOST:$SERVER_DEPLOY_DIR/"
else
    scp -P "$SERVER_PORT" -r "$DEPLOY_DIR"/* \
        "$SERVER_USER@$SERVER_HOST:$SERVER_DEPLOY_DIR/"
fi

color_info "文件已拷贝到服务器"

# 5. 可选：远程执行构建和启动
echo ""
read -p "是否在服务器上执行构建和启动？(y/n): " exec_remote
if [ "$exec_remote" = "y" ]; then
    echo ""
    echo "→ 远程执行构建..."

    if [ -n "$SERVER_PASSWORD" ]; then
        sshpass -p "$SERVER_PASSWORD" ssh -p "$SERVER_PORT" \
            "$SERVER_USER@$SERVER_HOST" "cd $SERVER_DEPLOY_DIR && ./build.sh && ./start.sh"
    else
        ssh -p "$SERVER_PORT" \
            "$SERVER_USER@$SERVER_HOST" "cd $SERVER_DEPLOY_DIR && ./build.sh && ./start.sh"
    fi
fi

echo ""
print_header "生产环境发布完成"
echo ""
echo "服务器: $SERVER_USER@$SERVER_HOST"
echo "部署目录: $SERVER_DEPLOY_DIR"
echo ""
echo "如需手动操作："
echo "  ssh $SERVER_USER@$SERVER_HOST"
echo "  cd $SERVER_DEPLOY_DIR"
echo "  ./build.sh  # 构建镜像"
echo "  ./start.sh  # 启动服务"
echo ""
