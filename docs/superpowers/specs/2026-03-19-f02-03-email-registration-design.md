# F02-03 用户注册 - 邮箱流程 设计文档

> 版本：v1.0 | 日期：2026-03-19
> 状态：已批准
> Epic：Epic 2 - 用户与登录

---

## 背景

F02-03 实现邮箱注册完整流程：验证码校验、密码强度校验、创建 User 聚合根、发布领域事件触发单人租户创建，最终返回 Sa-Token 登录凭证。

本 Feature 同时包含 F02-08（单人租户自动创建）的最小实现，通过领域事件解耦两个限界上下文。

**前置依赖：** F02-01（User 聚合根）、F02-02（VerificationCodeAppService）、F02-09（Sa-Token 认证基础设施）

---

## 接口设计

### POST /api/account/register/email

**鉴权：** 否（在 application.yml 中配置为公开路径 `/api/account/register/**`）

**请求字段：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（3-20位，字母开头） |
| email | String | 是 | 邮箱地址 |
| password | String | 是 | 密码（8-20位，含字母和数字） |
| nickname | String | 否 | 昵称（为空则使用用户名） |
| verificationCode | String | 是 | 6位数字验证码 |

**成功响应（200）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "uuid-string"
  }
}
```

**错误响应：**

| 场景 | HTTP Status | 错误码 |
|------|-------------|--------|
| 必填字段为空 | 400 | Bean Validation（@NotBlank） |
| 验证码错误 / 邮箱格式错误 | 400 | VerificationCodeError（透传） |
| 验证码已过期 | 400 | VerificationCodeError |
| 验证码已使用 | 400 | VerificationCodeError |
| 密码强度不足 | 400 | UserError.PASSWORD_WEAK |
| 用户名已存在 | 409 | UserError.USERNAME_ALREADY_EXISTS |
| 邮箱已被使用 | 409 | UserError.EMAIL_ALREADY_EXISTS |

---

## 架构设计

### 注册流程（AccountRegistrationAppService，@Transactional）

```
1. VerificationCodeAppService.verifyCode(email, code, "REGISTER")
   → 失败直接透传 VerificationCodeError（含邮箱格式校验，不重复校验）

2. userRepository.existsByUsername(username)
   → true 抛 UserError.USERNAME_ALREADY_EXISTS（409）

3. userRepository.existsByEmail(email)
   → true 抛 UserError.EMAIL_ALREADY_EXISTS（409）

4. User.registerByEmail(username, email, password, nickname)
   → 含密码强度校验 + BCrypt 加密 + 邮箱格式校验

5. userRepository.save(user)

6. authenticationService.login(user.getId())
   → 返回 TokenInfo，取 token 字段

7. applicationEventPublisher.publishEvent(new UserRegisteredEvent(...))
   → 同步触发 UserRegisteredEventListener → TenantAppService.createPersonalTenant()

8. return new RegisterResult(token)
```

### 领域事件方式说明

`User` 继承 `SoftDeletable`，不继承 `AbstractAggregateRoot`，无法使用 `registerEvent()`。
采用 `ApplicationEventPublisher.publishEvent()` 在应用层手动发布，`@EventListener` 同步监听，自动参与调用方事务（REQUIRED 传播）。租户创建失败 → 整个注册事务回滚。

---

## User 聚合根变更

### 1a. 密码强度校验

新增私有方法 `validatePasswordStrength(String plainPassword)`：
- 规则：8-20 位，必须同时包含字母和数字
- 正则：`^(?=.*[a-zA-Z])(?=.*\d).{8,20}$`
- 不符合抛 `UserError.PASSWORD_WEAK`
- 在构造函数和 `updatePassword()` 中均调用

### 1b. registerByEmail 工厂方法

```java
public static User registerByEmail(String username, String email,
                                   String plainPassword, String nickname)
