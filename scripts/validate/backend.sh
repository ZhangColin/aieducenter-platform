#!/bin/bash
# 后端验证：编译 + 测试

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."

cd "$PROJECT_ROOT/server"

echo "  → 清理旧构建..."
./gradlew clean --no-daemon --quiet

echo "  → 编译..."
./gradlew compileJava --no-daemon --quiet

echo "  → 运行单元测试..."
./gradlew test --no-daemon

# 检查是否有 ArchUnit 测试
if ./gradlew tasks --all 2>/dev/null | grep -q "archUnitTest"; then
    echo "  → 运行 ArchUnit 架构检查..."
    ./gradlew archUnitTest --no-daemon --quiet
else
    echo "  → (跳过 ArchUnit - 未配置)"
fi

echo "  ✅ 后端验证通过"
