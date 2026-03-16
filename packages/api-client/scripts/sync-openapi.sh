#!/bin/bash
# 从后端同步 OpenAPI JSON 到本地

OPENAPI_URL=${OPENAPI_URL:-http://localhost:8080/v3/api-docs}
OUTPUT_FILE=./openapi.json

echo "正在从 $OPENAPI_URL 获取 OpenAPI schema..."

if curl -f -s "$OPENAPI_URL" -o "$OUTPUT_FILE"; then
  echo "✓ OpenAPI schema 已同步到 $OUTPUT_FILE"
  echo "运行 pnpm gen:api 生成类型"
else
  echo "✗ 获取失败，请确认后端服务是否运行"
  exit 1
fi
