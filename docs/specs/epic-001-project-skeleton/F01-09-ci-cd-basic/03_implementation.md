# Feature: CI/CD 基础配置 — 实施计划

> 版本：v1.0 | 日期：2026-03-16
> 状态：待审核

---

## 目标复述

建立本地/服务器自动化验证和部署机制，支持单人开发+AI协作工作模式。包括：
- 代码质量验证脚本（后端+前端）
- Git pre-push hook 自动触发验证
- Docker 化部署能力（开发/测试/生产三环境）
- Flyway 数据库迁移集成

---

## 变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `scripts/validate.sh` | 验证入口脚本 |
| 新建 | `scripts/validate-backend.sh` | 后端验证脚本 |
| 新建 | `scripts/validate-frontend.sh` | 前端验证脚本 |
| 新建 | `scripts/build-backend.sh` | 后端构建脚本 |
| 新建 | `scripts/build-frontend.sh` | 前端构建脚本 |
| 新建 | `scripts/deploy-dev.sh` | 开发环境部署脚本 |
| 新建 | `scripts/deploy-prod.sh` | 生产环境部署脚本 |
| 新建 | `scripts/install-git-hooks.sh` | Git hooks 安装脚本 |
| 新建 | `docker/backend/Dockerfile` | 后端 Docker 镜像 |
| 新建 | `docker/backend/entrypoint.sh` | 后端启动脚本 |
| 新建 | `docker/frontend/Dockerfile.web` | Web 前端镜像 |
| 新建 | `docker/frontend/Dockerfile.admin` | Admin 前端镜像 |
| 新建 | `docker-compose.dev.yml` | 开发环境 Compose |
| 新建 | `docker-compose.test.yml` | 测试环境 Compose |
| 新建 | `docker-compose.prod.yml` | 生产环境 Compose |
| 新建 | `.githooks/pre-push` | Git pre-push hook |
| 新建 | `.env.prod.example` | 生产环境变量模板 |
| 修改 | `.gitignore` | 添加 .env.prod 忽略 |
| 修改 | `server/build.gradle.kts` | 添加 Flyway 依赖（如需） |
| 新建 | `server/src/main/resources/db/migration/` | Flyway 迁移脚本目录 |

---

## 原子任务清单

### Step 1: 创建目录结构

- 创建 `scripts/` 目录
- 创建 `docker/backend/` 目录
- 创建 `docker/frontend/` 目录
- 创建 `.githooks/` 目录
- 创建 `server/src/main/resources/db/migration/` 目录

**验证**：目录全部创建成功

---

### Step 2: 编写验证脚本

#### Step 2.1: scripts/validate.sh

- 内容：全量验证入口
- 行为：调用 validate-backend.sh 和 validate-frontend.sh
- 错误处理：任意失败则终止，返回非 0

#### Step 2.2: scripts/validate-backend.sh

- 内容：后端验证
- 步骤：clean → compileJava → test → archUnitTest
- 错误处理：每步失败即终止

#### Step 2.3: scripts/validate-frontend.sh

- 内容：前端验证
- 步骤：typecheck → lint → build
- 错误处理：每步失败即终止

**验证**：
- 脚本有执行权限（`chmod +x`）
- 手动执行 `./scripts/validate.sh` 成功

---

### Step 3: 编写构建脚本

#### Step 3.1: scripts/build-backend.sh

- 内容：后端构建
- 步骤：发布 cartisan-boot → 构建后端 JAR

#### Step 3.2: scripts/build-frontend.sh

- 内容：前端构建
- 步骤：build:web → build:admin

**验证**：
- 手动执行 `./scripts/build-backend.sh` 生成 JAR
- 手动执行 `./scripts/build-frontend.sh` 生成构建产物

---

### Step 4: 编写部署脚本

#### Step 4.1: scripts/deploy-dev.sh

- 内容：开发环境部署
- 步骤：验证 → 构建后端 → 构建前端 → docker-compose down → docker-compose up -d --build
- 参数：接受 `$1` 作为环境标识（默认 dev）

#### Step 4.2: scripts/deploy-prod.sh

- 内容：生产环境部署
- 步骤：加载 .env.prod → 用户确认 → 验证 → 构建 → 部署
- 交互：要求输入 "yes" 确认

**验证**：
- 手动执行 `./scripts/deploy-dev.sh` 成功启动服务
- 服务健康检查通过

---

### Step 5: 编写 Git Hook

#### Step 5.1: .githooks/pre-push

- 内容：调用 validate.sh
- 成功：返回 0，允许推送
- 失败：返回 1，阻止推送

#### Step 5.2: scripts/install-git-hooks.sh

