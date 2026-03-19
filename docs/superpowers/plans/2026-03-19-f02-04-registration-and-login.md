# F02-04 通用注册与登录 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构注册为通用流程（无验证码），实现密码登录和手机短信验证码登录（SMS mock）

**Architecture:** DDD 六边形架构，Account 上下文负责注册/登录，Verification 上下文提供 SMS 验证码服务。删除 F02-03 的邮箱专属注册实现，替换为通用注册端点。SMS 初期使用 LogMessageSenderAdapter 打印日志模拟发送。

**Tech Stack:** Java 21 / Spring Boot 3.4.x / Sa-Token / Redis / Hutool / AssertJ + Mockito / Testcontainers

**Spec:** `docs/superpowers/specs/2026-03-19-f02-04-registration-and-login-design.md`

---

## 文件清单

### 新增
- `server/src/main/java/com/aieducenter/account/application/dto/RegisterCommand.java`
- `server/src/main/java/com/aieducenter/account/application/dto/LoginByPasswordCommand.java`
- `server/src/main/java/com/aieducenter/account/application/dto/LoginBySmsCommand.java`
- `server/src/main/java/com/aieducenter/account/application/dto/LoginResult.java`
- `server/src/main/java/com/aieducenter/account/application/AccountLoginAppService.java`
- `server/src/main/java/com/aieducenter/verification/application/dto/SendSmsCodeCommand.java`
- `server/src/main/java/com/aieducenter/verification/application/dto/VerifySmsCodeCommand.java`
- `server/src/test/java/com/aieducenter/account/application/AccountLoginAppServiceTest.java`
- `server/src/test/java/com/aieducenter/account/web/AccountLoginIntegrationTest.java`

### 修改
- `server/src/main/resources/application.yml` — 新增公开路径
- `server/src/main/java/com/aieducenter/verification/domain/model/VerificationPurpose.java` — 新增 LOGIN
- `server/src/main/java/com/aieducenter/verification/domain/error/VerificationCodeError.java` — 新增 PHONE_INVALID、RATE_LIMIT_PHONE
- `server/src/main/java/com/aieducenter/account/domain/error/UserError.java` — 新增 ACCOUNT_NOT_FOUND、LOGIN_PASSWORD_INCORRECT
- `server/src/main/java/com/aieducenter/verification/config/VerificationCodeProperties.java` — 新增 phoneCooldownSeconds
- `server/src/main/java/com/aieducenter/verification/domain/repository/VerificationCodeRepository.java` — 新增 tryAcquirePhoneLock
- `server/src/main/java/com/aieducenter/verification/infrastructure/redis/RedisVerificationCodeRepository.java` — 修复 type 硬编码 + 新增 tryAcquirePhoneLock
- `server/src/main/java/com/aieducenter/verification/application/VerificationCodeAppService.java` — 新增 sendSmsVerificationCode + verifyPhoneCode
- `server/src/main/java/com/aieducenter/verification/web/VerificationCodeController.java` — 新增 /verification-code/sms
- `server/src/main/java/com/aieducenter/account/domain/aggregate/User.java` — 替换 registerByEmail → register
- `server/src/main/java/com/aieducenter/account/application/AccountRegistrationAppService.java` — 重写为通用注册
- `server/src/main/java/com/aieducenter/account/web/AccountController.java` — 替换 /register/email → /register，新增 /login、/login/sms
- `server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java` — 替换 registerByEmail 相关用例
- `server/src/test/java/com/aieducenter/account/application/AccountRegistrationAppServiceTest.java` — 重写
- `server/src/test/java/com/aieducenter/account/web/AccountRegistrationIntegrationTest.java` — 重写
- `server/src/test/java/com/aieducenter/verification/application/VerificationCodeAppServiceTest.java` — 补充 SMS 场景

### 删除
- `server/src/main/java/com/aieducenter/account/application/dto/RegisterByEmailCommand.java`

---

## Task 1：枚举准备

**Files:** VerificationPurpose.java、VerificationCodeError.java、UserError.java、application.yml

- [ ] 在 `VerificationPurpose` 中新增 `LOGIN`
- [ ] 在 `VerificationCodeError` 中新增 `PHONE_INVALID`（400）、`RATE_LIMIT_PHONE`（429），参照已有枚举格式
- [ ] 在 `UserError` 中新增 `ACCOUNT_NOT_FOUND`（401, USER_011）、`LOGIN_PASSWORD_INCORRECT`（401, USER_012），参照已有格式
- [ ] 在 `application.yml` 的 `exclude-path-patterns` 中补充 `/api/account/login/sms`（`/api/account/login` 和 `/api/account/verification-code/**` 已存在）
- [ ] 编译验证：`cd server && ./gradlew compileJava`
- [ ] Commit：`feat(account): add LOGIN purpose, phone verification errors, account login errors`

