# Feature: CI/CD 基础配置 — 接口契约

> 版本：v1.0 | 日期：2026-03-16
> 状态：待审核

---

## 一、脚本接口定义

### 1.1 scripts/validate.sh

**描述**：全量验证入口脚本，由 Git hook 或手动调用

**输入**：无

**输出**：
- 成功：返回码 0，打印 "✅ 全量验证通过！"
- 失败：返回码非 0，打印错误信息

**行为**：
1. 顺序调用 validate-backend.sh 和 validate-frontend.sh
2. 任意步骤失败则立即终止
3. 打印进度信息

### 1.2 scripts/validate-backend.sh

**描述**：后端代码质量验证

**输入**：无

**前置条件**：
- cartisan-boot 已发布到本地 Maven 仓库
- Java 21 已安装

**行为**：
1. 清理旧构建（`./gradlew clean`）
2. 编译（`./gradlew compileJava`）
3. 运行单元测试（`./gradlew test`）
4. 运行 ArchUnit 架构检查

**输出**：
- 成功：返回码 0，打印 "✅ 后端验证通过"
- 失败：返回码非 0，打印 Gradle 错误输出

### 1.3 scripts/validate-frontend.sh

**描述**：前端代码质量验证

**输入**：无

**前置条件**：
- Node.js 20+ 已安装
- pnpm 已安装

**行为**：
1. 类型检查（`pnpm -r typecheck`）
2. Lint 检查（`pnpm -r lint`）
3. 构建验证（`pnpm build:web` 和 `pnpm build:admin`）

**输出**：
- 成功：返回码 0，打印 "✅ 前端验证通过"
- 失败：返回码非 0，打印错误输出

### 1.4 scripts/build-backend.sh

**描述**：构建后端 JAR 包

**输入**：无

**行为**：
1. 进入 cartisan-boot 目录，执行 `publishToMavenLocal`
2. 进入 server 目录，执行 `./gradlew build -x test`
3. 输出 JAR 路径

**输出**：
- 成功：返回码 0，打印 JAR 文件路径
- 失败：返回码非 0

### 1.5 scripts/build-frontend.sh

**描述**：构建前端静态文件

**输入**：无

**行为**：
1. 执行 `pnpm build:web`
2. 执行 `pnpm build:admin`

**输出**：
- 成功：返回码 0，打印构建完成信息
- 失败：返回码非 0

### 1.6 scripts/deploy-dev.sh

**描述**：开发环境部署

**输入**：
- `$1`：环境标识（dev/test，默认 dev）

**行为**：
1. 调用 validate.sh 验证
2. 调用 build-backend.sh 构建后端
3. 调用 build-frontend.sh 构建前端
4. 停止旧容器（`docker-compose down`）
5. 启动新容器（`docker-compose up -d --build`）

**输出**：
- 成功：打印服务访问地址
- 失败：返回码非 0

### 1.7 scripts/deploy-prod.sh

**描述**：生产环境部署

**输入**：无

**前置条件**：
- `.env.prod` 文件存在
- 生产数据库连接信息已配置

**行为**：
1. 加载 `.env.prod` 环境变量
2. 要求用户手动确认（输入 "yes"）
3. 验证 → 构建 → 部署

**输出**：
- 成功：打印 "✅ 生产环境部署完成"
- 失败：返回码非 0

### 1.8 scripts/install-git-hooks.sh

**描述**：安装 Git hooks

**行为**：
1. 设置 `git config core.hooksPath .githooks`
2. 将 pre-push hook 关联到 `.githooks/pre-push`

**输出**：打印安装完成信息

---

## 二、Git Hook 接口

### 2.1 .githooks/pre-push

**触发时机**：执行 `git push` 前

**行为**：
1. 调用 `./scripts/validate.sh`
2. 验证成功：允许推送
3. 验证失败：打印错误信息，返回码 1，阻止推送

**输出**：
- 成功：打印 "✅ 验证通过，可以推送。"
- 失败：打印 "❌ 验证失败！推送已中止。"

---

## 三、Docker 配置接口

### 3.1 后端 Dockerfile

