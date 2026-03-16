#!/bin/bash
# 开发环境部署启动

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."

ENV=${1:-dev}
COMPOSE_FILE="$PROJECT_ROOT/docker-compose.$ENV.yml"

cd "$PROJECT_ROOT"

echo "=========================================="
echo "   启动开发部署环境 (Docker)"
echo "=========================================="

# 检查 compose 文件是否存在
if [ ! -f "$COMPOSE_FILE" ]; then
    echo "  ✗ 错误: Compose 文件不存在: $COMPOSE_FILE"
    exit 1
fi

# 如果本地开发环境在运行，自动停止
if docker ps --format '{{.Names}}' | grep -q "^aiedu-redis-local$"; then
    echo ""
    echo "→ 检测到本地开发环境正在运行，自动停止..."
    "$SCRIPT_DIR/local-stop.sh"
fi

# 设置代理
PROXY_HOST="127.0.0.1:7897"
echo ""
echo "设置代理 $PROXY_HOST ..."
export HTTP_PROXY=http://$PROXY_HOST
export HTTPS_PROXY=http://$PROXY_HOST

# 1. 验证
echo ""
echo "[1/5] 验证代码质量..."
"$SCRIPT_DIR/validate/all.sh"

# 2. 构建后端
echo ""
echo "[2/5] 构建后端..."
"$SCRIPT_DIR/build/backend.sh"

# 3. 构建前端
echo ""
echo "[3/5] 构建前端..."
"$SCRIPT_DIR/build/frontend.sh"

# 4. 构建 Docker 镜像
echo ""
echo "[4/5] 构建 Docker 镜像..."
docker buildx bake -f docker-bake.hcl --load backend web admin

# 5. 启动服务
echo ""
echo "[5/5] 启动服务..."
docker-compose -f "$COMPOSE_FILE" down 2>/dev/null || true
docker-compose -f "$COMPOSE_FILE" up -d

echo ""
echo "=========================================="
echo "   ✅ 开发部署已启动"
echo "=========================================="
echo "   后端:  http://localhost:8080"
echo "   Web:   http://localhost:3000"
echo "   Admin: http://localhost:3001"
echo "=========================================="
echo ""
echo "查看日志: docker-compose -f $COMPOSE_FILE logs -f"
echo "停止服务: ./scripts/dev-stop.sh"
echo ""
