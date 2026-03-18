# Feature: F02-09 认证基础设施

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 1 - Research

---

## 背景

F02-09 是用户登录认证的基础设施，为后续 Feature（F02-05 登录、F02-07 个人信息、F02-10 前端登录页）提供认证能力支撑。

本 Feature 基于 cartisan-boot 的 cartisan-security 模块，通过配置方式启用 Sa-Token 认证，提供 `@RequireAuth`、`@CurrentUser` 等注解，实现接口鉴权和用户上下文注入。

---

## 目标

1. 引入 cartisan-security 依赖，零配置启用认证拦截器
2. 配置公开路径（注册、登录）和需认证路径（API）
3. 提供测试专用 Controller，验证配置生效
4. 确保无效 Token 返回 401，未授权访问返回 403

---

## 范围

### 包含（In Scope）

| 内容 | 说明 |
|------|------|
| **依赖引入** | cartisan-security、sa-token-spring-boot3-starter |
| **配置** | Sa-Token 参数（Token 有效期 7 天）、拦截路径配置 |
| **测试 Controller** | `/test/auth/*` 专用端点，验证 @RequireAuth、@CurrentUser |
| **单元测试** | 配置加载、拦截器、异常处理、@CurrentUser 注入 |

### 不包含（Out of Scope）

| 内容 | 原因 | 归属 Feature |
|------|------|-------------|
| 登录业务逻辑（密码验证） | 属于应用层，本 Feature 仅做基建 | F02-05 |
| 注册接口 | 不需要认证 | F02-03, F02-04 |
| 个人信息接口 | 需要认证，但属于业务层 | F02-07 |
| JWT 实现 | cartisan-security 使用 Sa-Token | - |

---

## 验收标准（Acceptance Criteria）

### AC1: 依赖引入成功
- [ ] `build.gradle.kts` 添加 cartisan-security 和 sa-token 依赖
- [ ] `./gradlew compileJava` 编译通过

### AC2: 配置生效
- [ ] Token 有效期配置为 7 天（604800 秒）
- [ ] 公开路径包括：/api/account/login、/api/account/register/**、/api/account/verification-code/**
- [ ] 拦截路径包括：/api/**、/admin/**

### AC3: @RequireAuth 注解生效
- [ ] 访问带 @RequireAuth 的接口，未登录返回 401
- [ ] 登录后访问，返回 200

### AC4: @CurrentUser 注解生效
- [ ] `@CurrentUser Long userId` 注入成功，返回当前用户 ID
- [ ] 未登录时访问返回 401
- [ ] `@CurrentUser Optional<Long> userId` 未登录时返回 Optional.empty()

### AC5: 异常处理
- [ ] 无效 Token 返回 401，响应体符合 ApiResponse 格式
- [ ] 登出成功后 Token 失效

### AC6: 单元测试覆盖
- [ ] `AuthenticationConfigTest` — 验证配置加载
- [ ] `TestAuthControllerTest` — MockMvc 集成测试

---

## 约束

### 技术约束
- 基于 cartisan-security 模块，不自行实现 JWT 或认证逻辑
- 使用 Sa-Token 作为底层认证框架
- TestAuthController 放在 `src/test/java` 下，不暴露到生产环境

### 安全约束
- Token 有效期固定 7 天，不允许配置
- 允许并发登录（同一用户多设备）
- 不共享 Session

---

## 依赖关系

| 依赖 Feature | 依赖类型 | 说明 |
|-------------|---------|------|
| F02-01 User 聚合根 | 强依赖 | 需要用户 ID 作为 loginId |

---

## 参考资料

- [cartisan-boot 使用手册](../../../guides/cartisan-boot-使用手册.md)
- [Epic 2 Backlog](../00_epic_backlog.md)
