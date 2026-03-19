# Feature: F02-03 用户注册 - 邮箱流程

> 版本：v1.3 | 日期：2026-03-19
> 状态：Design
> Epic：Epic 2 - 用户与登录

---

## 背景

F02-03 实现邮箱注册完整流程：验证码校验、密码强度校验、创建 User 聚合根、发布领域事件触发单人租户创建，最终返回 Sa-Token 登录凭证。

本 Feature 同时包含 F02-08（单人租户自动创建）的最小实现，通过领域事件解耦两个限界上下文。

---

## 目标

- 实现 POST /api/account/register/email 端点
- 注册时校验验证码、邮箱/用户名唯一性、密码强度
- 注册成功后自动创建 Personal 类型租户（via 领域事件）
- 返回 Sa-Token 凭证（`TokenInfo.token`），支持前端直接建立登录态
- 覆盖完整单元测试 + 集成测试

---

## 范围

### 包含（In Scope）

- `User` 聚合根：补充密码强度校验逻辑、新增 `registerByEmail` 工厂方法
- `UserRepository` 接口：新增 `save(User user)` 方法
- `UserRegisteredEvent` 领域事件（plain record，通过 Spring `ApplicationEventPublisher` 发布）
- `AccountRegistrationAppService`（邮箱注册应用服务，`@Transactional`，注入 `AuthenticationService`）
- `AccountController`（注册端点）
- `Tenant` 限界上下文最小实现（聚合根、Repository、AppService、JPA 适配）
- `UserRegisteredEventListener`（Tenant 上下文同步事件监听器，`@EventListener`）
- 单元测试 + 集成测试

### 不包含（Out of Scope）

- 手机号注册（F02-04）
- 登录接口（F02-05）
- 密码重置（F02-06）
- 前端页面（F02-10）
- 多成员租户管理（Epic 6）
- 头像上传（后续 Epic）

---

## 验收标准（Acceptance Criteria）

### AC1: User 聚合根变更

**1a. 密码强度校验（所有密码设置场景均适用）**

- 提取私有方法 `validatePasswordStrength(String plainPassword)`
- 规则：8-20 位，必须同时包含字母和数字
- 正则：`^(?=.*[a-zA-Z])(?=.*\d).{8,20}$`
- 不符合抛 `UserError.PASSWORD_WEAK`
- 在构造函数（`User(username, plainPassword, nickname, avatar)`）和 `updatePassword()` 中均调用此方法

**1b. `registerByEmail` 工厂方法**

```java
public static User registerByEmail(String username, String email,
                                   String plainPassword, String nickname)
```

- 调用现有构造函数创建 User（含密码强度校验 + BCrypt 加密）
- 之后调用 `updateEmail(email)` 绑定邮箱（含格式校验）
- 返回设置了 email 的 User 实例

**1c. `UserRepository` 新增 `save` 方法**

```java
// UserRepository 接口声明
User save(User user);
```

> `SpringDataJpaUserRepository extends BaseRepository<User, Long>, UserRepository`，`save` 已由 JPA 提供实现，无需额外代码，只需在接口中声明方法签名。

### AC2: 领域事件

```java
// package: com.aieducenter.account.domain.event
// 使用 plain record，通过 ApplicationEventPublisher 发布（不依赖 DomainEvent 基类）
// 原因：User 继承 SoftDeletable，不继承 AbstractAggregateRoot，无法使用 registerEvent()
public record UserRegisteredEvent(
    Long userId,
    String email,
    String nickname,
    java.time.Instant occurredAt
) {}
```

由 `AccountRegistrationAppService` 在 `userRepository.save(user)` 成功后调用 `applicationEventPublisher.publishEvent(event)` 发布。

### AC3: 注册接口

- 端点：`POST /api/account/register/email`
- 请求字段：`username`（必填）、`email`（必填）、`password`（必填）、`nickname`（可选）、`verificationCode`（必填）
- Controller 使用 `@Valid` + Bean Validation（`@NotBlank`）对必填字段做入参校验，空值返回 400
- 成功响应 200：`{ "code": "SUCCESS", "data": { "token": "xxx" } }`

### AC4: 注册流程正确性

`AccountRegistrationAppService.registerByEmail()` 标注 `@Transactional`，按序执行：

1. 调用 `VerificationCodeAppService.verifyCode(new VerifyCodeCommand(email, verificationCode, "REGISTER"))`
   - `VerifyCodeCommand` 字段顺序：`(String email, String code, String purpose)`
   - 失败 → 透传 `VerificationCodeError` 异常（含邮箱格式校验，AppService 不重复校验邮箱格式）
2. 检查 username 唯一性：`userRepository.existsByUsername(username)` → 抛 `UserError.USERNAME_ALREADY_EXISTS`（409）
3. 检查 email 唯一性：`userRepository.existsByEmail(email)` → 抛 `UserError.EMAIL_ALREADY_EXISTS`（409）
4. 创建 User：`User.registerByEmail(username, email, password, nickname)`（含密码强度校验 + BCrypt + 邮箱格式校验）
5. 持久化：`userRepository.save(user)`
6. 签发 Token：`TokenInfo tokenInfo = authenticationService.login(user.getId())`，返回 `tokenInfo.token()`
7. 发布事件：`applicationEventPublisher.publishEvent(new UserRegisteredEvent(...))`
8. 返回：`new RegisterResult(tokenInfo.token())`

**关于 `AuthenticationService`**：注入 `com.cartisan.security.authentication.AuthenticationService`（cartisan-security 提供），调用 `login(Long loginId)` 返回 `TokenInfo`（含 `token`、`loginId`、`expireTime`）。无需自定义 TokenService 接口。

### AC5: 自动创建租户（F02-08）