```

- 调用现有构造函数（含密码强度校验 + BCrypt 加密）
- 调用 `updateEmail(email)` 绑定邮箱（含格式校验）
- 返回设置了 email 的 User 实例

### 1c. UserRepository 新增 save 方法

```java
User save(User user);
```

`SpringDataJpaUserRepository extends BaseRepository<User, Long>, UserRepository`，JPA 已提供实现，只需在接口声明方法签名。

---

## 领域事件

```java
// package: com.aieducenter.account.domain.event
public record UserRegisteredEvent(
    Long userId,
    String email,
    String nickname,
    java.time.Instant occurredAt
) {}
```

---

## Tenant 上下文（F02-08 最小实现）

### 聚合根 Tenant

字段：`id`（TSID）、`name`（VARCHAR 100）、`type`（TenantType 枚举）、`ownerId`（BIGINT）、审计字段、软删除

字段赋值规则：
- `name`：nickname 不为空则用 nickname，否则用 username（需从 event 传入）
- `type`：PERSONAL
- `ownerId`：注册用户 ID

### 数据库表

```sql
CREATE TABLE tenants (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_tenants_owner_id ON tenants(owner_id) WHERE deleted = FALSE;
```

### 事件监听器

```java
// package: com.aieducenter.tenant.application.event
@Component
public class UserRegisteredEventListener {
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        tenantAppService.createPersonalTenant(event.userId(), event.nickname());
    }
}
```

---

## 包结构

### Account 上下文（新增/变更）

```
com.aieducenter.account
├── application
│   ├── AccountRegistrationAppService.java
│   └── dto/
│       ├── RegisterByEmailCommand.java
│       └── RegisterResult.java
├── domain
│   ├── aggregate/User.java              // 补充密码强度校验 + registerByEmail
│   ├── event/
│   │   └── UserRegisteredEvent.java
│   └── repository/
│       └── UserRepository.java          // 新增 save(User) 声明
└── web/
    └── AccountController.java
```

### Tenant 上下文（新建）

```
com.aieducenter.tenant
├── domain
│   ├── aggregate/Tenant.java
│   ├── model/TenantType.java
│   ├── repository/TenantRepository.java
│   └── error/TenantError.java
├── application
│   ├── TenantAppService.java
│   └── event/
│       └── UserRegisteredEventListener.java
└── infrastructure
    └── persistence/
        └── SpringDataJpaTenantRepository.java
```

---

## 测试覆盖

### 单元测试

| 测试类 | 覆盖场景 |
|--------|---------|
| `UserTest`（补充） | 密码强度：弱密码抛 PASSWORD_WEAK、8位含字母数字通过、纯数字失败、纯字母失败、21位失败；`registerByEmail` 正确绑定 email |
| `AccountRegistrationAppServiceTest` | 成功注册（验证事件发布、token 返回）、邮箱重复、用户名重复、验证码错误（透传）、密码弱 |
| `TenantTest` | 创建 PERSONAL 租户，name/type/ownerId 正确，nickname 为空时 fallback |
| `TenantAppServiceTest` | createPersonalTenant 正常流程 |

`AccountRegistrationAppServiceTest` mock：`UserRepository`、`VerificationCodeAppService`、`ApplicationEventPublisher`、`AuthenticationService`。

### 集成测试

| 测试类 | 覆盖场景 |
|--------|---------|
| `AccountRegistrationIntegrationTest` | 完整注册流程（真实 Redis + PostgreSQL via Testcontainers）：DB 有 user + tenant 记录、token 有效、重复邮箱返回 409、弱密码返回 400 |

---

## 设计决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 领域事件发布 | ApplicationEventPublisher 手动发布 | User 继承 SoftDeletable，无法用 AbstractAggregateRoot.registerEvent() |
| 签发 Token | 直接注入 cartisan-security AuthenticationService | 避免重复抽象，测试时 mock 即可 |
| 邮箱格式校验 | 由 VerificationCodeAppService 负责，AppService 不重复校验 | 避免 VerificationCodeError 与 UserError 两套错误码混用 |
| 事件监听器事务 | @EventListener 同步，不加 @Transactional | 自动参与调用方 REQUIRED 事务，租户失败回滚整个注册 |
| Tenant ownerId | 直接字段，不引入 TenantMember 表 | 最小实现，多成员管理留 Epic 6 |
