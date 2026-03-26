#!/bin/bash
# 确保测试服务（postgres:25432, redis:26379）正在运行

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.test.yml"
PROJECT="aiedu-test"

# 检查端口是否被占用
is_port_in_use() {
    nc -z localhost "$1" 2>/dev/null
}

# 检查服务是否正确运行
is_postgres_ready() {
    is_port_in_use 25432 && \
        docker exec aiedu-postgres-test pg_isready -U aiedu_test &>/dev/null
}

is_redis_ready() {
    is_port_in_use 26379 && \
        docker exec aiedu-redis-test redis-cli ping &>/dev/null
}

# 清理旧容器（如果存在但端口不对）
cleanup_old_containers() {
    # 如果容器存在但端口不对，停止并删除
    if docker ps -a -q -f name=aiedu-postgres-test | grep -q .; then
        if ! is_port_in_use 25432; then
            docker rm -f aiedu-postgres-test &>/dev/null || true
        fi
    fi
    if docker ps -a -q -f name=aiedu-redis-test | grep -q .; then
        if ! is_port_in_use 26379; then
            docker rm -f aiedu-redis-test &>/dev/null || true
        fi
    fi
}

# 启动服务
start_services() {
    echo "启动测试服务..."
    cleanup_old_containers
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT" up -d

    # 等待服务就绪
    echo "等待服务就绪..."
    for i in $(seq 1 30); do
        if is_postgres_ready && is_redis_ready; then
            echo "✓ 测试服务就绪"
            return 0
        fi
        sleep 1
    done

    echo "错误: 服务启动超时"
    exit 1
}

# 主逻辑
if is_postgres_ready && is_redis_ready; then
    # 服务已运行且端口正确
    exit 0
else
    # 启动服务
    start_services
fi
