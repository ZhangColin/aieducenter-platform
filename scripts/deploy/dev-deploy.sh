#!/bin/bash
# 开发环境打包发布（本地 → 服务器）
# 用法: ./scripts/deploy/dev-deploy.sh [server_user] [server_host]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 服务器配置（支持参数或默认值）
SERVER_USER=${1:-${SERVER_USER:-root}}
SERVER_HOST=${2:-${SERVER_HOST:-localhost}}
SERVER_PORT=${SERVER_PORT:-22}
SERVER_DEPLOY_DIR=${SERVER_DEPLOY_DIR:-/opt/aiedu}
SERVER_PASSWORD=${SERVER_PASSWORD:-}

# 加载共性函数
source "$SCRIPT_DIR/common.sh"

print_header "开发环境打包发布"

echo "服务器: $SERVER_USER@$SERVER_HOST:$SERVER_PORT"
echo "部署目录: $SERVER_DEPLOY_DIR"
echo ""

# 最后确认
read -p "确认发布到开发环境？(yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "已取消"
    exit 0
fi

# 执行部署流程
run_tests
run_build
DEPLOY_DIR=$(prepare_deploy_package "dev")
upload_to_server "dev"
remote_build "dev"
remote_start

echo ""
print_header "开发环境发布完成"
echo ""
echo "服务器: $SERVER_USER@$SERVER_HOST"
echo "部署目录: $SERVER_DEPLOY_DIR"
echo ""
echo "如需手动操作："
echo "  ssh $SERVER_USER@$SERVER_HOST"
echo "  cd $SERVER_DEPLOY_DIR"
echo "  ./build.sh  # 构建镜像"
echo "  ./start.sh  # 启动服务"
echo ""
