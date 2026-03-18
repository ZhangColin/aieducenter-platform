# F02-02 邮箱验证码服务 - 测试规格

> **版本:** 1.0.0
> **日期:** 2026-03-18
> **状态:** 已完成

## 1. 测试概览

### 1.1 测试范围

| 层级 | 测试类型 | 覆盖范围 |
|------|----------|----------|
| 领域层 | 单元测试 | VerificationCode 聚合根、VerificationCodeGenerator |
| 应用层 | 单元测试 | VerificationCodeApplicationService |
| 基础设施层 | 集成测试 | RedisVerificationCodeRepository (Redis 原子操作) |
| Web 层 | 单元测试 | VerificationCodeController |

### 1.2 测试统计

| 指标 | 数量 |
|------|------|
| 总测试数 | 18 |
| 领域层测试 | 7 |
| 应用层测试 | 7 |
| Web 层测试 | 2 |
| 集成测试 | 6 |
| 代码行覆盖率 | >85% |

## 2. 单元测试

### 2.1 领域层测试

**VerificationCodeTest** - 验证码聚合根测试

| 测试方法 | 场景 | 预期结果 |
|----------|------|----------|
| `given_valid_input_when_create_then_success` | 创建验证码 | 字段正确设置，5分钟过期 |
| `given_code_matches_when_validate_then_true` | 验证码正确 | 返回 true |
| `given_wrong_code_when_validate_then_false` | 验证码错误 | 返回 false |
| `given_code_used_when_validate_then_false` | 已使用 | 返回 false |
| `given_code_not_used_when_mark_used_then_success` | 标记已使用 | isUsed = true |
| `given_code_used_when_mark_used_then_exception` | 重复标记 | 抛出 DomainException |
| `given_id_matches_when_compare_identity_then_true` | ID 相同 | sameIdentityAs = true |

**VerificationCodeGeneratorTest** - 验证码生成器测试

| 测试方法 | 场景 | 预期结果 |
|----------|------|----------|
| `given_generator_when_generate_then_6_digit` | 生成验证码 | 6 位数字 |
| `given_generator_when_multiple_then_different` | 多次生成 | 结果不同 |
| `given_generator_when_generate_then_in_range` | 数值范围 | 100000-999999 |

### 2.2 应用层测试

**VerificationCodeApplicationServiceTest** - 应用服务测试

| 测试方法 | 场景 | 预期结果 |
|----------|------|----------|
| `given_valid_email_and_no_rate_limit_when_send_then_success` | 正常发送 | 返回响应，调用仓储和消息发送器 |
| `given_invalid_email_when_send_then_exception` | 邮箱格式错误 | EMAIL_INVALID 异常 |
| `given_email_rate_limited_when_send_then_exception` | 邮箱限流 | RATE_LIMIT_EMAIL 异常 |
| `given_ip_rate_limited_when_send_then_exception` | IP 限流 | RATE_LIMIT_IP 异常 |
| `given_valid_code_when_verify_then_success` | 验证码正确 | 返回 verified=true |
| `given_invalid_code_when_verify_then_exception` | 验证码错误 | DomainException |
| `given_code_not_found_when_verify_then_exception` | 验证码不存在 | CODE_INVALID 异常 |

### 2.3 Web 层测试

**VerificationCodeControllerTest** - 控制器测试

| 测试方法 | 场景 | 预期结果 |
|----------|------|----------|
| `given_valid_request_when_send_code_then_200` | 发送验证码 | HTTP 200，返回数据 |
| `given_valid_code_when_verify_then_200` | 校验验证码 | HTTP 200，verified=true |

## 3. 集成测试

### 3.1 Redis 仓储集成测试

**RedisVerificationCodeRepositoryTest** - Redis 原子操作测试

| 测试方法 | 场景 | 验证内容 |
|----------|------|----------|
| `given_first_request_when_acquire_lock_then_success` | 首次获取邮箱锁 | setIfAbsent 返回 true |
| `given_second_request_when_acquire_lock_then_fail` | 重复获取邮箱锁 | setIfAbsent 返回 false |
| `given_first_ip_when_increment_then_1` | 首次 IP 计数 | increment 返回 1 |
| `given_multiple_ips_when_increment_then_count` | 多次 IP 计数 | 返回递增值 |
| `given_ip_exceeded_when_increment_then_over_limit` | IP 超限 | 返回超过限制的计数 |
| `given_lock_expired_when_acquire_then_success` | 锁过期后 | 可重新获取 |

