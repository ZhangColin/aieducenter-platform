# Feature: F02-02 邮箱验证码服务 — 接口契约

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 2 完成

---

## 一、HTTP 接口定义

### 1.1 发送邮箱验证码

**端点：** `POST /api/account/verification-code/email`

**描述：** 向指定邮箱发送 6 位数字验证码，支持注册和密码重置场景。

**鉴权：** 否

---

**Request Body:**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| email | String | 是 | 邮箱格式 | 目标邮箱地址 |
| purpose | String | 是 | REGISTER 或 RESET_PASSWORD | 验证码使用目的 |

**示例：**
```json
{
  "email": "user@example.com",
  "purpose": "REGISTER"
}
```

---

**Response (成功 - 200 OK):**

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 200 |
| message | String | "验证码已发送" |
| data.expireInSeconds | Integer | 300（有效期秒数） |
| data.resentAfterSeconds | Integer | 60（重发间隔秒数） |
| requestId | String | 请求追踪 ID |

**示例：**
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": {
    "expireInSeconds": 300,
    "resentAfterSeconds": 60
  },
  "requestId": "req-123456"
}
```

---

**错误响应：**

| 错误码 | HTTP Status | 触发条件 | 响应示例 |
|--------|-------------|----------|----------|
| EMAIL_INVALID | 400 | 邮箱格式不正确 | `{"code": 400, "message": "邮箱格式不正确"}` |
| PURPOSE_INVALID | 400 | purpose 枚举值无效 | `{"code": 400, "message": "验证码目的无效"}` |
| RATE_LIMIT_EMAIL | 429 | 同一邮箱60秒内已发送 | `{"code": 429, "message": "请60秒后再试"}` |
| RATE_LIMIT_IP | 429 | 同一IP每小时超限 | `{"code": 429, "message": "发送次数过多，请稍后再试"}` |

---

### 1.2 校验验证码

**端点：** `POST /api/account/verify-code`

**描述：** 校验邮箱验证码是否正确，用于注册或密码重置流程。

**鉴权：** 否

---

**Request Body:**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| email | String | 是 | 邮箱格式 | 邮箱地址 |
| code | String | 是 | 6位数字 | 验证码 |
| purpose | String | 是 | REGISTER 或 RESET_PASSWORD | 验证码使用目的 |

**示例：**
```json
{
  "email": "user@example.com",
  "code": "123456",
  "purpose": "REGISTER"
}
```

---

**Response (成功 - 200 OK):**

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 200 |
| message | String | "验证码正确" |
| data.verified | Boolean | true |
| requestId | String | 请求追踪 ID |

**示例：**
```json
{
  "code": 200,
  "message": "验证码正确",
  "data": {
    "verified": true
  },
  "requestId": "req-123456"
}
```

---

**错误响应：**

| 错误码 | HTTP Status | 触发条件 | 响应示例 |
|--------|-------------|----------|----------|
| EMAIL_INVALID | 400 | 邮箱格式不正确 | `{"code": 400, "message": "邮箱格式不正确"}` |
| CODE_INVALID | 400 | 验证码错误或不存在 | `{"code": 400, "message": "验证码错误"}` |
| CODE_EXPIRED | 400 | 验证码已过期 | `{"code": 400, "message": "验证码已过期"}` |
| CODE_ALREADY_USED | 400 | 验证码已使用 | `{"code": 400, "message": "验证码已使用"}` |
| PURPOSE_INVALID | 400 | purpose 枚举值无效 | `{"code": 400, "message": "验证码目的无效"}` |

---

## 二、领域接口描述（伪代码）

### 2.1 VerificationCode 聚合根

```java
聚合根：VerificationCode
属性：
  - id: String                    // 唯一标识，格式 {target}:{purpose}
  - type: VerificationType        // EMAIL / SMS
  - target: String                // 邮箱或手机号
  - code: String                  // 6位数字
  - purpose: VerificationPurpose  // REGISTER / RESET_PASSWORD
  - expireAt: Instant             // 过期时间
  - used: boolean                 // 是否已使用
  - createdAt: Instant            // 创建时间

