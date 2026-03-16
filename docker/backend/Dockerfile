# 后端 Docker 镜像
# 注意：构建产物由 build-backend.sh 在本地生成，Docker 只负责打包

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 安装健康检查工具
RUN apk add --no-cache wget

# 复制已构建好的 JAR（由 build-backend.sh 生成）
COPY server/build/libs/*.jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
