#!/bin/bash
# 联调启动脚本 - 同时启动后端和前端开发服务器

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
LOG_DIR="$PROJECT_ROOT/logs"

# 创建日志目录
mkdir -p "$LOG_DIR"

echo "======================================"
echo "启动前后端联调开发环境"
echo "======================================"

# 清理函数
cleanup() {
    echo ""
    echo "停止服务..."

    # 杀死后端进程
    if [ -f "$LOG_DIR/backend.pid" ]; then
        BACKEND_PID=$(cat "$LOG_DIR/backend.pid")
        if kill -0 "$BACKEND_PID" 2>/dev/null; then
            echo "停止后端 (PID: $BACKEND_PID)"
            kill "$BACKEND_PID" 2>/dev/null || true
            wait "$BACKEND_PID" 2>/dev/null || true
        fi
        rm -f "$LOG_DIR/backend.pid"
    fi

    echo "已停止所有服务"
}

# 捕获退出信号
trap cleanup EXIT INT TERM

# 启动后端（后台运行）
echo "启动后端服务..."
cd "$PROJECT_ROOT/server"
./gradlew bootRun > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$LOG_DIR/backend.pid"
echo "后端已启动 (PID: $BACKEND_PID)"
echo "后端日志: $LOG_DIR/backend.log"

# 等待后端启动
echo "等待后端就绪..."
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✓ 后端就绪"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "✗ 后端启动超时，请检查日志: $LOG_DIR/backend.log"
        exit 1
    fi
    sleep 1
done

# 启动前端（前台运行）
echo ""
echo "启动前端服务..."
cd "$PROJECT_ROOT"
echo "======================================"
echo "前端地址: http://localhost:3000"
echo "后端地址: http://localhost:8080"
echo "API文档: http://localhost:8080/swagger-ui.html"
echo "按 Ctrl+C 停止所有服务"
echo "======================================"
echo ""

exec pnpm dev