- 内容：设置 `git config core.hooksPath .githooks`

**验证**：
- 执行 `./scripts/install-git-hooks.sh` 成功
- 验证 `git config core.hooksPath` 输出 `.githooks`
- 尝试推送未验证代码被阻止

---

### Step 6: 编写 Dockerfile

#### Step 6.1: docker/backend/Dockerfile

- 构建阶段：eclipse-temurin:21-jdk，发布 cartisan-boot，构建后端
- 运行阶段：eclipse-temurin:21-jre-alpine，暴露 8080
- 健康检查：/actuator/health

#### Step 6.2: docker/backend/entrypoint.sh

- 内容：等待数据库就绪再启动应用

#### Step 6.3: docker/frontend/Dockerfile.web

- 构建阶段：node:20-alpine，pnpm build
- 运行阶段：node standalone 或 nginx

#### Step 6.4: docker/frontend/Dockerfile.admin

- 同 web，端口改为 3001

**验证**：
- `docker build -f docker/backend/Dockerfile -t aiedu-backend .` 成功
- `docker build -f docker/frontend/Dockerfile.web -t aiedu-web .` 成功

---

### Step 7: 编写 Docker Compose

#### Step 7.1: docker-compose.dev.yml

- 服务：postgres, redis, backend, web, admin
- Volume：postgres-data, redis-data
- 网络：默认网络
- 依赖：backend 依赖 postgres 健康检查

#### Step 7.2: docker-compose.test.yml

- 类似 dev.yml，配置使用测试数据库

#### Step 7.3: docker-compose.prod.yml

- 服务：backend, web, admin（不含数据库）
- 环境变量：从 .env.prod 读取

**验证**：
- `docker-compose -f docker-compose.dev.yml up -d` 成功启动
- 所有容器状态为 healthy
- 数据持久化验证：重启后数据不丢失

---

### Step 8: Flyway 配置

#### Step 8.1: 检查 cartisan-boot Flyway 依赖

- 确认 cartisan-data-jpa 是否包含 Flyway
- 如需补充，在 server/build.gradle.kts 添加

#### Step 8.2: 创建迁移脚本目录

- `server/src/main/resources/db/migration/`

#### Step 8.3: 配置 application.yml

- 添加 spring.flyway 配置

**验证**：
- 首次启动应用，Flyway 自动创建 schema_migrations 表

---

### Step 9: 环境变量配置

#### Step 9.1: 创建 .env.prod.example

- 内容：生产环境变量模板

#### Step 9.2: 更新 .gitignore

- 添加 `.env.prod`

**验证**：
- `.env.prod` 不被 git 追踪

---

### Step 10: 集成测试

#### Step 10.1: 端到端验证

1. 执行 `./scripts/install-git-hooks.sh`
2. 执行 `./scripts/deploy-dev.sh`
3. 访问 http://localhost:8080/actuator/health
4. 访问 http://localhost:3000
5. 访问 http://localhost:3001
6. 验证前端能调用后端健康检查

#### Step 10.2: Git Hook 验证

1. 修改代码引入错误
2. 尝试 `git push`，验证被阻止
3. 修复错误，验证推送成功

**验证**：所有验收标准（AC1-AC8）满足

---

## 执行顺序

```
Step 1 (目录) → Step 2-3 (脚本) → Step 4 (部署) → Step 5 (Hook)
     ↓
Step 6-7 (Docker) → Step 8 (Flyway) → Step 9 (环境变量) → Step 10 (集成测试)
```

**并行机会**：
- Step 2 和 Step 3 可并行
- Step 6.1 和 Step 6.3/6.4 可并行

---

## 风险与缓解

| 风险 | 缓解措施 |
|------|---------|
| cartisan-boot 本地依赖路径 | 文档说明，Dockerfile 先发布再构建 |
| Docker volume 权限问题 | 文档说明，使用固定 UID |
| Flyway 迁移冲突 | 文档规范，版本号递增 |
| Git hook 不生效 | 文档说明安装步骤，提供验证命令 |

---

## 验收方式

### 手动检查清单

- [ ] 所有脚本可执行（`ls -l scripts/*.sh`）
- [ ] `./scripts/validate.sh` 执行成功
- [ ] Git pre-push hook 已安装并生效
- [ ] `docker-compose -f docker-compose.dev.yml up -d` 成功启动
- [ ] 后端健康检查返回 UP
- [ ] 前端能调用后端接口
- [ ] 容器重启后数据不丢失
- [ ] Flyway 迁移表已创建

### 无测试用例说明

本 Feature 为纯配置/脚本类 Feature，无业务代码。验收方式为手动执行脚本和验证 Docker 服务运行状态。
