# F02-04 通用注册与登录 设计文档

> 版本：v1.0 | 日期：2026-03-19
> 状态：已批准
> Epic：Epic 2 - 用户与登录

---

## 背景

本 Feature 包含两部分：

1. **重构注册**：修正 F02-03 错误实现的"邮箱专属注册 + 注册验证码"设计，改为通用注册（用户名 + 密码，邮箱/手机可选，无需验证码）。
2. **登录**：实现密码登录（用户名/邮箱/手机 + 密码）和手机短信验证码登录（初期 mock，日志打印）。

**前置依赖：** F02-01（User 聚合根）、F02-02（VerificationCodeAppService）、F02-09（Sa-Token 认证基础设施）

---

## 删除清单

| 文件 / 方法 | 原因 |
|-------------|------|
| `RegisterByEmailCommand.java` | 替换为通用 `RegisterCommand` |
| `AccountRegistrationAppService.registerByEmail()` | 替换为通用 `register()` |
| `AccountController.POST /register/email` | 替换为 `/register` |
| `AccountRegistrationAppServiceTest` 中邮箱专属测试场景 | 重写为通用注册测试 |
| `AccountRegistrationIntegrationTest` 中邮箱专属测试场景 | 同上 |
| `User.registerByEmail()` 工厂方法 | 替换为 `User.register()` |

`UserRegisteredEvent`、`UserRegisteredEventListener`、`TenantAppService` 等保持不变，通用注册同样发布该事件。

---

## 接口设计

### POST /api/account/register

**鉴权：** 否（公开路径 `/api/account/register`）

**请求字段：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 3-20位，字母开头，允许字母/数字/下划线 |
| password | String | 是 | 8-20位，必须同时包含字母和数字 |
| nickname | String | 否 | 最长 50 字符，为空则使用用户名 |
| email | String | 否 | 邮箱格式 |
| phone | String | 否 | 11位手机号（1[3-9]开头） |

**成功响应（200）：**
```json
{ "code": 200, "message": "success", "data": { "token": "uuid-string" } }
```

**错误响应：**

| 场景 | HTTP Status | 错误码 |
|------|-------------|--------|
| 必填字段为空 | 400 | Bean Validation |
| 密码强度不足 | 400 | UserError.PASSWORD_WEAK |
| 邮箱格式错误 | 400 | UserError.EMAIL_INVALID |
| 手机号格式错误 | 400 | UserError.PHONE_NUMBER_INVALID |
| 用户名已存在 | 409 | UserError.USERNAME_ALREADY_EXISTS |
| 邮箱已被使用 | 409 | UserError.EMAIL_ALREADY_EXISTS |
| 手机号已被使用 | 409 | UserError.PHONE_NUMBER_ALREADY_EXISTS |

---

### POST /api/account/login

**鉴权：** 否

**请求字段：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | String | 是 | 用户名 / 邮箱 / 手机号均可 |
| password | String | 是 | 明文密码 |

**错误响应：**

| 场景 | HTTP Status | 错误码 |
|------|-------------|--------|
| 账号不存在 | 401 | UserError.ACCOUNT_NOT_FOUND |
| 密码错误 | 401 | UserError.LOGIN_PASSWORD_INCORRECT |

---

### POST /api/account/verification-code/sms

**鉴权：** 否

**请求字段：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 是 | 手机号 |
| purpose | String | 是 | 目的，当前支持 "LOGIN" |

**错误响应：**

| 场景 | HTTP Status | 错误码 |
|------|-------------|--------|
| 手机号格式错误 | 400 | VerificationCodeError.PHONE_INVALID |
| purpose 无效 | 400 | VerificationCodeError.PURPOSE_INVALID |
| 手机号 60 秒限流 | 429 | VerificationCodeError.RATE_LIMIT_PHONE |
| IP 超限 | 429 | VerificationCodeError.RATE_LIMIT_IP |

---

