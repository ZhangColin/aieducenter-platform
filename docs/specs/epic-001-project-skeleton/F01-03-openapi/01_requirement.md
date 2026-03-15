# Feature: OpenAPI 文档配置

> 版本：v1.0 | 日期：2026-03-16
> Epic：Epic 1 - 项目骨架
> Feature：F01-03

---

## 背景

为后续前端自动生成 TypeScript API 客户端做准备，需要配置后端 OpenAPI 文档。前端将通过 OpenAPI 规范自动生成类型安全的 API 调用代码，减少手工维护接口定义的工作量。

---

## 目标

- 集成 SpringDoc 自动生成 OpenAPI 3.1 规范文档
- 提供 Swagger UI 界面供开发人员查看和测试 API
- 配置 Bearer Token 认证方式（占位，后续 Epic 实现真实认证）

---

## 范围

### 包含（In Scope）

- 引入 `springdoc-openapi-starter-webmvc-ui` 依赖
- 创建 `OpenApiConfig` 配置类，设置 API 元信息
- 配置 Bearer Token 安全 Scheme（占位）
- 验证 Swagger UI 和 OpenAPI JSON 端点可访问

### 不包含（Out of Scope）

- 具体业务 API 的文档注解（@Operation、@Schema 等）
- 多 API 分组配置
- 生产环境部署配置（如禁用 Swagger UI）

---

## 验收标准（Acceptance Criteria）

### AC1: 依赖配置正确
- `build.gradle.kts` 中包含 `springdoc-openapi-starter-webmvc-ui` 依赖
- 项目编译通过

### AC2: OpenAPI 元信息正确
- 访问 `/v3/api-docs` 返回的 JSON 中：
  - `info.title` = "海创元智研云平台 API"
  - `info.version` = "1.0.0"
  - `info.description` = "AI 聚合 SaaS 平台接口"

### AC3: Swagger UI 可访问
- 访问 `http://localhost:8080/swagger-ui.html` 或 `/swagger-ui/index.html`
- 页面正常加载，无 JavaScript 错误
- 显示 API 文档界面

### AC4: Bearer Token 认证配置
- `/v3/api-docs` 返回的 JSON 中：
  - `components.securitySchemes.BearerAuth.type` = "http"
  - `components.securitySchemes.BearerAuth.scheme` = "bearer"
  - `components.securitySchemes.BearerAuth.bearerFormat` = "JWT"

### AC5: 安全要求应用
- `/v3/api-docs` 返回的 JSON 中：
  - `security` 包含 `{"BearerAuth": []}`

---

## 约束

### 技术约束
- 使用 SpringDoc 2.7.0（兼容 Spring Boot 3.4）
- OpenAPI 规范版本 3.1
- 配置类位于 `com.aieducenter.config` 包

### 依赖约束
- 本 Feature 依赖 F01-01（后端项目骨架搭建）
- 与 F01-02（Actuator）无冲突

---

## 设计概述

### 实现方式
- **方案选择**：在 aieducenter-platform 中直接配置（不创建新的 cartisan-boot 模块）
- **配置方式**：Java Config 类（`@Configuration` + `@Bean`）
- **端点路径**：使用 SpringDoc 默认路径（不自定义）

### 核心组件
| 组件 | 文件 | 职责 |
|------|------|------|
| OpenApiConfig | `server/src/main/java/com/aieducenter/config/OpenApiConfig.java` | 提供 OpenAPI Bean，配置元信息和安全 Scheme |

---

## 变更范围

| 操作 | 文件 | 说明 |
|------|------|------|
| 修改 | `server/build.gradle.kts` | 添加 SpringDoc 依赖 |
| 新增 | `server/src/main/java/com/aieducenter/config/OpenApiConfig.java` | OpenAPI 配置类 |

---

## 验证方式

### 手动验证命令

```bash
# 启动应用
./gradlew :server:bootRun

# 验证端点可访问
curl http://localhost:8080/swagger-ui/index.html
curl http://localhost:8080/v3/api-docs

# 验证元信息
curl -s http://localhost:8080/v3/api-docs | jq '.info'
curl -s http://localhost:8080/v3/api-docs | jq '.components.securitySchemes'
```

### 浏览器验证
1. 访问 `http://localhost:8080/swagger-ui.html`
2. 确认页面显示 API 文档界面
3. 确认右上角有 "Authorize" 按钮（Bearer Token 认证）
