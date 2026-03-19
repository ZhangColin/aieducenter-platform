# F02-06 密码重置 设计文档

> 版本：v1.2 | 日期：2026-03-19
> 状态：已批准
> Epic：Epic 2 - 用户与登录

---

## 背景

实现忘记密码功能：用户通过邮箱或手机号获取验证码，验证通过后重置密码。重置成功后旧 Token 立即失效，需重新登录。

**前置依赖：** F02-01（User 聚合根）、F02-02（VerificationCodeAppService）、F02-09（Sa-Token 认证基础设施）

---

## 接口设计

### 发送重置验证码（复用现有接口）

客户端根据账号类型调用已有接口，purpose 传 `RESET_PASSWORD`：

- 邮箱：`POST /api/account/verification-code/email`，body: `{ "email": "...", "purpose": "RESET_PASSWORD" }`
- 手机：`POST /api/account/verification-code/sms`，body: `{ "phone": "...", "purpose": "RESET_PASSWORD" }`

`VerificationPurpose.RESET_PASSWORD` 已存在，无需新增。

---

### POST /api/account/reset-password

**鉴权：** 否（公开路径）

**请求字段：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | String | 是 | 邮箱或手机号；含 `@` 视为邮箱，否则视为手机号 |
| verificationCode | String | 是 | 6 位验证码 |
| newPassword | String | 是 | 新密码，8-20 位，必须同时包含字母和数字 |

**成功响应（200）：**
```json
{ "code": 200, "message": "success", "data": null }
```

**错误响应：**

| 场景 | HTTP Status | 错误码 |
|------|-------------|--------|
| 必填字段为空 | 400 | Bean Validation |
| 验证码错误/过期/已使用 | 400 | VerificationCodeError（透传） |
| 账号不存在 | 400 | VerificationCodeError.CODE_INVALID |
| 新密码强度不足 | 400 | UserError.PASSWORD_WEAK |

> **安全说明：** 账号不存在时统一返回 400 + CODE_INVALID（与验证码错误相同的错误码），防止攻击者通过错误码枚举注册用户。合法用户在调用发送验证码接口时已经确认了账号可接收验证码，不需要在此接口再区分账号是否存在。

---

## 架构设计

### 重置密码流程（AccountPasswordResetAppService.resetPassword，@Transactional）

```
1. 根据 account 是否含 '@' 判断账号类型：
   - 邮箱：verificationCodeAppService.verifyCode(
               new VerifyCodeCommand(account, verificationCode, "RESET_PASSWORD"))
   - 手机：verificationCodeAppService.verifyPhoneCode(
               new VerifySmsCodeCommand(account, verificationCode, "RESET_PASSWORD"))
   → 验证码错误/过期/已使用 → 透传 VerificationCodeError

2. 查找用户：
   - 邮箱：userRepository.findByEmail(account)
   - 手机：userRepository.findByPhoneNumber(account)
   → 为空 → 抛 VerificationCodeError.CODE_INVALID（防枚举，不暴露账号是否存在）

3. user.resetPassword(newPassword)
   → 密码强度校验（PASSWORD_WEAK）→ BCrypt 加密 → 更新密码字段

4. userRepository.save(user)

5. try { sessionManagementPort.kickout(user.getId()); }
   catch (Exception e) { log.warn("kickout failed userId={}", user.getId(), e); }
   → kickout 异常被吞掉，确保 save 事务正常提交，不因 kickout 失败而回滚
```

> **事务说明：** 整个方法加 `@Transactional`，步骤 1-4 在事务保护内。步骤 5 的 `kickout` 在事务边界内被 try-catch 包裹：若 kickout 抛出异常，异常不向外传播，事务正常提交（密码已更新）；若异常向外传播则会触发事务回滚（密码未更新、旧 Token 未失效），因此必须捕获。kickout 失败（极罕见）时旧 Token 在 7 天后自然过期，最终一致性风险可接受。

> **验证码消耗说明：** 步骤 1 中验证码校验成功即标记为已使用（`verifyAndMarkAsUsed`）。若后续步骤 2 账号不存在，验证码已消耗，用户需重新发送验证码。合法用户（账号存在且验证码正确）不会遇到此问题。

---

## 领域模型变更

### User 聚合根 — 新增 resetPassword 方法

```java
public void resetPassword(String plainPassword) {
    // 复用已有密码强度校验逻辑（PASSWORD_WEAK）
    // BCrypt 加密
    // 更新 this.password
}
```

