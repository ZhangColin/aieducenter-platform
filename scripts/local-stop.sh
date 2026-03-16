#!/bin/bash
# 本地开发环境停止

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
LOG_DIR="$PROJECT_ROOT/logs"

echo "=========================================="
echo "   停止本地开发环境"
echo "=========================================="
echo ""

# 停止后端进程
if [ -f "$LOG_DIR/backend.pid" ]; then
    BACKEND_PID=$(cat "$LOG_DIR/backend.pid")
    if kill -0 "$BACKEND_PID" 2>/dev/null; then
        echo "→ 停止后端 (PID: $BACKEND_PID)..."
        kill "$BACKEND_PID" 2>/dev/null || true
        wait "$BACKEND_PID" 2>/dev/null || true
        echo "  ✓ 后端已停止"
    fi
    rm -f "$LOG_DIR/backend.pid"
fi

# 停止 Redis 容器
if docker ps -a --format '{{.Names}}' | grep -q "^aiedu-redis-local$"; then
    echo ""
    echo "→ 停止 Redis..."
    docker stop aiedu-redis-local
    docker rm aiedu-redis-local
    echo "  ✓ Redis 已停止并移除"
fi

echo ""
echo "=========================================="
echo "   ✅ 本地开发环境已停止"
echo "=========================================="
echo ""
