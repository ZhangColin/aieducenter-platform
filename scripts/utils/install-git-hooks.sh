#!/bin/bash
# 安装 Git hooks

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."

cd "$PROJECT_ROOT"

echo "安装 Git hooks..."

# 设置 hooks 路径
git config core.hooksPath .githooks

echo "✅ Git hooks 安装完成"
echo ""
echo "已启用的 hooks:"
echo "  - pre-push: 推送前自动执行验证"
echo ""
echo "验证命令: ./scripts/validate.sh"
echo "临时跳过: git push --no-verify"
