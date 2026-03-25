#!/bin/bash
# 全量验证入口 - Git hook 调用

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."

cd "$PROJECT_ROOT"

echo "=========================================="
echo "   开始全量验证..."
echo "=========================================="

# 1. 后端验证
echo ""
echo "[1/2] 验证后端..."
"$SCRIPT_DIR/backend.sh"

# 2. 前端验证
echo ""
echo "[2/2] 验证前端..."
"$SCRIPT_DIR/frontend.sh"

echo ""
echo "=========================================="
echo "   ✅ 全量验证通过！"
echo "=========================================="