### POST /api/account/login/sms

**鉴权：** 否

**请求字段：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 是 | 手机号 |
| code | String | 是 | 6位短信验证码 |

**错误响应：**

| 场景 | HTTP Status | 错误码 |
|------|-------------|--------|
| 验证码错误/过期/已使用 | 400 | VerificationCodeError（透传） |
| 手机号未注册 | 401 | UserError.ACCOUNT_NOT_FOUND |

---

## 架构设计

### 通用注册流程（AccountRegistrationAppService.register，@Transactional）

```
1. existsByUsername(username) → true → 抛 UserError.USERNAME_ALREADY_EXISTS（409）

2. email 非空 → existsByEmail(email) → true → 抛 UserError.EMAIL_ALREADY_EXISTS（409）

3. phone 非空 → existsByPhoneNumber(phone) → true → 抛 UserError.PHONE_NUMBER_ALREADY_EXISTS（409）

4. User.register(username, password, nickname, email, phone)
   → 密码强度校验（已有）+ BCrypt 加密
   → email 非空 → updateEmail(email)（含邮箱格式校验）
   → phone 非空 → updatePhoneNumber(phone)（含手机格式校验）

5. userRepository.save(user)

6. authenticationService.login(user.getId()) → TokenInfo → 取 token

7. applicationEventPublisher.publishEvent(UserRegisteredEvent(...))
   → 同步触发 UserRegisteredEventListener → TenantAppService.createPersonalTenant(...)
   → 租户创建失败 → 整个注册事务回滚

8. return RegisterResult(token)
```

### 密码登录流程（AccountLoginAppService.loginByPassword，无 @Transactional）

```
1. findByUsername(account)
   → 为空 → findByEmail(account)
   → 为空 → findByPhoneNumber(account)
   → 全部为空 → 抛 UserError.ACCOUNT_NOT_FOUND（401）

2. user.matchesPassword(password)
   → false → 抛 UserError.LOGIN_PASSWORD_INCORRECT（401）

3. authenticationService.login(user.getId()) → TokenInfo → 取 token

4. return LoginResult(token)
```

### 短信验证码登录流程（AccountLoginAppService.loginBySms，无 @Transactional）

```
1. verificationCodeAppService.verifyPhoneCode(
       new VerifySmsCodeCommand(phone, code, "LOGIN"))
   → 验证码错误/过期/已使用 → 透传 VerificationCodeError

2. userRepository.findByPhoneNumber(phone)
   → 为空 → 抛 UserError.ACCOUNT_NOT_FOUND（401）

3. authenticationService.login(user.getId()) → TokenInfo → 取 token

4. return LoginResult(token)
```

### 发送短信验证码流程（VerificationCodeAppService.sendSmsVerificationCode）

```
1. 校验手机号格式（hutool Validator.isMobile）
   → 不合法 → 抛 VerificationCodeError.PHONE_INVALID

2. validatePurpose(purpose)（已有方法）

3. tryAcquirePhoneLock(phone, purpose)
   → false → 抛 VerificationCodeError.RATE_LIMIT_PHONE（60秒内只能发一次）

4. checkAndIncrementIp(ip)（已有方法）
   → 超限 → 抛 VerificationCodeError.RATE_LIMIT_IP

5. 生成验证码（已有）

6. VerificationCode.create(VerificationType.SMS, phone, code, purpose)

7. repository.save(verificationCode)

8. messageSender.send(phone, code, purpose)
   → LogMessageSenderAdapter 打印日志（mock）
   → 日志格式：[SMS MOCK] phone={} purpose={} code={}

9. return SendCodeResponse(expireSeconds, cooldownSeconds)
```

---

## 领域模型变更

### User 聚合根

**新增工厂方法 `User.register()`（替换 `registerByEmail`）：**

```java
public static User register(String username, String plainPassword,
                             String nickname, String email, String phone)
```

