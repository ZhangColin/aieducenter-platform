#!/bin/bash
# 测试共用函数

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.test.yml"

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

# 清理测试环境
cleanup_test_env() {
    docker-compose -f "$COMPOSE_FILE" -p test --profile test down -v 2>/dev/null || true
}

# 等待服务健康
wait_for_service() {
    local service=$1
    local max_attempts=${2:-30}
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if docker-compose -f "$COMPOSE_FILE" -p test ps "$service" 2>/dev/null | grep -q "Up"; then
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 1
    done

    color_error "服务 $service 启动超时"
    return 1
}

# 运行后端单元测试（无需 docker）
run_backend_unit_tests() {
    print_header "后端单元测试"

    cd "$PROJECT_ROOT/server"
    ./gradlew test --no-daemon

    color_info "后端单元测试通过"
}

# 运行前端单元测试（无需 docker）
run_frontend_unit_tests() {
    print_header "前端单元测试"

    cd "$PROJECT_ROOT"
    pnpm test --run

    color_info "前端单元测试通过"
}

# 运行后端集成测试
run_backend_integration_tests() {
    print_header "后端集成测试"

    # 启动中间件 + 测试容器
    docker-compose -f "$COMPOSE_FILE" -p test --profile test up -d postgres redis backend-tests

    # 等待测试完成
    docker-compose -f "$COMPOSE_FILE" -p test logs -f backend-tests

    # 获取退出码
    local exit_code=$(docker-compose -f "$COMPOSE_FILE" -p test ps -q backend-tests | xargs -r docker inspect -f '{{.State.ExitCode}}' 2>/dev/null)
    exit_code=${exit_code:-1}

    # 清理
    docker-compose -f "$COMPOSE_FILE" -p test --profile test down -v

    if [ "$exit_code" -eq 0 ]; then
        color_info "后端集成测试通过"
    else
        color_error "后端集成测试失败 (退出码: $exit_code)"
        exit 1
    fi
}

# 运行前端集成测试
run_frontend_integration_tests() {
    print_header "前端集成测试"

    # 启动中间件 + 后端 + 前端测试
    docker-compose -f "$COMPOSE_FILE" -p test --profile test up -d postgres redis backend frontend-tests

    # 等待测试完成
    docker-compose -f "$COMPOSE_FILE" -p test logs -f frontend-tests

    # 获取退出码
    local exit_code=$(docker-compose -f "$COMPOSE_FILE" -p test ps -q frontend-tests | xargs -r docker inspect -f '{{.State.ExitCode}}' 2>/dev/null)
    exit_code=${exit_code:-1}

    # 清理
    docker-compose -f "$COMPOSE_FILE" -p test --profile test down -v

    if [ "$exit_code" -eq 0 ]; then
        color_info "前端集成测试通过"
    else
        color_error "前端集成测试失败 (退出码: $exit_code)"
        exit 1
    fi
}

# 运行 E2E 测试
run_e2e_tests() {
    print_header "E2E 测试"

    # 启动完整环境
    docker-compose -f "$COMPOSE_FILE" -p test --profile test up -d postgres redis backend web admin e2e-tests

    # 等待测试完成
    docker-compose -f "$COMPOSE_FILE" -p test logs -f e2e-tests

    # 获取退出码
    local exit_code=$(docker-compose -f "$COMPOSE_FILE" -p test ps -q e2e-tests | xargs -r docker inspect -f '{{.State.ExitCode}}' 2>/dev/null)
    exit_code=${exit_code:-1}

    # 清理
    docker-compose -f "$COMPOSE_FILE" -p test --profile test down -v

    if [ "$exit_code" -eq 0 ]; then
        color_info "E2E 测试通过"
    else
        color_error "E2E 测试失败 (退出码: $exit_code)"
        exit 1
    fi
}
