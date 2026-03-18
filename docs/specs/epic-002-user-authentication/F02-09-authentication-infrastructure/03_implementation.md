# Feature: F02-09 认证基础设施 — 实施计划

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 3 - Plan

---

## 目标复述

引入 cartisan-security 依赖，通过配置方式启用 Sa-Token 认证。配置公开路径和需认证路径，提供测试专用 Controller 验证 @RequireAuth、@CurrentUser 注解生效。确保无效 Token 返回 401，单元测试覆盖配置加载和拦截器功能。

---

## 变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| **修改** | `server/build.gradle.kts` | 添加依赖 |
| **修改** | `server/src/main/resources/application.yml` | 添加配置 |
| **新增** | `server/src/test/java/com/aieducenter/account/web/TestAuthController.java` | 测试 Controller |
| **新增** | `server/src/test/java/com/aieducenter/account/config/AuthenticationConfigTest.java` | 配置测试 |

---

## 原子任务清单

### Step 1: 引入依赖
- **文件：** `server/build.gradle.kts`
- **内容：** 添加 cartisan-security 和 sa-token 依赖
- **验证：** `./gradlew compileJava` 编译通过

### Step 2: 添加配置
- **文件：** `server/src/main/resources/application.yml`
- **内容：** cartisan.security 和 sa-token 配置
- **验证：** 配置项无拼写错误

### Step 3: 创建 TestAuthController（红灯）
- **文件：** `server/src/test/java/com/aieducenter/account/web/TestAuthController.java`
- **内容：** 测试专用登录端点，包含 @RequireAuth、@CurrentUser 测试方法
- **验证：** 编译通过

### Step 4: 编写配置测试（红灯）
- **文件：** `server/src/test/java/com/aieducenter/account/config/AuthenticationConfigTest.java`
- **内容：** 验证配置加载、Bean 存在性
- **验证：** 编译通过

### Step 5: 编写集成测试（红灯）
- **文件：** `server/src/test/java/com/aieducenter/account/config/AuthenticationIntegrationTest.java`
- **内容：** MockMvc 测试 @RequireAuth、@CurrentUser、401/403 响应
- **验证：** 编译通过 + 测试红灯（实现不存在）

### Step 6: 创建 SaTokenConfig
- **文件：** `server/src/main/java/com/aieducenter/account/config/SaTokenConfig.java`
- **内容：** @Configuration 类（预留扩展点）
- **验证：** 编译通过

### Step 7: 运行全量测试
- **命令：** `./gradlew test`
- **验证：** 测试全绿 + ArchUnit 通过

---

## 核心流程（伪代码）

### 登录测试流程
```
1. POST /test/auth/login?userId=123
   → Sa-Token 创建会话
   → 返回 TokenInfo

2. GET /test/auth/check
   Header: Authorization: {token}
   → SecurityInterceptor 检查 @RequireAuth
   → Sa-Token 验证 Token
   → 返回 200

3. GET /test/auth/user-id
   Header: Authorization: {token}
   → CurrentUserMethodArgumentResolver 注入 userId
   → 返回当前用户 ID
```

### 错误处理流程
```
1. 无 Token 访问 @RequireAuth 接口
   → NotLoginException
   → SecurityExceptionHandler 捕获
   → 返回 401

2. Token 过期
   → Sa-Token 验证失败
   → NotLoginException
   → 返回 401
```

---

## 依赖关系

- 依赖 F02-01 User 聚合根（获取用户 ID 作为 loginId）

---

## 注意事项

1. TestAuthController 放在 `src/test/java` 下，不打包到生产环境
2. Sa-Token 配置使用 yaml，不创建 Java 配置类（除预留扩展点外）
3. 公开路径需包含注册、登录、验证码接口
4. Token 有效期固定 7 天，不允许前端配置
