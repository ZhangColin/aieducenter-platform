# F02-06 密码重置 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现忘记密码功能：用户通过邮箱/手机号验证码重置密码，重置后旧 Token 立即失效。

**Architecture:** 新增 `AccountPasswordResetAppService` 处理重置流程，复用 `VerificationCodeAppService` 校验验证码，通过新增 `SessionManagementPort` 接口调用 Sa-Token kickout。`account` 字段含 `@` 时走邮箱验证码，否则走短信验证码。

**Tech Stack:** Java 21, Spring Boot, JPA, Sa-Token, Mockito, MockMvc

---

## 文件清单

| 操作 | 文件 |
|------|------|
| 修改 | `server/src/main/java/com/aieducenter/account/domain/aggregate/User.java` |
| 新增 | `server/src/main/java/com/aieducenter/account/domain/port/SessionManagementPort.java` |
| 新增 | `server/src/main/java/com/aieducenter/account/infrastructure/SaTokenSessionManagementAdapter.java` |
| 新增 | `server/src/main/java/com/aieducenter/account/application/dto/ResetPasswordCommand.java` |
| 新增 | `server/src/main/java/com/aieducenter/account/application/AccountPasswordResetAppService.java` |
| 修改 | `server/src/main/java/com/aieducenter/account/web/AccountController.java` |
| 修改 | `server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java` |
| 新增 | `server/src/test/java/com/aieducenter/account/application/AccountPasswordResetAppServiceTest.java` |
| 新增 | `server/src/test/java/com/aieducenter/account/web/AccountPasswordResetIntegrationTest.java` |

> `application.yml` 中 `/api/account/reset-password` 已在 `exclude-path-patterns` 中，无需修改。

---

## Task 1：User.resetPassword() 领域方法

**Files:**
- Modify: `User.java` — 新增 `resetPassword(String plainPassword)` 方法
- Modify: `UserTest.java` — 新增两个测试用例

**说明：** `resetPassword` 不需要旧密码，直接调用已有的 `validatePasswordStrength()` + BCrypt 加密 + 更新 `this.password`。参考同文件中 `updatePassword` 方法的实现结构，但去掉旧密码校验。

- [ ] 在 `UserTest.java` 补充测试：`given_valid_new_password_when_reset_password_then_password_updated` 和 `given_weak_password_when_reset_password_then_throw_password_weak`
- [ ] 运行测试，确认 FAIL（方法不存在）
  ```
  cd server && ./gradlew test --tests "*.UserTest"
  ```
- [ ] 在 `User.java` 实现 `resetPassword(String plainPassword)` 方法
- [ ] 运行测试，确认 PASS
- [ ] Commit：`feat(account): add User.resetPassword domain method`

---

## Task 2：SessionManagementPort + SaTokenSessionManagementAdapter

**Files:**
- Create: `SessionManagementPort.java` — `@Port(PortType.CLIENT)` 接口，声明 `void kickout(Long userId)`
- Create: `SaTokenSessionManagementAdapter.java` — `@Service` 实现，调用 `StpUtil.kickout(userId)`

**说明：** 参考同包下 `MessageSender`（`@Port(PortType.CLIENT)`）和 `LogMessageSenderAdapter`（`@Service` 实现）的结构。不写单元测试（trivial 适配器，集成测试覆盖）。

- [ ] 创建 `SessionManagementPort.java` 接口
- [ ] 创建 `SaTokenSessionManagementAdapter.java` 实现类
- [ ] 编译验证：`cd server && ./gradlew compileJava`
- [ ] Commit：`feat(account): add SessionManagementPort and SaToken adapter`

---

## Task 3：ResetPasswordCommand DTO

**Files:**
- Create: `ResetPasswordCommand.java` — Java Record，字段：`account`、`verificationCode`、`newPassword`，全部 `@NotBlank`

**说明：** 参考 `LoginByPasswordCommand` 的 Record + `@NotBlank` 风格。

- [ ] 创建 `ResetPasswordCommand.java`
- [ ] 编译验证：`cd server && ./gradlew compileJava`
- [ ] Commit：`feat(account): add ResetPasswordCommand DTO`

---

