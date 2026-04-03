#!/bin/bash
# 测试运行脚本
# 用法: ./scripts/test/run.sh [选项]
#
# 选项:
#   --backend           后端测试（默认）
#   --quick             快速测试（跳过清理）

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."

# 颜色输出
color_info() { echo "\033[0;32m✓ $*\033[0m"; }
color_warn() { echo "\033[0;33m⚠ $*\033[0m"; }
color_error() { echo "\033[0;31m✗ $*\033[0m"; }

# 解析参数
QUICK=false
while [[ $# -gt 0 ]]; do
    case $1 in
        --quick)
            QUICK=true
            shift
            ;;
        --backend)
            shift
            ;;
        *)
            echo "未知选项: $1"
            echo "用法: $0 [--quick]"
            exit 1
            ;;
    esac
done

# 1. 确保测试服务运行
echo "=========================================="
echo "   检查测试服务"
echo "=========================================="
bash "$SCRIPT_DIR/ensure-services.sh"

# 2. 清理测试数据
if [ "$QUICK" = false ]; then
    echo ""
    echo "=========================================="
    echo "   清理测试数据"
    echo "=========================================="
    docker exec aiedu-postgres-test psql -U aiedu_test -d aieducenter_test -c "
        DROP SCHEMA public CASCADE;
        CREATE SCHEMA public;
        GRANT ALL ON SCHEMA public TO aiedu_test;
    " &>/dev/null || true
    docker exec aiedu-redis-test redis-cli FLUSHALL &>/dev/null || true
    color_info "测试数据已清理"
fi

# 3. 运行测试
echo ""
echo "=========================================="
echo "   运行后端测试"
echo "=========================================="
cd "$PROJECT_ROOT/server"

mvn test

echo ""
color_info "测试完成！"
