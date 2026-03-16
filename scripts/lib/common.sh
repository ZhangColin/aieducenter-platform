#!/bin/bash
# 共性函数库
# 供其他脚本 source 使用

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

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

# 验证代码质量
run_validate() {
    echo ""
    echo "[1/4] 验证代码质量..."
    "$SCRIPT_DIR/validate/all.sh"
}

# 构建后端
run_build_backend() {
    echo ""
    echo "[2/4] 构建后端..."
    "$SCRIPT_DIR/build/backend.sh"
}

# 构建前端
run_build_frontend() {
    echo ""
    echo "[3/4] 构建前端..."
    "$SCRIPT_DIR/build/frontend.sh"
}

# 构建 Docker 镜像
run_build_docker() {
    echo ""
    echo "[4/4] 构建 Docker 镜像..."
    docker buildx bake -f docker-bake.hcl --load backend web admin
}

# 检查文件存在
check_file_exists() {
    local file="$1"
    local description="$2"
    if [ ! -f "$file" ]; then
        color_error "$description 不存在: $file"
        exit 1
    fi
}

# 获取项目根目录
get_project_root() {
    echo "$(dirname "$SCRIPT_DIR")"
}
