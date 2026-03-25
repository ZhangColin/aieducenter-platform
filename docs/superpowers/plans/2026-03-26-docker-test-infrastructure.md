# Docker 测试环境实施方案

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 实现在 Docker 容器内运行测试的干净隔离环境，测试容器通过内部网络连接中间件

**架构:** 测试容器在独立 Docker 网络中运行，通过服务名访问 PostgreSQL (postgres:5432) 和 Redis (redis:6379)，不映射端口到宿主机

**技术栈:** Docker Compose, Gradle, pnpm, PostgreSQL, Redis

---

## 文件结构

| 文件 | 职责 | 操作 |
|------|------|------|
| `docker/test/Dockerfile.backend` | 后端测试容器 | 创建 |
| `docker/test/Dockerfile.frontend` | 前端测试容器 | 创建 |
| `docker/test/.dockerignore` | 排除不需要的文件 | 创建 |
| `scripts/test/common.sh` | 测试共用函数 | 修改（修复 typo + 添加 --profile） |
| `scripts/test/docker-compose.test.yml` | Docker Compose 测试配置 | 修改（统一 profile + 修复 volume） |

---

## Task 1: 创建后端测试 Dockerfile

**文件:** 创建 `docker/test/Dockerfile.backend`

- [ ] **创建 `docker/test/` 目录**
```bash
mkdir -p docker/test
```

- [ ] **创建 `docker/test/Dockerfile.backend`**

```dockerfile
# 后端测试容器 Dockerfile
# 在隔离的 Docker 环境中运行后端测试

FROM eclipse-temurin:21-jdk-alpine

# 安装必要工具
RUN apk add --no-cache bash curl

# 设置工作目录
WORKDIR /app

# 复制 Gradle wrapper（使用绝对路径避免歧义）
COPY server/gradlew /app/server/
COPY server/gradle/wrapper/gradle-wrapper.jar /app/server/gradle/wrapper/
COPY server/gradle/wrapper/gradle-wrapper.properties /app/server/gradle/wrapper/

# 复制构建配置
COPY server/build.gradle.kts /app/server/
COPY server/settings.gradle.kts /app/server/
COPY server/src /app/server/src

# Gradle 配置：禁用守护进程
ENV GRADLE_USER_HOME=/app/server/.gradle
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.caching=false"

# 切换到 server 目录运行测试
WORKDIR /app/server

# 运行测试（运行时，不是构建时）
CMD ["./gradlew", "test", "--no-daemon"]
```

---

## Task 2: 创建前端测试 Dockerfile

- [ ] **创建 `docker/test/Dockerfile.frontend`**

```dockerfile
# 前端测试容器 Dockerfile
# 在隔离的 Docker 环境中运行前端测试

FROM node:20-alpine

# 安装 pnpm
RUN npm install -g pnpm@8

WORKDIR /app

# 复制 monorepo 配置（使用绝对路径）
COPY package.json /app/
COPY pnpm-workspace.yaml /app/
COPY pnpm-lock.yaml /app/

# 复制 packages 目录（共享依赖）
COPY packages/ /app/packages/

# 复制前端项目
COPY web/ /app/web/
COPY admin/ /app/admin/

# 安装依赖
RUN pnpm install --frozen-lockfile

# 运行测试（只测试 web 和 admin，跳过没有测试的 packages）
CMD ["sh", "-c", "pnpm --filter './web' test && pnpm --filter './admin' test"]
```

---

## Task 3: 创建 .dockerignore

- [ ] **创建 `docker/test/.dockerignore`**

```dockerignore
# Gradle
.gradle/
build/
bin/
!gradle/wrapper/

# Node
node_modules/
.pnpm-store/
.next/
coverage/
*.log*
*.tsbuildinfo

# Git
.git/

# IDE
.idea/
.vscode/
*.iml
```

---

## Task 4: 修复 common.sh 中的 typo

**文件:** 修改 `scripts/test/common.sh`

- [ ] **修复第 131 行的 typo**

将 `$exit码` 改为 `$exit_code`：

```bash
# 修复前:
color_error "E2E 测试失败 (退出码: $exit码)"

# 修复后:
color_error "E2E 测试失败 (退出码: $exit_code)"
```

---

## Task 5: 统一 docker-compose.test.yml 的 profile

**文件:** 修改 `scripts/test/docker-compose.test.yml`

- [ ] **将所有 services 的 profiles 统一为 `test`**

| 服务 | 原 profiles | 新 profiles |
|------|------------|------------|
| postgres | `["integration", "e2e"]` | `["test"]` |
| redis | `["integration", "e2e"]` | `["test"]` |
| backend | `["integration-backend", "integration-frontend", "e2e"]` | `["test"]` |
| web | `["integration-frontend", "e2e"]` | `["test"]` |
| admin | `["integration-frontend", "e2e"]` | `["test"]` |
| backend-tests | `["integration"]` | `["test"]` |
| frontend-tests | `["integration"]` | `["test"]` |
| e2e-tests | `["e2e"]` | `["test"]` |