行为：
  - isValid(inputCode: String): boolean
    前置条件：无
    后置条件：返回 inputCode 匹配 && 未过期 && 未使用
    异常：无

  - markAsUsed(): void
    前置条件：!used
    后置条件：used = true
    异常：IllegalStateException（如果已使用）

工厂方法：
  - create(type, target, purpose): VerificationCode
    前置条件：target 格式正确
    后置条件：生成 6 位随机码，expireAt = now + 5分钟，used = false
    异常：无
```

### 2.2 领域服务

```java
服务：VerificationCodeGenerator
职责：生成符合规则的验证码

方法：
  - generate(): String
    返回：6 位随机数字（100000-999999）
    保证：连续调用返回不同值
```

```java
服务：RateLimitService
职责：检查限流规则

方法：
  - checkEmailCooldown(email: String, purpose: String): void
    前置条件：无
    后置条件：如果邮箱在 60 秒内已发送，抛 RATE_LIMIT_EMAIL

  - checkIpLimit(ip: String): void
    前置条件：无
    后置条件：如果 IP 在 1 小时内发送超过 10 次，抛 RATE_LIMIT_IP
```

### 2.3 端口接口

```java
端口：VerificationCodeRepository (PortType.REPOSITORY)
职责：验证码持久化抽象

方法：
  - save(code: VerificationCode): void
  - find(id: String): Optional<VerificationCode>
```

```java
端口：MessageSender (PortType.MESSAGING)
职责：发送消息（邮件/短信）抽象

方法：
  - send(target: String, code: String, purpose: VerificationPurpose): void
```

---

## 三、数据结构

### 3.1 SendEmailCodeCommand

```java
Record {
  email: String          // 邮箱地址
  purpose: String        // "REGISTER" | "RESET_PASSWORD"
}
```

### 3.2 VerifyCodeCommand

```java
Record {
  email: String          // 邮箱地址
  code: String           // 6位验证码
  purpose: String        // "REGISTER" | "RESET_PASSWORD"
}
```

### 3.3 VerifyCodeResult

```java
Record {
  verified: boolean      // 是否校验通过
  message: String        // 结果消息
}
```

### 3.4 VerificationPurpose 枚举

```java
enum VerificationPurpose {
  REGISTER,           // 用户注册
  RESET_PASSWORD      // 密码重置
}
```

### 3.5 VerificationType 枚举

```java
enum VerificationType {
  EMAIL,   // 邮箱验证码
  SMS      // 短信验证码（预留）
}
```

---

## 四、错误码枚举

```java
enum VerificationCodeError implements CodeMessage {
  // 业务错误
  EMAIL_INVALID("VERIFICATION_EMAIL_INVALID", "邮箱格式不正确", 400),
  CODE_INVALID("VERIFICATION_CODE_INVALID", "验证码错误", 400),
  CODE_EXPIRED("VERIFICATION_CODE_EXPIRED", "验证码已过期", 400),
  CODE_ALREADY_USED("VERIFICATION_CODE_ALREADY_USED", "验证码已使用", 400),
  PURPOSE_INVALID("VERIFICATION_PURPOSE_INVALID", "验证码目的无效", 400),

  // 限流错误
  RATE_LIMIT_EMAIL("VERIFICATION_RATE_LIMIT_EMAIL", "请60秒后再试", 429),
  RATE_LIMIT_IP("VERIFICATION_RATE_LIMIT_IP", "发送次数过多，请稍后再试", 429);
}
```

---

## 五、Redis 存储策略

### 5.1 Key 设计

| 用途 | Key 格式 | Value | TTL |
|------|----------|-------|-----|
| 验证码 | `verification:{email}:{purpose}` | JSON: {code, expireAt, used} | 300s |
| 邮箱限流 | `limit:email:{email}:{purpose}` | count | 60s |
| IP 限流 | `limit:ip:{ip}` | count | 3600s |

### 5.2 Redis 操作

| 操作 | 命令 |
|------|------|
| 保存验证码 | SET key value NX EX 300 |
| 获取验证码 | GET key |
| 邮箱限流 | SET key 1 NX EX 60 |
| IP 限流 | INCR key, EXPIRE key 3600 |