---

## Task 2：修复 Redis type 字段硬编码

**Files:** RedisVerificationCodeRepository.java

- [ ] 在 `save()` 中将 `type` 字段写入 Redis hash（`code.getType().name()`）
- [ ] 在 `findById()` 中从 hash 读取 `type` 字段，替换硬编码的 `VerificationType.EMAIL`
- [ ] 运行已有 Redis 仓储测试：`cd server && ./gradlew test --tests "*.RedisVerificationCodeRepositoryTest"`（需本地 Redis）
- [ ] Commit：`fix(verification): store and restore VerificationType in Redis hash`

---

## Task 3：Phone 限流基础设施

**Files:** VerificationCodeProperties.java、VerificationCodeRepository.java、RedisVerificationCodeRepository.java

- [ ] 在 `VerificationCodeProperties` 中新增 `phoneCooldownSeconds`（默认 60），参照 `emailCooldownSeconds` 格式
- [ ] 在 `VerificationCodeRepository` 接口新增 `tryAcquirePhoneLock(String phone, String purpose)`
- [ ] 在 `RedisVerificationCodeRepository` 实现 `tryAcquirePhoneLock`：Redis key 使用 `limit:phone:{phone}:{purpose}`，TTL 60秒，参照 `tryAcquireEmailLock` 逻辑
- [ ] 编译验证：`cd server && ./gradlew compileJava`
- [ ] Commit：`feat(verification): add phone rate limiting infrastructure`

---

## Task 4：SMS 验证码服务

**Files:** SendSmsCodeCommand.java（新增）、VerifySmsCodeCommand.java（新增）、VerificationCodeAppService.java、VerificationCodeAppServiceTest.java

- [ ] 创建 `SendSmsCodeCommand` record（phone、purpose），参照 `SendEmailCodeCommand`
- [ ] 创建 `VerifySmsCodeCommand` record（phone、code、purpose）
- [ ] 在 `VerificationCodeAppServiceTest` 中先写以下失败测试（参照已有邮箱测试结构）：
  - `given_valid_phone_when_send_sms_code_then_success`
  - `given_invalid_phone_when_send_sms_code_then_throw_phone_invalid`
  - `given_phone_rate_limited_when_send_sms_code_then_throw_rate_limit_phone`
  - `given_valid_sms_code_when_verify_phone_code_then_success`
  - `given_wrong_code_when_verify_phone_code_then_throw_code_invalid`
- [ ] 运行测试，确认全部失败：`cd server && ./gradlew test --tests "*.VerificationCodeAppServiceTest"`
- [ ] 在 `VerificationCodeAppService` 实现 `sendSmsVerificationCode(SendSmsCodeCommand, String ip)`：校验手机号（hutool Validator.isMobile）→ validatePurpose → tryAcquirePhoneLock → checkAndIncrementIp → 生成码 → create(SMS) → save → messageSender.send → 返回 SendCodeResponse（使用 phoneCooldownSeconds）
- [ ] 实现 `verifyPhoneCode(VerifySmsCodeCommand)`：校验手机号格式 → validatePurpose → verifyAndMarkAsUsed → 错误细化，参照 `verifyCode` 逻辑但走 phone 路径
- [ ] 运行测试，确认全部通过
- [ ] Commit：`feat(verification): add SMS verification code service`

---

## Task 5：SMS 验证码端点

**Files:** VerificationCodeController.java

- [ ] 在 `VerificationCodeController` 新增 `POST /verification-code/sms` 端点，参照已有 `/verification-code/email` 的结构（含 IP 提取）
- [ ] 编译验证：`cd server && ./gradlew compileJava`
- [ ] Commit：`feat(verification): add SMS verification code endpoint`

---

## Task 6：User.register() 工厂方法

**Files:** User.java、UserTest.java

- [ ] 在 `UserTest` 中先写以下失败测试：
  - `given_username_and_password_only_when_register_then_success`（无 email/phone）
  - `given_email_provided_when_register_then_email_set`
  - `given_phone_provided_when_register_then_phone_set`
  - `given_invalid_email_when_register_then_throw_email_invalid`
  - `given_invalid_phone_when_register_then_throw_phone_number_invalid`
  - 删除原 `registerByEmail` 相关测试用例
- [ ] 运行测试，确认新测试失败：`cd server && ./gradlew test --tests "*.UserTest"`
- [ ] 在 `User` 中新增 `register(username, plainPassword, nickname, email, phone)` 工厂方法，删除 `registerByEmail`
- [ ] 运行测试，确认全部通过
- [ ] Commit：`feat(account): replace registerByEmail with generic register factory method`

