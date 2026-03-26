#!/bin/bash
# 本地开发环境启动

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
LOG_DIR="$PROJECT_ROOT/logs"

# 创建日志目录
mkdir -p "$LOG_DIR"

# 端口清理函数
kill_process_on_port() {
    local port=$1
    local service_name=$2

    # 检查端口是否被占用
    if lsof -ti :"$port" >/dev/null 2>&1; then
        echo "  ⚠ 端口 $port 被 $service_name 占用，尝试终止进程..."
        local pid=$(lsof -ti :"$port" 2>/dev/null)
        if [ -n "$pid" ]; then
            kill -9 "$pid" 2>/dev/null || true
            sleep 1
            if lsof -ti :"$port" >/dev/null 2>&1; then
                echo "  ✗ 无法终止端口 $port 上的进程，请手动处理"
                return 1
            else
                echo "  ✓ 已终止端口 $port 上的进程"
            fi
        fi
    fi
}

# Docker 容器端口清理函数
kill_docker_on_port() {
    local port=$1
    local container_name=$2

    # 查找占用该端口的容器
    local container_id=$(docker ps -q -f "publish=$port" 2>/dev/null)
    if [ -n "$container_id" ]; then
        # 检查是否是我们期望的容器
        local container_name_actual=$(docker inspect --format='{{.Name}}' "$container_id" 2>/dev/null | sed 's|^/||')
        if [ "$container_name_actual" = "$container_name" ]; then
            echo "  ✓ $container_name 已在运行"
            return 0
        fi

        # 是其他容器占用端口
        echo "  ⚠ 端口 $port 被容器 $container_name_actual 占用，尝试停止..."
        docker stop "$container_id" >/dev/null 2>&1 || true
        sleep 1
        echo "  ✓ 已停止容器 $container_name_actual"
    fi
}

echo "=========================================="
echo "   启动本地开发环境"
echo "=========================================="

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

# 启动 Redis 容器（如果未运行）
echo ""
echo "→ 检查 Redis..."
kill_docker_on_port 6379 "aiedu-redis-local"

if docker ps -a --format '{{.Names}}' | grep -q "^aiedu-redis-local$"; then
    docker start aiedu-redis-local >/dev/null 2>&1
    echo "  ✓ Redis 已启动"
else
    docker run -d --name aiedu-redis-local -p 6379:6379 redis:7-alpine >/dev/null 2>&1
    echo "  ✓ Redis 已启动"
fi

# 启动后端（后台运行）
echo ""
echo "→ 启动后端服务..."
kill_process_on_port 8080 "后端服务"

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
echo "→ 检查前端端口..."
kill_process_on_port 3000 "前端 web 服务"
kill_process_on_port 3001 "前端 admin 服务"

echo ""
echo "=========================================="
echo "   本地开发环境已就绪"
echo "=========================================="
echo "   前端:   http://localhost:3000 (web)"
echo "          http://localhost:3001 (admin)"
echo "   后端:   http://localhost:8080"
echo "   Redis:  localhost:6379 (Docker)"
echo ""
echo "   按 Ctrl+C 停止应用服务"
echo "   完全清理: ./scripts/local-stop.sh"
echo "=========================================="
echo ""

cd "$PROJECT_ROOT"
exec pnpm dev
