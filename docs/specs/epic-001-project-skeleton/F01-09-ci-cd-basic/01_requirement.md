# Feature: CI/CD 基础配置

> 版本：v1.0 | 日期：2026-03-16
> 状态：待审核

---

## 背景

项目需要一套本地/服务器自动化的验证和部署机制，支持单人开发+AI协作的工作模式。

当前情况：
- 无任何 CI/CD 配置
- 一人开发，AI 协作会产生不可控的提交/推送
- 开发/测试/生产环境使用不同的基础设施策略
- cartisan-boot 通过本地 Maven 仓库发布

---

## 目标

- 建立代码质量验证机制，确保推送前代码通过全量测试
- 提供 Docker 化的部署能力
- 支持开发/测试/生产三种环境的部署
- 脚本化、可拆分，便于灵活使用

---

## 范围

### 包含（In Scope）

**验证脚本**
- 全量验证入口脚本
- 后端验证：编译 + 单元测试 + ArchUnit
- 前端验证：类型检查 + Lint + 构建
- Git pre-push hook 自动触发验证

**Docker 配置**
- 后端 Dockerfile（多阶段构建）
- 前端 Dockerfile（web + admin）
- Docker Compose 配置（dev/test/prod）

**部署脚本**
- 开发环境部署脚本
- 生产环境部署脚本
- 构建脚本（后端/前端分离）

**数据库迁移**
- 使用 Flyway（cartisan-boot 已集成）
- Docker volume 持久化数据

### 不包含（Out of Scope）

- GitHub Actions / GitLab CI 等传统 CI/CD 平台配置（留待后续）
- 自动化发布流程
- 灰度发布、蓝绿部署等高级策略
- 监控告警系统

---

## 验收标准（Acceptance Criteria）

- **AC1**：执行 `./scripts/validate.sh` 能完成全量验证，后端测试通过、前端 typecheck 和 lint 通过
- **AC2**：Git pre-push hook 已安装，推送未经验证的代码时被阻止
- **AC3**：执行 `./scripts/deploy-dev.sh` 能在本地启动完整服务（后端 + web + admin + PostgreSQL + Redis）
- **AC4**：服务启动后，后端健康检查 `/actuator/health` 返回 UP
- **AC5**：前端能成功调用后端健康检查接口并展示结果
- **AC6**：Docker volume 数据持久化，容器重启后数据不丢失
- **AC7**：Flyway 迁移脚本在数据库首次初始化时自动执行
- **AC8**：验证过程总耗时 < 5 分钟（本地执行）

---

## 约束

### 技术约束

- 后端：Gradle + Java 21 + Spring Boot
- 前端：pnpm monorepo + Next.js 15
- 容器：Docker + Docker Compose
- 数据库迁移：Flyway（cartisan-boot 已集成）
- cartisan-boot：发布到本地 Maven 仓库（~/.m2/repository）

### 环境约束

- 开发环境：PostgreSQL 本地或 Docker，Redis Docker
- 测试环境：PostgreSQL Docker，Redis Docker
- 生产环境：PostgreSQL 云服务，Redis Docker（可能）

### 流程约束

- 单人开发 + AI 协作，需要 Git hook 防止 AI 未经验证的推送
- 脚本需可拆分执行，方便灵活使用
- "慢就是快"——优先保证质量而非速度

---

## 依赖

- **F01-01**：后端项目骨架（Gradle 构建配置）
- **F01-04**：前端 Monorepo（pnpm workspace）
- **F01-08**：前后端联调验证（验证脚本可复用健康检查逻辑）
