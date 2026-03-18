# Feature: F02-02 邮箱验证码服务

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 1 完成
> Epic：Epic 2 - 用户与登录

---

## 背景

邮箱验证码是用户注册、密码重置等场景的必要安全机制。本 Feature 实现验证码的生成、存储、限流、校验核心逻辑，作为独立限界上下文供其他上下文复用。

---

## 目标

- 建立 Verification 独立限界上下文
- 实现邮箱验证码发送与校验
- 支持多场景（REGISTER、RESET_PASSWORD）
- 提供限流保护（邮箱级 + IP级）
- 验证码存储在 Redis，支持自动过期

---

## 范围

### 包含（In Scope）

- Verification 限界上下文（domain、application、infrastructure）
- VerificationCode 聚合根与领域模型
- 邮箱验证码发送接口
- 验证码校验接口
- Redis 存储 + 限流
- 日志模拟邮件发送（真实邮件发送后续扩展）

### 不包含（Out of Scope）

- 真实邮件发送（后续扩展）
- 短信验证码（F02-04）
- 前端页面（F02-10）

---

## 验收标准（Acceptance Criteria）

### AC1: 验证码生成规则

验证码必须满足：
- 6 位随机数字（100000-999999）
- 有效期 5 分钟
- 存储在 Redis，自动过期清理

### AC2: 邮箱限流规则

- 同一邮箱 60 秒内只能发送一次验证码
- 超过限制返回 429 错误，提示"请60秒后再试"

### AC3: IP 限流规则

- 同一 IP 每小时最多发送 10 次验证码
- 超过限制返回 429 错误，提示"发送次数过多，请稍后再试"

### AC4: 发送验证码接口

- 端点：POST /api/account/verification-code/email
- 请求字段：email（必填）、purpose（必填：REGISTER/RESET_PASSWORD）
- 成功响应：返回有效期和重发间隔
- 错误处理：邮箱格式错误、purpose 无效、限流触发

### AC5: 校验验证码接口

- 端点：POST /api/account/verify-code
- 请求字段：email、code（6位数字）、purpose
- 成功响应：返回 verified=true
- 错误处理：验证码错误、已过期、已使用

### AC6: 验证码使用规则

- 验证码校验成功后标记为已使用
- 已使用的验证码不能再次使用
- 过期的验证码校验失败

### AC7: 支持的验证码目的（purpose）

- REGISTER：用户注册
- RESET_PASSWORD：密码重置

### AC8: 单元测试覆盖

| 测试类 | 覆盖场景 |
|--------|----------|
| `VerificationCodeTest` | 创建、校验成功/失败、标记已使用 |
| `VerificationCodeGeneratorTest` | 生成6位数字、连续生成都不同 |
| `RateLimitServiceTest` | 限流检查通过/失败 |

### AC9: 集成测试覆盖

| 测试类 | 覆盖场景 |
|--------|----------|
| `VerificationCodeApplicationServiceTest` | 发送成功、邮箱限流、IP限流、校验成功/失败 |

---

## 约束

### 技术约束

- Java 21 + Spring Boot 3.4
- 使用 cartisan-boot 框架（cartisan-core、cartisan-web、cartisan-data-jpa）
- Redis 用于验证码存储和限流
- 测试使用 JUnit 5 + AssertJ

### 架构约束

- Verification 作为独立限界上下文
- 遵循 DDD 六边形架构
- 端口接口：VerificationCodeRepository（存储）、MessageSender（发送消息）
- 邮件发送使用适配器模式，初期实现为日志输出

### 代码规范

- 测试命名：`should_{预期结果}_when_{条件}`
- 使用 AssertJ 断言
- Record 实现 ValueObject
- 公共接口必须包含 JavaDoc
