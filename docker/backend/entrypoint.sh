#!/bin/sh
# 后端启动脚本 - 等待数据库就绪

set -e

echo "等待数据库启动..."

# 检查数据库连接
if [ -n "$DB_HOST" ]; then
    until nc -z "$DB_HOST" 5432; do
        echo "数据库未就绪，等待中..."
        sleep 2
    done
    echo "✓ 数据库已就绪"
fi

# 启动应用
exec java -jar /app/app.jar
