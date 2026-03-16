#!/bin/bash
# 本地开发环境启动

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
LOG_DIR="$PROJECT_ROOT/logs"

# 创建日志目录
mkdir -p "$LOG_DIR"

echo "=========================================="
echo "   启动本地开发环境"
echo "=========================================="

# 如果开发部署在运行，自动停止
if docker ps --format '{{.Names}}' | grep -q "aiedu-backend-dev\|aiedu-web-dev\|aiedu-admin-dev"; then
    echo ""
    echo "→ 检测到开发部署正在运行，自动停止..."
    "$SCRIPT_DIR/dev-stop.sh"
fi

# 启动 Redis 容器（本地开发用）
echo ""
echo "→ 启动 Redis..."
if docker ps --format '{{.Names}}' | grep -q "^aiedu-redis-local$"; then
    echo "  ✓ Redis 已在运行"
elif docker ps -a --format '{{.Names}}' | grep -q "^aiedu-redis-local$"; then
    docker start aiedu-redis-local
    echo "  ✓ Redis 已启动"
else
    docker run -d --name aiedu-redis-local -p 6379:6379 redis:7-alpine
    echo "  ✓ Redis 已启动"
fi

# 清理函数
cleanup() {
    echo ""
    echo "→ 停止应用服务..."

    # 杀死后端进程
    if [ -f "$LOG_DIR/backend.pid" ]; then
        BACKEND_PID=$(cat "$LOG_DIR/backend.pid")
        if kill -0 "$BACKEND_PID" 2>/dev/null; then
            kill "$BACKEND_PID" 2>/dev/null || true
            wait "$BACKEND_PID" 2>/dev/null || true
        fi
        rm -f "$LOG_DIR/backend.pid"
    fi

    echo "  ✓ 应用服务已停止"
    echo "  Redis 继续运行（完全清理: ./scripts/local-stop.sh）"
}

# 捕获退出信号
trap cleanup EXIT INT TERM

# 启动后端（后台运行）
echo ""
echo "→ 启动后端服务..."
cd "$PROJECT_ROOT/server"
./gradlew bootRun > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$LOG_DIR/backend.pid"

# 等待后端启动
echo "  → 等待后端就绪..."
for i in {1..60}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "  ✓ 后端就绪"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "  ✗ 后端启动超时，请检查日志: $LOG_DIR/backend.log"
        exit 1
    fi
    sleep 1
done

# 启动前端（前台运行）
echo ""
echo "=========================================="
echo "   本地开发环境已就绪"
echo "=========================================="
echo "   前端:   http://localhost:3000"
echo "   后端:   http://localhost:8080"
echo "   Redis:  localhost:6379 (Docker)"
echo ""
echo "   按 Ctrl+C 停止应用服务"
echo "   完全清理: ./scripts/local-stop.sh"
echo "=========================================="
echo ""

cd "$PROJECT_ROOT"
exec pnpm dev
