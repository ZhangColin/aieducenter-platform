#!/bin/bash
# 测试环境打包发布（本地 → 服务器）
# 用法: ./scripts/test-deploy.sh [server_user] [server_host]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
DEPLOY_DIR="$PROJECT_ROOT/.deploy-test"

# 服务器配置（支持参数或默认值）
SERVER_USER=${1:-${SERVER_USER:-root}}
SERVER_HOST=${2:-${SERVER_HOST:-localhost}}
SERVER_PORT=${SERVER_PORT:-22}
SERVER_DEPLOY_DIR=${SERVER_DEPLOY_DIR:-/opt/aiedu}
SERVER_PASSWORD=${SERVER_PASSWORD:-}

# 加载共性函数
source "$SCRIPT_DIR/lib/common.sh"

print_header "测试环境打包发布"

echo "服务器: $SERVER_USER@$SERVER_HOST:$SERVER_PORT"
echo "部署目录: $SERVER_DEPLOY_DIR"
echo ""

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
mkdir -p "$DEPLOY_DIR/scripts"

# 复制 Docker Compose 配置
cp "$PROJECT_ROOT/docker-compose.test.yml" "$DEPLOY_DIR/docker-compose.yml"

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
echo "构建 Docker 镜像..."
docker buildx bake -f docker-bake.hcl --load backend web admin
echo "✓ 镜像构建完成"
EOF

cat > "$DEPLOY_DIR/start.sh" << 'EOF'
#!/bin/bash
set -e
echo "启动服务..."
docker-compose -f docker-compose.yml down 2>/dev/null || true
docker-compose -f docker-compose.yml up -d
echo "✓ 服务已启动"
docker-compose -f docker-compose.yml ps
EOF

cat > "$DEPLOY_DIR/stop.sh" << 'EOF'
#!/bin/bash
set -e
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
    # 使用 sshpass
    sshpass -p "$SERVER_PASSWORD" scp -P "$SERVER_PORT" -r "$DEPLOY_DIR"/* \
        "$SERVER_USER@$SERVER_HOST:$SERVER_DEPLOY_DIR/"
else
    # 使用密钥认证
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
print_header "测试环境发布完成"
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