- 调用现有构造函数（含密码强度校验 + BCrypt）
- email 非空 → 调用 `updateEmail(email)`（含邮箱格式校验）
- phone 非空 → 调用 `updatePhoneNumber(phone)`（含手机格式校验）

**删除：** `User.registerByEmail()` 工厂方法

### UserError 变更

`PHONE_NUMBER_ALREADY_EXISTS`（USER_006, 409）已存在，无需新增。

新增两个枚举值：

```java
ACCOUNT_NOT_FOUND(401, "USER_011", "账号不存在")
LOGIN_PASSWORD_INCORRECT(401, "USER_012", "账号或密码错误")
```

> **说明**：
> - `USER_NOT_FOUND`（USER_010, 404）用于资源查询场景（如按 ID 查用户）；`ACCOUNT_NOT_FOUND`（401）用于登录，避免泄露账号是否存在。
> - 现有 `PASSWORD_INCORRECT`（USER_007, 400）用于修改密码等场景，HTTP 状态为 400。登录场景需要返回 401，新增 `LOGIN_PASSWORD_INCORRECT`（USER_012, 401）与之区分，密码登录流程使用该新错误码。

### VerificationPurpose 新增

```java
LOGIN    // 短信验证码登录（已有 REGISTER）
```

### VerificationCodeError 新增

```java
PHONE_INVALID       // 手机号格式错误
RATE_LIMIT_PHONE    // 手机号发送频率超限（60秒内）
```

### VerificationCodeRepository 新增

```java
boolean tryAcquirePhoneLock(String phone, String purpose);
```

语义与 `tryAcquireEmailLock` 相同：原子操作，60秒内同一手机号+purpose 只能获取一次锁。Redis key 使用独立前缀 `limit:phone:{phone}:{purpose}`。

### RedisVerificationCodeRepository 变更（修复 type 硬编码）

`findById` 当前在还原 `VerificationCode` 时硬编码 `VerificationType.EMAIL`（第80行）。SMS 验证码走错误明细路径时会返回类型错误的对象。

修复：在 `save()` 时将 `type` 字段写入 Redis hash，在 `findById()` 时读取并还原，不再硬编码。

```java
// save() 新增一行
data.put("type", code.getType().name());

// findById() 替换硬编码
VerificationType type = VerificationType.valueOf(
    (String) redisTemplate.opsForHash().get(key, "type"));
```

### VerificationCodeProperties 新增

新增手机号限流配置字段（默认 60 秒，与邮箱相同）：

```java
private long phoneCooldownSeconds = 60;
// getter/setter: getPhoneCooldownSeconds / setPhoneCooldownSeconds
```

`sendSmsVerificationCode` 返回 `SendCodeResponse` 时使用 `properties.getPhoneCooldownSeconds()` 作为 cooldown 参数，`RedisVerificationCodeRepository.tryAcquirePhoneLock` 使用该值作为 TTL。

---

## DTO 清单

### 新增

```java
// account/application/dto/
RegisterCommand(username, password, nickname, email, phone)
LoginByPasswordCommand(account, password)
LoginBySmsCommand(phone, code)
LoginResult(token)

// verification/application/dto/
SendSmsCodeCommand(phone, purpose)
VerifySmsCodeCommand(phone, code, purpose)
```

### 删除

```java
RegisterByEmailCommand   // 被 RegisterCommand 替代
```

---

## 包结构

### Account 上下文（变更）

```
com.aieducenter.account
├── application
│   ├── AccountRegistrationAppService.java   // 重写（通用注册）
│   ├── AccountLoginAppService.java          // 新增（密码登录 + 短信登录）
│   └── dto/
│       ├── RegisterCommand.java             // 新增
│       ├── RegisterResult.java              // 保留
│       ├── LoginByPasswordCommand.java      // 新增
│       ├── LoginBySmsCommand.java           // 新增
│       └── LoginResult.java                 // 新增
│       [删除 RegisterByEmailCommand.java]
├── domain
│   └── aggregate/User.java                  // 替换 registerByEmail → register
└── web/
    └── AccountController.java               // 新增 /register、/login、/login/sms
```