**构建阶段（builder）**：
- 基础镜像：`eclipse-temurin:21-jdk`
- 工作目录：`/build`
- 复制 cartisan-boot 源码，执行 `publishToMavenLocal`
- 复制 server 源码，执行 `./gradlew build`
- 缓存：Gradle 依赖缓存

**运行阶段**：
- 基础镜像：`eclipse-temurin:21-jre-alpine`
- 工作目录：`/app`
- 复制 JAR：`app.jar`
- 健康检查：`/actuator/health`
- 暴露端口：8080

### 3.2 前端 Dockerfile

**构建阶段**：
- 基础镜像：`node:20-alpine`
- 工作目录：`/build`
- 安装 pnpm，执行 `pnpm build`

**运行阶段**：
- Web 基础镜像：`node`（standalone 模式）或 `nginx:alpine`
- 暴露端口：3000（web）/ 3001（admin）

### 3.3 docker-compose.yml 配置项

#### dev.yml（开发环境）

| 服务 | 镜像 | 端口 | Volume | 依赖 |
|------|------|------|--------|------|
| postgres | postgres:16 | 5432 | postgres-data | - |
| redis | redis:7-alpine | 6379 | redis-data | - |
| backend | 自建 | 8080 | - | postgres(健康), redis |
| web | 自建 | 3000 | - | - |
| admin | 自建 | 3001 | - | - |

#### prod.yml（生产环境）

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| backend | 自建 | 8080 | 数据库连接云服务 |
| web | 自建 | 3000 | - |
| admin | 自建 | 3001 | - |

---

## 四、环境变量配置

### 4.1 后端环境变量

| 变量名 | 开发环境默认值 | 生产环境 | 说明 |
|--------|---------------|---------|------|
| `SPRING_PROFILES_ACTIVE` | `dev` | `prod` | Spring Profile |
| `POSTGRES_PASSWORD` | `dev123` | 从 .env.prod 读取 | 数据库密码 |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/aieducenter` | 云服务地址 | 数据库 URL |
| `SPRING_DATASOURCE_USERNAME` | `aiedu` | 云服务用户名 | 数据库用户 |
| `REDIS_HOST` | `redis` | 云服务地址或容器名 | Redis 主机 |
| `REDIS_PORT` | `6379` | `6379` | Redis 端口 |

### 4.2 前端环境变量

| 变量名 | 开发环境 | 生产环境 | 说明 |
|--------|---------|---------|------|
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | 实际域名 | 后端 API 地址 |

---

## 五、Flyway 迁移配置

### 5.1 迁移脚本位置

```
server/src/main/resources/db/migration/
├── V1__Create_schema.sql
├── V2__Create_users_table.sql
├── V3__Create_tenants_table.sql
└── ...
```

### 5.2 Flyway 配置（application.yml）

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
```

### 5.3 迁移执行时机

- **开发/测试环境**：应用启动时自动执行（Spring Boot 自动配置）
- **生产环境**：部署前手动确认，应用启动时执行

---

## 六、文件清单

| 路径 | 类型 | 说明 |
|------|------|------|
| `scripts/validate.sh` | 脚本 | 验证入口 |
| `scripts/validate-backend.sh` | 脚本 | 后端验证 |
| `scripts/validate-frontend.sh` | 脚本 | 前端验证 |
| `scripts/build-backend.sh` | 脚本 | 后端构建 |
| `scripts/build-frontend.sh` | 脚本 | 前端构建 |
| `scripts/deploy-dev.sh` | 脚本 | 开发环境部署 |
| `scripts/deploy-prod.sh` | 脚本 | 生产环境部署 |
| `scripts/install-git-hooks.sh` | 脚本 | 安装 Git hooks |
| `docker/backend/Dockerfile` | Dockerfile | 后端镜像 |
| `docker/frontend/Dockerfile.web` | Dockerfile | Web 镜像 |
| `docker/frontend/Dockerfile.admin` | Dockerfile | Admin 镜像 |
| `docker-compose.dev.yml` | Compose | 开发环境 |
| `docker-compose.test.yml` | Compose | 测试环境 |
| `docker-compose.prod.yml` | Compose | 生产环境 |
| `.githooks/pre-push` | Hook | Git pre-push |
| `.env.prod.example` | 配置 | 生产环境变量模板 |
| `.gitignore` | 配置 | 添加 .env.prod |
