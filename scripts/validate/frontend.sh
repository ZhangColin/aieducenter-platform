#!/bin/bash
# 前端验证：类型检查 + Lint + 构建

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."

cd "$PROJECT_ROOT"

echo "  → 类型检查..."
pnpm -r typecheck

echo "  → Lint 检查（暂时跳过，ESLint 10 兼容性问题待处理）..."
# TODO: 迁移到 ESLint CLI 后恢复
# pnpm -r lint || true

echo "  → 构建验证 (web)..."
cd "$PROJECT_ROOT/web"
pnpm build

echo "  → 构建验证 (admin)..."
cd "$PROJECT_ROOT/admin"
pnpm build

echo "  ✅ 前端验证通过"