---

## Task 7：注册 AppService 重构

**Files:** RegisterCommand.java（新增）、AccountRegistrationAppService.java、AccountRegistrationAppServiceTest.java、RegisterByEmailCommand.java（删除）

- [ ] 创建 `RegisterCommand` record（username、password、nickname、email、phone），参照 spec 中的校验注解
- [ ] 在 `AccountRegistrationAppServiceTest` 中先写以下失败测试（mock：UserRepository、ApplicationEventPublisher、AuthenticationService，**不再需要 VerificationCodeAppService**）：
  - `given_valid_command_when_register_then_return_token_and_publish_event`（仅 username+password）
  - `given_duplicate_username_when_register_then_throw_409`
  - `given_duplicate_email_when_register_then_throw_409`
  - `given_duplicate_phone_when_register_then_throw_409`
- [ ] 运行测试，确认失败
- [ ] 重写 `AccountRegistrationAppService.register(RegisterCommand)` 实现：按 spec 中的注册流程
- [ ] 删除 `registerByEmail()` 方法和 `RegisterByEmailCommand.java` 文件
- [ ] 运行测试，确认全部通过：`cd server && ./gradlew test --tests "*.AccountRegistrationAppServiceTest"`
- [ ] Commit：`feat(account): replace email registration with generic registration`

---

## Task 8：AccountLoginAppService

**Files:** LoginByPasswordCommand.java（新增）、LoginBySmsCommand.java（新增）、LoginResult.java（新增）、AccountLoginAppService.java（新增）、AccountLoginAppServiceTest.java（新增）

- [ ] 创建三个 DTO record（参照 spec 接口设计中的字段和校验注解）
- [ ] 先写 `AccountLoginAppServiceTest`（mock：UserRepository、VerificationCodeAppService、AuthenticationService）：
  - `given_username_when_login_by_password_then_return_token`
  - `given_email_when_login_by_password_then_return_token`
  - `given_phone_when_login_by_password_then_return_token`
  - `given_unknown_account_when_login_by_password_then_throw_account_not_found`
  - `given_wrong_password_when_login_by_password_then_throw_login_password_incorrect`
  - `given_valid_sms_code_when_login_by_sms_then_return_token`
  - `given_unregistered_phone_when_login_by_sms_then_throw_account_not_found`
  - `given_invalid_sms_code_when_login_by_sms_then_propagate_verification_error`
- [ ] 运行测试，确认全部失败：`cd server && ./gradlew test --tests "*.AccountLoginAppServiceTest"`
- [ ] 实现 `AccountLoginAppService`（无 @Transactional）：按 spec 中的密码登录流程和短信登录流程
- [ ] 运行测试，确认全部通过
- [ ] Commit：`feat(account): add login app service with password and SMS login`

---

## Task 9：AccountController 端点更新

**Files:** AccountController.java

- [ ] 将 `POST /register/email` 替换为 `POST /register`（改用 `RegisterCommand`，调 `register()`）
- [ ] 注入 `AccountLoginAppService`，新增 `POST /login`（调 `loginByPassword()`）和 `POST /login/sms`（调 `loginBySms()`）
- [ ] 编译验证：`cd server && ./gradlew compileJava`
- [ ] Commit：`feat(account): update account controller with register and login endpoints`

---

## Task 10：集成测试

**Files:** AccountRegistrationIntegrationTest.java（重写）、AccountLoginIntegrationTest.java（新增）

- [ ] 重写 `AccountRegistrationIntegrationTest`：
  - **保留** `@MockBean VerificationCodeAppService`（虽然注册不再调用它，但 mock 可阻止 Redis bean 实例化，避免集成测试依赖 Redis）
  - 场景：通用注册成功（DB 有 user + tenant，token 有效）、用户名重复 409、弱密码 400
- [ ] 新增 `AccountLoginIntegrationTest`：
  - `@MockBean VerificationCodeAppService`（短信登录用）
  - 场景：用户名密码登录成功、邮箱密码登录成功、手机号密码登录成功、账号不存在 401
  - 场景：短信登录成功（mock verifyPhoneCode）、手机号未注册 401
- [ ] 运行全量测试：`cd server && ./gradlew test`
- [ ] 确认全部通过（GREEN）
- [ ] Commit：`test(account): rewrite registration integration test and add login integration test`

---

## Task 11：收尾验证

- [ ] 运行全量检查（含 ArchUnit）：`cd server && ./gradlew check`
- [ ] 确认 BUILD SUCCESSFUL
- [ ] Commit（如有补丁）：`fix: address issues found in full check`