## Task 4：AccountPasswordResetAppService + 单元测试

**Files:**
- Create: `AccountPasswordResetAppService.java` — `@Service`，`@Transactional`，依赖注入：`UserRepository`、`VerificationCodeAppService`、`SessionManagementPort`
- Create: `AccountPasswordResetAppServiceTest.java` — `@ExtendWith(MockitoExtension.class)`，mock 上述三个依赖

**重置流程（参考设计文档）：**
1. 根据 `account.contains("@")` 判断类型，调用 `verifyCode` 或 `verifyPhoneCode`（purpose = `"RESET_PASSWORD"`）
2. `findByEmail` 或 `findByPhoneNumber`，找不到抛 `VerificationCodeError.CODE_INVALID`
3. `user.resetPassword(newPassword)`
4. `userRepository.save(user)`
5. `try { sessionManagementPort.kickout(userId) } catch (Exception e) { log.warn(...) }`

**参考：** `AccountLoginAppService`（流程结构）、`AccountLoginAppServiceTest`（mock 模式和断言风格）。

**测试场景：**
- 邮箱账号重置成功（verifyCode 被调用、findByEmail、kickout 被调用）
- 手机号账号重置成功（verifyPhoneCode 被调用、findByPhoneNumber、kickout 被调用）
- 验证码错误时透传 VerificationCodeError（使用 `doThrow`）
- 账号不存在时抛 `VerificationCodeError.CODE_INVALID`
- kickout 失败时不抛异常（用 `doThrow` 让 kickout 抛 RuntimeException，验证整个方法正常返回）

- [ ] 创建 `AccountPasswordResetAppServiceTest.java`，写好全部测试用例
- [ ] 运行测试，确认 FAIL（服务类不存在）
  ```
  cd server && ./gradlew test --tests "*.AccountPasswordResetAppServiceTest"
  ```
- [ ] 创建 `AccountPasswordResetAppService.java` 实现
- [ ] 运行测试，确认 PASS
- [ ] Commit：`feat(account): implement AccountPasswordResetAppService`

---

## Task 5：Controller 端点 + 集成测试

**Files:**
- Modify: `AccountController.java` — 修改构造函数增加 `AccountPasswordResetAppService` 参数（构造函数注入，禁止 `@Autowired`），新增 `POST /reset-password` 端点，返回 `ApiResponse<Void>`
- Create: `AccountPasswordResetIntegrationTest.java` — 参考 `AccountLoginIntegrationTest` 的结构（`@SpringBootTest`、`@AutoConfigureMockMvc`、`@MockBean VerificationCodeAppService`、`@BeforeAll SaTokenTestConfig.initSaTokenContext()`、`@Transactional`）

**集成测试场景：**

1. **邮箱重置成功 + 旧 Token 失效 + 新密码可登录**
   - registerUser 带 email → login 得到 token → mock `verifyCode` 返回成功 → POST reset-password → 用旧 token 访问 `GET /test/auth/require-auth` 期望 401（注：设计文档用 `/api/account/profile`，该接口尚未实现，改用 `TestAuthController` 提供的 `/test/auth/require-auth`）→ 用新密码 login 期望 200

2. **手机号重置成功**
   - registerUser 带 phone → mock `verifyPhoneCode` → POST reset-password → 新密码 login 成功

3. **验证码错误返回 400**
   - mock `verifyCode` 抛 `VerificationCodeError.CODE_INVALID` → POST reset-password → 期望 400

4. **账号不存在返回 400**
   - mock `verifyCode` 返回成功 → POST reset-password 用不存在的 account → 期望 400

- [ ] 创建 `AccountPasswordResetIntegrationTest.java`，写好全部测试
- [ ] 运行，确认 FAIL（端点不存在）
  ```
  cd server && ./gradlew test --tests "*.AccountPasswordResetIntegrationTest"
  ```
- [ ] 在 `AccountController.java` 注入 `AccountPasswordResetAppService`，添加 `POST /reset-password` 端点
- [ ] 运行，确认 PASS
- [ ] Commit：`feat(account): add POST /api/account/reset-password endpoint`

---

## 最终验证

- [ ] 运行全量测试，确认无回归：`cd server && ./gradlew test`