不需要旧密码，直接重置。与现有 `updatePassword(oldPassword, newPassword)` 共存，后者用于已登录用户主动修改密码场景。

### SessionManagementPort 接口 — 新增

```java
// com.aieducenter.account.domain.port
@Port(PortType.CLIENT)
public interface SessionManagementPort {
    void kickout(Long userId);
}
```

独立的端口接口，不修改 `cartisan-security` 的 `AuthenticationService`（该接口来自外部库，当前只有 `login/logout/getTokenInfo/authenticate`）。

### SaTokenSessionManagementAdapter — 新增

```java
// com.aieducenter.account.infrastructure
@Service
public class SaTokenSessionManagementAdapter implements SessionManagementPort {
    @Override
    public void kickout(Long userId) {
        StpUtil.kickout(userId);
    }
}
```

---

## DTO 清单

### 新增

```java
// account/application/dto/
ResetPasswordCommand(account, verificationCode, newPassword)
```

---

## 包结构

### Account 上下文（变更）

```
com.aieducenter.account
├── application
│   ├── AccountPasswordResetAppService.java  // 新增
│   └── dto/
│       └── ResetPasswordCommand.java        // 新增
├── domain
│   ├── aggregate/User.java                  // 新增 resetPassword(newPassword)
│   └── port/
│       └── SessionManagementPort.java       // 新增（独立端口，不修改 AuthenticationService）
├── infrastructure/
│   └── SaTokenSessionManagementAdapter.java // 新增（实现 SessionManagementPort）
└── web/
    └── AccountController.java               // 新增 POST /reset-password
```

---

## 测试覆盖

### 单元测试

| 测试类 | 覆盖场景 |
|--------|---------|
| `UserTest`（补充） | `resetPassword()` 成功；密码弱抛 PASSWORD_WEAK |
| `AccountPasswordResetAppServiceTest`（新增） | 邮箱重置成功（verifyCode + findByEmail + kickout 被调用）；手机号重置成功（verifyPhoneCode + findByPhoneNumber + kickout 被调用）；验证码错误透传 400；账号不存在返回 400 CODE_INVALID；kickout 被调用验证 |

mock：`UserRepository`、`VerificationCodeAppService`、`SessionManagementPort`

### 集成测试

| 测试类 | 覆盖场景 |
|--------|---------|
| `AccountPasswordResetIntegrationTest`（新增） | 完整邮箱重置流程；完整手机号重置流程；验证码错误 400；账号不存在 400 |

**邮箱/手机号完整重置流程测试步骤：**
1. 注册用户（含邮箱或手机号）
2. 登录，记录旧 Token
3. 发送重置验证码（`purpose=RESET_PASSWORD`）
4. 调用 `POST /api/account/reset-password`，携带账号、验证码、新密码
5. 用旧 Token 访问受保护接口（`GET /api/account/profile`），验证返回 401（Token 已失效）
6. 用新密码重新登录，验证成功

---

## 设计决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 单端点自动判断账号类型 | 是 | 与 F02-04 登录 `account` 字段风格一致，客户端体验好 |
| 旧 Token 失效策略 | kickout | 安全性保障，防止密码泄露后旧 Token 仍可用 |
| 新建 AccountPasswordResetAppService | 是 | 职责单一，与注册/登录分开 |
| resetPassword 不需要旧密码 | 是 | 忘记密码场景，用户无法提供旧密码；与 updatePassword 区分使用场景 |
| 重置成功不返回 Token | 是 | 安全性考量：强制用户使用新密码重新登录，确认身份 |
| 账号不存在返回 400 CODE_INVALID | 是 | 防枚举攻击，与登录场景的 ACCOUNT_NOT_FOUND(401) 策略一致 |
| SessionManagementPort 独立接口 | 是 | `cartisan-security` 的 `AuthenticationService` 是外部库接口，无法直接修改；独立端口符合 DDD 六边形架构，保持领域层零基础设施依赖 |
| @Transactional + kickout try-catch | 是 | 整个方法加 @Transactional；kickout 用 try-catch 包裹，防止异常传播触发事务回滚。kickout 失败时密码已更新，旧 Token 7 天自然过期，最终一致性可接受 |
| SessionManagementPort 使用 @Port(PortType.CLIENT) | 是 | 与 MessageSender 保持一致；CLIENT 语义用于外部服务调用（Sa-Token），符合 cartisan-boot ArchUnit 规则 |
