#!/bin/bash
# 前后端集成验证脚本

set -e

# 默认参数
PORT=${PORT:-8080}
TIMEOUT=${TIMEOUT:-30}
API_URL="http://localhost:$PORT/api/health"

echo "======================================"
echo "前后端集成验证"
echo "======================================"
echo "API 地址: $API_URL"
echo "超时时间: ${TIMEOUT}秒"
echo ""

# 等待后端启动
echo "等待后端服务启动..."
for i in $(seq 1 $TIMEOUT); do
    if curl -sf "$API_URL" > /dev/null 2>&1; then
        echo "✓ 后端服务已就绪"
        break
    fi

    if [ $i -eq $TIMEOUT ]; then
        echo "✗ FAIL: 后端服务启动超时"
        exit 1
    fi

    echo "等待中... ($i/$TIMEOUT)"
    sleep 1
done

# 调用健康检查端点
echo ""
echo "调用健康检查端点..."
RESPONSE=$(curl -s "$API_URL")

# 检查响应
if [ -z "$RESPONSE" ]; then
    echo "✗ FAIL: 无响应"
    exit 1
fi

echo "收到响应:"
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

# 验证 JSON 格式（使用 python 或基本 grep）
if command -v python3 &> /dev/null; then
    if ! echo "$RESPONSE" | python3 -c "import sys, json; json.load(sys.stdin)" 2>/dev/null; then
        echo "✗ FAIL: 无效的 JSON 格式"
        exit 1
    fi
fi

# 验证必要字段（使用 python）
if ! command -v python3 &> /dev/null; then
    echo "✗ FAIL: 需要 python3 来解析响应"
    exit 1
fi

CODE=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('code', 0))" 2>/dev/null)
STATUS=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('data', {}).get('status', ''))" 2>/dev/null)

if [ "$CODE" != "200" ]; then
    echo "✗ FAIL: code 不是 200 (实际: $CODE)"
    exit 1
fi

if [ "$STATUS" != "ok" ]; then
    echo "✗ FAIL: status 不是 ok (实际: $STATUS)"
    exit 1
fi

# 验证 timestamp 存在
if ! echo "$RESPONSE" | grep -q '"timestamp":"'; then
    echo "✗ FAIL: 缺少 timestamp 字段"
    exit 1
fi

echo "======================================"
echo "✅ PASS - 所有验证通过"
echo "======================================"
echo "- 响应格式正确"
echo "- code = 200"
echo "- status = ok"
echo "- timestamp 存在"
echo ""