- [ ] **修复 backend-tests 的 volume 挂载路径**

```yaml
# 修复前（与 WORKDIR /app/server 不匹配）:
volumes:
  - ../server/build/reports:/app/build/reports

# 修复后（与 Dockerfile.backend 的 WORKDIR /app/server 对齐）:
volumes:
  - ../server/build/reports:/app/server/build/reports
```

---

## Task 6: 更新测试脚本添加 --profile test

**文件:** 修改 `scripts/test/common.sh`

- [ ] **添加 `--profile test` 到所有 docker-compose up 命令**

```bash
# 后端集成测试
run_backend_integration_tests() {
    print_header "后端集成测试"

    docker-compose -f "$COMPOSE_FILE" -p test --profile test up -d postgres redis backend-tests

    docker-compose -f "$COMPOSE_FILE" -p test logs -f backend-tests

    local exit_code=$(docker-compose -f "$COMPOSE_FILE" -p test ps -q backend-tests | xargs -r docker inspect -f '{{.State.ExitCode}}' 2>/dev/null)
    exit_code=${exit_code:-1}

    docker-compose -f "$COMPOSE_FILE" -p test --profile test down -v

    if [ "$exit_code" = "0" ]; then
        color_info "后端集成测试通过"
    else
        color_error "后端集成测试失败 (退出码: $exit_code)"
        exit 1
    fi
}

# 前端集成测试
run_frontend_integration_tests() {
    print_header "前端集成测试"

    docker-compose -f "$COMPOSE_FILE" -p test --profile test up -d postgres redis backend frontend-tests

    docker-compose -f "$COMPOSE_FILE" -p test logs -f frontend-tests

    local exit_code=$(docker-compose -f "$COMPOSE_FILE" -p test ps -q frontend-tests | xargs -r docker inspect -f '{{.State.ExitCode}}' 2>/dev/null)
    exit_code=${exit_code:-1}

    docker-compose -f "$COMPOSE_FILE" -p test --profile test down -v

    if [ "$exit_code" = "0" ]; then
        color_info "前端集成测试通过"
    else
        color_error "前端集成测试失败 (退出码: $exit_code)"
        exit 1
    fi
}

# E2E 测试
run_e2e_tests() {
    print_header "E2E 测试"

    docker-compose -f "$COMPOSE_FILE" -p test --profile test up -d postgres redis backend web admin e2e-tests

    docker-compose -f "$COMPOSE_FILE" -p test logs -f e2e-tests

    local exit_code=$(docker-compose -f "$COMPOSE_FILE" -p test ps -q e2e-tests | xargs -r docker inspect -f '{{.State.ExitCode}}' 2>/dev/null)
    exit_code=${exit_code:-1}

    docker-compose -f "$COMPOSE_FILE" -p test --profile test down -v

    if [ "$exit_code" = "0" ]; then
        color_info "E2E 测试通过"
    else
        color_error "E2E 测试失败 (退出码: $exit_code)"
        exit 1
    fi
}
```

---

## Task 7: 验证测试运行

- [ ] **构建后端测试镜像**
```bash
# 注意：docker-compose.test.yml 的 build context 是 ../（项目根目录）
docker-compose -f scripts/test/docker-compose.test.yml -p test build backend-tests
```

- [ ] **构建前端测试镜像**
```bash
docker-compose -f scripts/test/docker-compose.test.yml -p test build frontend-tests
```

- [ ] **运行后端集成测试**
```bash
docker-compose -f scripts/test/docker-compose.test.yml -p test --profile test up postgres redis backend-tests
```

- [ ] **检查测试结果**
```bash
docker-compose -f scripts/test/docker-compose.test.yml -p test logs backend-tests
```

- [ ] **清理测试环境**
```bash
docker-compose -f scripts/test/docker-compose.test.yml -p test --profile test down -v
```

- [ ] **运行全部测试**
```bash
./scripts/test/run.sh --all
```

---

## Task 8: 提交更改

- [ ] **提交测试基础设施**
```bash
git add docker/test/ scripts/test/
git commit -m "feat: add docker test infrastructure

- Add backend and frontend test Dockerfiles
- Unify docker-compose profiles to 'test'
- Fix typo in common.sh (\$exit码 -> \$exit_code)
- Fix backend-tests volume mount path (align with WORKDIR /app/server)
- Add --profile test to docker-compose commands
- Improve exit code handling with default to failure

Co-Authored-By: Claude Opus 4.6 (1M context) <noreply@anthropic.com>"
```

---

## 验证清单

运行以下命令验证实施结果：

```bash
# 构建测试镜像
docker-compose -f scripts/test/docker-compose.test.yml -p test build backend-tests frontend-tests

# 运行测试
./scripts/test/run.sh --all

# 检查测试报告
open server/build/reports/tests/test/index.html
```
