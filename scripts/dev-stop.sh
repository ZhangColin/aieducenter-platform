#!/bin/bash
# 开发部署环境停止

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
ENV=${1:-dev}
COMPOSE_FILE="$PROJECT_ROOT/docker-compose.$ENV.yml"

cd "$PROJECT_ROOT"

echo "=========================================="
echo "   停止开发部署环境 (Docker)"
echo "=========================================="
echo ""

if [ ! -f "$COMPOSE_FILE" ]; then
    echo "  ✗ 错误: Compose 文件不存在: $COMPOSE_FILE"
    exit 1
fi

echo "停止并移除所有服务..."
docker-compose -f "$COMPOSE_FILE" down

echo ""
echo "=========================================="
echo "   ✅ 开发部署已停止"
echo "=========================================="
echo ""