### 3.2 测试环境要求

- Redis 运行在 localhost:6379
- 测试前清空数据库 (flushDb)
- 使用 StringRedisTemplate 确保序列化一致

## 4. 测试规范

### 4.1 命名规范

遵循 `given_{条件}_when_{操作}_then_{预期结果}` 格式：

```java
@Test
void given_valid_email_and_no_rate_limit_when_send_verification_code_then_success() {
    // Given - 准备测试数据
    // When - 执行被测试方法
    // Then - 验证结果
}
```

### 4.2 断言规范

使用 AssertJ 断言：

```java
import static org.assertj.core.api.Assertions.*;

assertThat(result.verified()).isTrue();
assertThat(exception).hasMessageContaining(VerificationCodeError.EMAIL_INVALID.message());
```

### 4.3 Mock 规范

使用 Mockito，严格 when-then-verify 模式：

```java
when(repository.tryAcquireEmailLock(email, purpose)).thenReturn(true);
// ... 执行测试
verify(repository).tryAcquireEmailLock(email, purpose);
```

## 5. 运行测试

### 5.1 运行所有测试

```bash
./gradlew test --tests "com.aieducenter.verification.**"
```

### 5.2 运行特定层级

```bash
# 领域层
./gradlew test --tests "com.aieducenter.verification.domain.**"

# 应用层
./gradlew test --tests "com.aieducenter.verification.application.**"

# Web 层
./gradlew test --tests "com.aieducenter.verification.web.**"

# 集成测试
./gradlew test --tests "com.aieducenter.verification.infrastructure.**"
```

### 5.3 测试配置

默认配置（application.properties 可覆盖）：

```properties
verification.code.expire-minutes=5
verification.code.email-cooldown-seconds=60
verification.code.ip-max-per-hour=10
```

## 6. 质量门禁

### 6.1 通过标准

- ✅ 所有单元测试通过
- ✅ 集成测试通过（需要 Redis）
- ✅ 代码行覆盖率 >85%
- ✅ 遵循测试命名规范

### 6.2 Phase 5 修复项

**Critical 竞态条件修复：**
1. ✅ 邮箱限流：`tryAcquireEmailLock()` 使用 Redis `setIfAbsent` 原子操作
2. ✅ 验证码校验：`verifyAndMarkAsUsed()` 使用 Lua 脚本原子操作
3. ✅ IP 限流：`checkAndIncrementIp()` 使用 Redis `increment` 原子操作

**其他改进：**
4. ✅ 测试命名规范化
5. ✅ 邮箱验证正则改进
6. ✅ IP 限制可配置
7. ✅ 集成测试覆盖
8. ✅ restore() 方法设置 createdAt

## 7. 未覆盖项

以下项标记为未来改进：

1. **RequestId 追踪** - API 响应中添加 requestId 用于链路追踪
2. **Testcontainers** - 使用 Docker 容器替代本地 Redis（已简化为直接连接）
3. **PIT 变异测试** - 需要额外配置，待后续添加

## 8. 附录

### 8.1 测试文件清单

```
server/src/test/java/com/aieducenter/verification/
├── domain/
│   ├── model/
│   │   └── VerificationCodeTest.java (7 tests)
│   └── service/
│       └── VerificationCodeGeneratorTest.java (3 tests)
├── application/
│   └── VerificationCodeApplicationServiceTest.java (7 tests)
├── infrastructure/redis/
│   └── RedisVerificationCodeRepositoryTest.java (6 tests)
└── web/
    └── VerificationCodeControllerTest.java (2 tests)
```

### 8.2 依赖项

```gradle
// Testcontainers for future use
testImplementation("org.testcontainers:testcontainers:1.20.1")
testImplementation("org.testcontainers:junit-jupiter:1.20.1")
```
