# Feature: F02-02 邮箱验证码服务 — 实施计划

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 3 完成

---

## 目标复述

实现邮箱验证码服务，包括验证码生成、存储（Redis）、限流（邮箱+IP）、校验核心逻辑。作为独立 Verification 限界上下文，提供邮箱验证码发送和校验接口，支持 REGISTER 和 RESET_PASSWORD 场景。

---

## 变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `verification/domain/model/` | VerificationCode、VerificationType、VerificationPurpose |
| 新建 | `verification/domain/service/` | VerificationCodeGenerator、RateLimitService |
| 新建 | `verification/domain/repository/` | VerificationCodeRepository 接口 |
| 新建 | `verification/domain/port/` | MessageSender 接口 |
| 新建 | `verification/domain/error/` | VerificationCodeError 枚举 |
| 新建 | `verification/application/` | VerificationCodeApplicationService |
| 新建 | `verification/application/dto/` | Command/Response Record |
| 新建 | `verification/infrastructure/redis/` | RedisVerificationCodeRepository |
| 新建 | `verification/infrastructure/messaging/` | LogMessageSenderAdapter |
| 新建 | `verification/web/` | VerificationCodeController |
| 新建 | `verification/domain/model/test/` | 领域层单元测试 |
| 新建 | `verification/application/test/` | 应用层集成测试 |

---

## 核心流程（伪代码）

### 发送验证码流程

```java
sendEmailVerificationCode(email, purpose) {
    // 1. 校验邮箱格式
    validateEmailFormat(email)

    // 2. 检查邮箱限流（60秒）
    rateLimitService.checkEmailCooldown(email, purpose)

    // 3. 检查IP限流（每小时10次）
    rateLimitService.checkIpLimit(currentIp)

    // 4. 生成6位验证码
    code = verificationCodeGenerator.generate()

    // 5. 创建验证码实体
    verificationCode = VerificationCode.create(EMAIL, email, code, purpose)

    // 6. 存储到 Redis（5分钟过期）
    verificationCodeRepository.save(verificationCode)

    // 7. 发送邮件（日志模拟）
    messageSender.send(email, code, purpose)
}
```

### 校验验证码流程

```java
verifyCode(email, code, purpose) {
    // 1. 从 Redis 获取验证码
    id = generateId(email, purpose)
    verificationCode = verificationCodeRepository.find(id)

    // 2. 校验验证码
    if (verificationCode.isEmpty) {
        throw CODE_INVALID
    }

    if (!verificationCode.get().isValid(code)) {
        throw CODE_EXPIRED or CODE_ALREADY_USED or CODE_INVALID
    }

    // 3. 标记已使用
    verificationCode.get().markAsUsed()

    // 4. 更新 Redis
    verificationCodeRepository.save(verificationCode.get())

    return VerifyCodeResult(verified: true)
}
```

---

## 原子任务清单

### Step 1: 生成领域模型代码

**文件：**
- `verification/domain/model/VerificationCode.java`
- `verification/domain/model/VerificationType.java`
- `verification/domain/model/VerificationPurpose.java`
- `verification/domain/error/VerificationCodeError.java`

**内容：**
- 将 02_interface.md 中的接口描述转为 Java 代码
- VerificationType、VerificationPurpose 为枚举
- VerificationCodeError 实现 CodeMessage 接口

**验证：** 编译通过

---

### Step 2: 编写领域服务接口（红灯）

**文件：**
- `verification/domain/service/VerificationCodeGenerator.java`
- `verification/domain/service/RateLimitService.java`
- `verification/domain/repository/VerificationCodeRepository.java`
- `verification/domain/port/MessageSender.java`

**内容：**
- 定义端口和服务接口
- VerificationCodeRepository 端口接口
- MessageSender 端口接口

**验证：** 编译通过

---

### Step 3: 编写领域模型单元测试（红灯）

**文件：**
- `verification/domain/model/VerificationCodeTest.java`
- `verification/domain/service/VerificationCodeGeneratorTest.java`

**内容：**
- 基于 AC 编写测试
- VerificationCodeTest：创建、校验成功/失败、标记已使用
- VerificationCodeGeneratorTest：生成6位数字、连续生成都不同

**验证：** 编译通过 + 测试全红

---

### Step 4: 编写基础设施适配器（绿灯）

**文件：**
- `verification/infrastructure/redis/RedisVerificationCodeRepository.java`
- `verification/infrastructure/messaging/LogMessageSenderAdapter.java`

**内容：**
- RedisVerificationCodeRepository：使用 RedisTemplate 实现
- LogMessageSenderAdapter：日志输出模拟

**验证：** 编译通过

---

### Step 5: 编写应用服务（绿灯）

**文件：**
- `verification/application/VerificationCodeApplicationService.java`
- `verification/application/dto/SendEmailCodeCommand.java`
- `verification/application/dto/VerifyCodeCommand.java`
- `verification/application/dto/VerifyCodeResult.java`

**内容：**
- 实现发送验证码和校验验证码流程
- Command 使用 Record

**验证：** 编译通过 + Step 3 测试全绿

---

### Step 6: 编写 Controller 层（绿灯）

**文件：**
- `verification/web/VerificationCodeController.java`

**内容：**
- POST /api/account/verification-code/email
- POST /api/account/verify-code
- 返回 ApiResponse<T>

**验证：** 编译通过 + 测试全绿 + ArchUnit 通过

---

### Step 7: 编写集成测试（红灯→绿灯）

**文件：**
- `verification/application/VerificationCodeApplicationServiceTest.java`

**内容：**
- 继承 IntegrationTestBase
- 覆盖：发送成功、邮箱限流、IP限流、校验成功/失败

**验证：** 编译通过 + 测试全绿

---

### Step 8: 全量验证

**命令：**
```bash
./gradlew test           # 单元测试 + 集成测试
./gradlew check          # 包含 ArchUnit
```

**验证：** 所有测试绿灯 + ArchUnit 通过