### Verification 上下文（变更）

```
com.aieducenter.verification
├── application
│   ├── VerificationCodeAppService.java      // 新增 sendSmsVerificationCode + verifyPhoneCode
│   └── dto/
│       ├── SendSmsCodeCommand.java          // 新增
│       └── VerifySmsCodeCommand.java        // 新增
├── domain
│   ├── model/VerificationPurpose.java       // 新增 LOGIN
│   ├── error/VerificationCodeError.java     // 新增 PHONE_INVALID、RATE_LIMIT_PHONE
│   └── repository/VerificationCodeRepository.java  // 新增 tryAcquirePhoneLock
├── config
│   └── VerificationCodeProperties.java      // 新增 phoneCooldownSeconds
├── infrastructure/redis
│   └── RedisVerificationCodeRepository.java // 修复 findById type 硬编码 + 新增 tryAcquirePhoneLock
└── web/
    └── VerificationCodeController.java      // 新增 /verification-code/sms
                                             // base mapping 保持 @RequestMapping("/api/account")
                                             // 最终路径：POST /api/account/verification-code/sms
```

---

## 测试覆盖

### 单元测试

| 测试类 | 覆盖场景 |
|--------|---------|
| `UserTest`（修改） | `register()` 正常创建；email/phone 可选均不填；email 格式错误；phone 格式错误；密码弱；删除 `registerByEmail` 相关用例 |
| `AccountRegistrationAppServiceTest`（重写） | 成功注册（无邮箱无手机）；用户名重复 409；邮箱重复 409；手机号重复 409；事件发布验证；token 返回 |
| `AccountLoginAppServiceTest`（新增） | 用户名登录成功；邮箱登录成功；手机号登录成功；账号不存在 401；密码错误 401；短信登录成功；验证码错误透传；手机号未注册 401 |
| `VerificationCodeAppServiceTest`（补充） | 发送短信验证码成功；手机格式错误；60秒限流；IP 限流；`verifyPhoneCode` 成功；验证码错误/过期/已使用 |

`AccountRegistrationAppServiceTest` mock：`UserRepository`、`ApplicationEventPublisher`、`AuthenticationService`（注：注册不再依赖 `VerificationCodeAppService`）

`AccountLoginAppServiceTest` mock：`UserRepository`、`VerificationCodeAppService`、`AuthenticationService`

### 集成测试

| 测试类 | 覆盖场景 |
|--------|---------|
| `AccountRegistrationIntegrationTest`（重写） | 完整注册流程（DB 有 user + tenant，token 有效）；用户名重复 409；弱密码 400 |
| `AccountLoginIntegrationTest`（新增） | 密码登录（用户名/邮箱/手机三种 account）；短信登录完整流程（发码 → 验证 → 登录）；账号不存在 401 |

---

## 设计决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 注册去掉验证码 | 是 | 注册是创建账号，邮箱/手机绑定是独立业务，无需在注册阶段验证 |
| 删除 `/register/email` | 彻底删除 | F02-03 设计错误，保留会造成持续混乱 |
| 账号查找顺序 | username → email → phone | username 最常用且查询最快；email/phone 为补充登录方式 |
| SMS mock 方式 | LogMessageSenderAdapter（复用） | 现有实现已是通用 log，无需额外 mock 类；后续换真实 SMS 只需新增 Adapter Bean |
| VerifySmsCodeCommand 独立 | 与 VerifyCodeCommand 分开 | 语义不同（phone vs email），避免混用造成格式校验逻辑复杂化 |
| 短信登录不需要 purpose 字段暴露给客户端 | LoginBySmsCommand 无 purpose | purpose 固定为 LOGIN，由服务端写死，不暴露给客户端 |
