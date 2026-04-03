#!/bin/bash
# 后端验证：编译 + 测试

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."

cd "$PROJECT_ROOT/server"

echo "  → 清理旧构建..."
mvn -q clean

echo "  → 编译..."
mvn -q compile

echo "  → 运行单元测试..."
mvn test


echo "  ✅ 后端验证通过"
