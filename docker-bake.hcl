# Docker BuildKit bake 配置
# 注意：构建产物由本地脚本生成，Docker 只负责打包

target "backend" {
    context = "."
    dockerfile = "docker/backend/Dockerfile"
}

target "web" {
    context = "."
    dockerfile = "docker/frontend/Dockerfile.web"
}

target "admin" {
    context = "."
    dockerfile = "docker/frontend/Dockerfile.admin"
}
