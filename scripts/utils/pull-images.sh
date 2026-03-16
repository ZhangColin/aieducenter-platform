#!/bin/bash
# 预拉取 Docker 基础镜像
# 用途：首次部署或镜像更新时运行，之后 deploy 脚本无需网络

set -e

echo "=========================================="
echo "   预拉取 Docker 基础镜像"
echo "=========================================="
echo ""
echo "提示：如果拉取失败，请开启梯子后重试"
echo ""

# 后端镜像
echo "→ 拉取后端镜像..."
echo "  eclipse-temurin:21-jdk"
docker pull eclipse-temurin:21-jdk
echo "  eclipse-temurin:21-jre-alpine"
docker pull eclipse-temurin:21-jre-alpine

# 前端镜像
echo ""
echo "→ 拉取前端镜像..."
echo "  node:20-alpine"
docker pull node:20-alpine

# 基础设施镜像
echo ""
echo "→ 拉取基础设施镜像..."
echo "  postgres:16-alpine"
docker pull postgres:16-alpine
echo "  redis:7-alpine"
docker pull redis:7-alpine

echo ""
echo "=========================================="
echo "   ✅ 镜像预拉取完成！"
echo "=========================================="
echo ""
echo "当前镜像列表："
docker images | grep -E "eclipse-temurin|node|postgres|redis" || echo "  （无）"
echo ""
echo "之后运行 deploy 脚本时，Docker 将直接使用这些缓存镜像。"