```java
// package: com.aieducenter.tenant.application.event
@Component
public class UserRegisteredEventListener {

    // @EventListener 同步执行，运行在调用方（AccountRegistrationAppService）的同一线程
    // 自动参与调用方的 @Transactional 事务（默认 REQUIRED 传播）
    // 租户创建失败会导致整个注册事务回滚，无需在此额外声明 @Transactional
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        tenantAppService.createPersonalTenant(event.userId(), event.nickname());
    }
}
```

租户字段：

| 字段 | 值 |
|------|----|
| name | nickname（为空时使用 username） |
| type | PERSONAL |
| ownerId | 注册用户 ID |

### AC6: 错误响应

| 场景 | HTTP Status | 错误码来源 |
|------|-------------|-----------|
| 必填字段为空 | 400 | Bean Validation（`@NotBlank`） |
| 验证码错误 / 邮箱格式错误 | 400 | `VerificationCodeError`（step 1 透传） |
| 验证码已过期 | 400 | `VerificationCodeError` |
| 验证码已使用 | 400 | `VerificationCodeError` |
| 密码强度不足 | 400 | `UserError.PASSWORD_WEAK` |
| 用户名已存在 | 409 | `UserError.USERNAME_ALREADY_EXISTS` |
| 邮箱已被使用 | 409 | `UserError.EMAIL_ALREADY_EXISTS` |

### AC7: 单元测试覆盖

| 测试类 | 覆盖场景 |
|--------|---------|
| `UserTest`（补充） | 密码强度：弱密码抛 PASSWORD_WEAK、8位含字母数字通过、纯数字失败、纯字母失败、21位失败；`registerByEmail` 正确绑定 email |
| `AccountRegistrationAppServiceTest` | 成功注册（验证事件发布、token 返回）、邮箱重复、用户名重复、验证码错误（透传）、密码弱 |
| `TenantTest` | 创建 PERSONAL 租户，name/type/ownerId 正确，nickname 为空时 fallback 到 username |
| `TenantAppServiceTest` | createPersonalTenant 正常流程 |

`AccountRegistrationAppServiceTest` 使用 Mockito mock：`UserRepository`、`VerificationCodeAppService`、`ApplicationEventPublisher`、`AuthenticationService`。

### AC8: 集成测试覆盖

| 测试类 | 覆盖场景 |
|--------|---------|
| `AccountRegistrationIntegrationTest` | 完整注册流程（真实 Redis + PostgreSQL via Testcontainers）：DB 有 user + tenant 记录、token 有效、重复邮箱返回 409、弱密码返回 400 |

---

## 设计决策

### 领域事件发布方式

`User` 当前继承 `SoftDeletable`，不继承 `AbstractAggregateRoot`，无法使用 `registerEvent()` 方法。因此采用 `ApplicationEventPublisher.publishEvent()` 在应用层手动发布领域事件，不依赖 `BaseRepository.save()` 的自动发布机制。

### 使用框架的 AuthenticationService

直接注入 cartisan-security 提供的 `AuthenticationService`，调用 `login(Long loginId)` 获取 `TokenInfo`。不创建自定义 `TokenService` 接口，避免重复抽象。测试时 mock `AuthenticationService` 即可。

### 邮箱格式校验归属

邮箱格式校验由 `VerificationCodeAppService.verifyCode`（step 1）负责，AppService 不重复校验，避免 `VerificationCodeError.EMAIL_INVALID` 与 `UserError.EMAIL_INVALID` 两套错误码混用。

### 事件监听器事务参与

`@EventListener`（同步）自动参与调用方事务（REQUIRED 传播），无需在监听器上额外声明 `@Transactional`。租户创建失败 → 整个注册事务回滚，保证数据一致性。

### Tenant 上下文最小实现

不引入 TenantMember 关联表，`ownerId` 字段满足"用户自动成为 OWNER"的需求。多成员管理留给 Epic 6。

---

## 包结构

### Account 上下文（新增/变更部分）

```
com.aieducenter.account
├── application
│   ├── AccountRegistrationAppService.java    # @Transactional，注入 AuthenticationService
│   └── dto/
│       ├── RegisterByEmailCommand.java        # @NotBlank 校验
│       └── RegisterResult.java
├── domain
│   ├── aggregate/User.java                   # 补充密码强度校验 + registerByEmail 工厂方法
│   ├── event/
│   │   └── UserRegisteredEvent.java          # plain record
│   └── repository/
│       └── UserRepository.java               # 新增 save(User) 方法声明
└── web/
    └── AccountController.java                # POST /api/account/register/email
```

### Tenant 上下文（新建）

```
com.aieducenter.tenant
├── domain
│   ├── aggregate/Tenant.java
│   ├── model/TenantType.java              # 枚举：PERSONAL
│   ├── repository/TenantRepository.java
│   └── error/TenantError.java
├── application
│   ├── TenantAppService.java
│   └── event/
│       └── UserRegisteredEventListener.java  # @EventListener，同步
└── infrastructure
    └── persistence/
        └── SpringDataJpaTenantRepository.java
```

---

## 数据模型

### tenants 表（新建）

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

---

## 依赖

### 前置依赖

| Feature | 依赖内容 |
|---------|---------|
| F02-01 | User 聚合根、UserRepository、UserError |
| F02-02 | VerificationCodeAppService.verifyCode、VerifyCodeCommand(email, code, purpose)、VerificationCodeError |
| F02-09 | AuthenticationService（cartisan-security）、公开路径配置（/api/account/register/**） |

### 被依赖

| Feature | 依赖内容 |
|---------|---------|
| F02-05 | AccountController（同 Controller 类复用）、AuthenticationService |
| F02-10 | POST /api/account/register/email 接口契约 |
