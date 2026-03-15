# Feature: 后端项目骨架搭建

> Epic: Epic 1 - 项目骨架
> Feature: F01-01
> 版本：v1.0 | 日期：2026-03-15

---

## 背景

搭建 aieducenter-platform 后端 Spring Boot 项目基础骨架，建立 8 个限界上下文的标准包结构，使项目可编译运行，为后续 Feature 提供技术底座。

这是后端开发的第一个 Feature，完成后开发团队应具备：
- 可编译运行的 Spring Boot 项目
- 与 cartisan-boot 框架的联调能力
- 8 个限界上下文的标准目录结构
- 基础的健康检查验证能力

## 目标

- 创建 Spring Boot 项目，配置 Composite Build 引用本地 cartisan-boot
- 建立 8 个 Context 的标准四层目录结构（domain/application/controller/infrastructure）
- 应用可成功启动，健康检查接口可访问
- 为 F01-02 Actuator 和后续开发做好准备

## 范围

### 包含（In Scope）

- 创建 `server/` 目录及完整的 Gradle 项目结构
- 配置 `settings.gradle.kts` 和 `build.gradle.kts`，通过 Composite Build 引用 `cartisan-boot`
- 创建 8 个限界上下文的空目录：account/tenant/gateway/conversation/billing/agent/creative/admin
- 每个包含 4 层子目录：domain/application/controller/infrastructure
- 创建 Spring Boot 主启动类 `AieduCenterApplication`
- 创建健康检查 Controller，返回基本状态信息
- 配置 `application.yml` 和 `logback-spring.xml`
- Gradle Wrapper 配置，支持 `./gradlew` 命令

### 不包含（Out of Scope）

- 具体业务逻辑实现
- Actuator 健康检查（F01-02）
- OpenAPI 文档配置（F01-03）
- 数据库连接和 JPA 配置
- cartisan-security、cartisan-data-jpa 等高级模块

## 验收标准（Acceptance Criteria）

**AC1：项目结构正确**
- [ ] `server/` 目录存在，包含完整的 Gradle 项目结构
- [ ] `settings.gradle.kts` 配置了 `includeBuild("../../cartisan-boot")`
- [ ] 8 个 Context 目录存在：account/tenant/gateway/conversation/billing/agent/creative/admin
- [ ] 每个 Context 包含 4 层子目录：domain/application/controller/infrastructure

**AC2：主启动类配置正确**
- [ ] `AieduCenterApplication.java` 位于 `com.aieducenter` 根包
- [ ] 使用 `@SpringBootApplication` 注解
- [ ] 可正常启动，日志无错误

**AC3：健康检查接口可访问**
- [ ] `HealthController` 位于 `com.aieducenter.controller`
- [ ] GET `/api/health` 返回 200
- [ ] 响应体包含 `status: "ok"` 和 `timestamp`

**AC4：项目可编译运行**
- [ ] `./gradlew build` 编译成功
- [ ] `./gradlew bootRun` 启动成功
- [ ] 启动日志显示 "Started AieduCenterApplication"

## 约束

### 技术约束

- Java 21
- Spring Boot 3.4
- Gradle 8.x（Kotlin DSL）
- cartisan-boot 通过 Composite Build 引用（非 Maven Local）

### 架构约束

- 遵循 DDD 六边形架构
- 根包为 `com.aieducenter`
- 应用级接口（如健康检查）放在 `com.aieducenter.controller`，不属于任何 Context
- 各 Context 的 controller 放在对应 Context 下（如 `com.aieducenter.account.controller`）

### 性能约束

- 启动时间 < 30 秒（本地开发环境）
- 健康检查接口响应时间 < 100ms

## 依赖关系

| 依赖项 | 类型 | 说明 |
|--------|------|------|
| cartisan-boot | 外部 | 框架已存在于 `../../cartisan-boot/` |
| F01-02 | 后续 | Actuator 健康检查依赖本 Feature |
| F01-03 | 后续 | OpenAPI 配置依赖本 Feature |
