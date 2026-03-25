#!/bin/bash
# 测试运行入口 - 所有测试在 Docker 容器内执行
# 用法: ./scripts/test/run.sh [选项]
#
# 选项:
#   --backend           后端测试（Docker 容器内）
#   --frontend          前端测试（Docker 容器内）
#   --e2e               E2E 测试（Docker 容器内）
#   --all               全部测试（默认）

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 加载共用函数
source "$SCRIPT_DIR/common.sh"

# 显示用法
show_usage() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  --backend           运行后端测试"
    echo "  --frontend          运行前端测试"
    echo "  --e2e               运行 E2E 测试"
    echo "  --all               运行全部测试（默认）"
    echo ""
    echo "示例:"
    echo "  $0 --backend"
    echo "  $0 --frontend"
    echo "  $0 --e2e"
    echo "  $0 --all"
    exit 1
}

# 解析参数
TEST_TYPE="${1:---all}"

case $TEST_TYPE in
    --backend)
        print_header "运行后端测试"
        run_backend_integration_tests
        print_header "后端测试完成"
        ;;

    --frontend)
        print_header "运行前端测试"
        run_frontend_integration_tests
        print_header "前端测试完成"
        ;;

    --e2e)
        print_header "运行 E2E 测试"
        run_e2e_tests
        print_header "E2E 测试完成"
        ;;

    --all)
        print_header "运行全部测试"
        echo ""
        echo "[1/3] 后端测试..."
        run_backend_integration_tests

        echo ""
        echo "[2/3] 前端测试..."
        run_frontend_integration_tests

        echo ""
        echo "[3/3] E2E 测试..."
        run_e2e_tests

        print_header "全部测试完成 ✅"
        ;;

    *)
        color_error "未知选项: $TEST_TYPE"
        show_usage
        ;;
esac
