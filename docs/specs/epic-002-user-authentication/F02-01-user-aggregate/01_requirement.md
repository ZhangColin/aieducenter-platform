# Feature: F02-01 User 聚合根与领域模型

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 1 完成
> Epic：Epic 2 - 用户与登录

---

## 背景

用户是 AI 教育云平台的核心实体，所有业务功能（AI 对话、计费、智能体等）都需要基于用户身份进行。本 Feature 建立 User 聚合根及相关领域模型，为后续的用户注册、登录、个人信息管理等功能提供领域基础。

---

## 目标

- 建立 User 聚合根，包含用户的核心属性和行为
- 定义 Username、Email、PhoneNumber 值对象，封装格式校验逻辑
- 定义 UserRepository 接口，规定用户持久化边界
- 提供密码加密存储和验证能力
- 覆盖完整的单元测试

---

## 范围

### 包含（In Scope）

- User 聚合根（领域模型）
- Username、Email、PhoneNumber 值对象
- UserRepository 接口定义
- UserError 错误码枚举
- 值对象和聚合根的单元测试
- UserRepository 的集成测试

### 不包含（Out of Scope）

- ApplicationService 层（F02-03/F02-04）
- Controller 层（F02-10）
- 用户注册/登录业务逻辑（F02-03~F02-06）
- 密码重置功能（F02-06）
- 对象存储/头像上传（后续 Epic）

---

## 验收标准（Acceptance Criteria）

### AC1: User 聚合根结构

User 聚合根包含以下字段：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `Long` | ✅ | TSID 生成 |
| `username` | `Username` | ✅ | 登录凭证，唯一 |
| `email` | `Optional<Email>` | ❌ | 可选登录凭证 |
| `phoneNumber` | `Optional<PhoneNumber>` | ❌ | 可选登录凭证 |
| `password` | `String` | ✅ | BCrypt hash |
| `nickname` | `String` | ❌ | 显示名称，空则=username |
| `avatar` | `Optional<String>` | ❌ | 头像 URL |
| `createdAt` | `LocalDateTime` | ✅ | 审计字段 |
| `updatedAt` | `LocalDateTime` | ✅ | 审计字段 |

### AC2: Username 值对象

- 格式：3-20 位，字母开头，允许字母/数字/下划线
- 正则：`^[a-zA-Z][a-zA-Z0-9_]{2,19}$`
- 构造函数中校验格式，不符合抛 `UserError.USERNAME_INVALID`

### AC3: Email 值对象

- 标准邮箱格式校验
- 使用 Jakarta Validation 的 `@Email` 或 Apache Commons Validation
- 构造函数中校验格式，不符合抛 `UserError.EMAIL_INVALID`

### AC4: PhoneNumber 值对象

- 中国大陆手机号格式：1 开头，第二位 3-9，后 9 位数字
- 正则：`^1[3-9]\d{9}$`
- 构造函数中校验格式，不符合抛 `UserError.PHONE_NUMBER_INVALID`

### AC5: 密码加密

- 密码使用 BCrypt 加密存储（strength=10）
- 提供 `matchesPassword(String plainPassword)` 方法验证密码
- 构造函数接受明文密码并加密

### AC6: UserRepository 接口

- 继承 `BaseRepository<User, Long>`
- 提供以下查询方法：
  - `boolean existsByUsername(String username)`
  - `boolean existsByEmail(String email)`
  - `boolean existsByPhoneNumber(String phoneNumber)`
  - `Optional<User> findByUsername(String username)`
  - `Optional<User> findByEmail(String email)`
  - `Optional<User> findByPhoneNumber(String phoneNumber)`

### AC7: User 聚合根行为

```java
// 构造函数 - 创建新用户
User(String username, String plainPassword, String nickname, String avatar)

// 密码验证
boolean matchesPassword(String plainPassword)

// 修改用户名（应用层需先校验唯一性）
void updateUsername(String newUsername)

// 修改个人信息
void updateNickname(String nickname)
void updateAvatar(String avatar)

// 修改密码
void updatePassword(String oldPassword, String newPassword)
```

### AC8: 单元测试覆盖

| 测试类 | 覆盖场景 |
|--------|----------|
| `UsernameTest` | 格式正确、太短、数字开头、特殊字符、值相等 |
| `EmailTest` | 格式正确、格式错误、值相等 |
| `PhoneNumberTest` | 格式正确、非1开头、第二位非法、值相等 |
| `UserTest` | 创建、nickname默认、密码匹配/不匹配、修改各字段 |

### AC9: Repository 集成测试

- 继承 `IntegrationTestBase`
- 覆盖：保存、按username/email/phone查询、exists检查、软删除

---

## 约束

### 技术约束

- Java 21 + Spring Boot 3.4
- 使用 cartisan-boot 框架（cartisan-core, cartisan-data-jpa）
- JPA + PostgreSQL
- 测试使用 JUnit 5 + AssertJ + Testcontainers

### 架构约束

- 遵循 DDD 六边形架构
- 领域层不依赖基础设施层
- 格式校验在领域层（值对象构造函数）
- 唯一性校验在应用层（Repository exists 方法）

### 代码规范

- 测试命名：`should_{预期结果}_when_{条件}`
- 使用 AssertJ 断言
- Record 实现 ValueObject
- 公共接口必须包含 JavaDoc

---

## 依赖

### 前置依赖

- cartisan-boot v0.1.0-SNAPSHOT 已发布到本地 Maven
- PostgreSQL 数据库可用

### 被依赖

| Feature | 依赖内容 |
|---------|----------|
| F02-02 | - |
| F02-03 | User 聚合根、UserRepository |
| F02-04 | User 聚合根、UserRepository |
| F02-05 | User 聚合根、matchesPassword 方法 |
| F02-06 | User 聚合根、updatePassword 方法 |
| F02-07 | User 聚合根 |
| F02-08 | User ID 作为租户 OWNER |

---

## 数据模型

### users 表

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(512),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_users_username ON users(username) WHERE deleted = FALSE;
CREATE INDEX idx_users_email ON users(email) WHERE deleted = FALSE AND email IS NOT NULL;
CREATE INDEX idx_users_phone ON users(phone_number) WHERE deleted = FALSE AND phone_number IS NOT NULL;
```

---

## 错误码

| 错误码 | HTTP Status | 触发条件 |
|--------|-------------|----------|
| USERNAME_INVALID | 400 | 用户名格式不正确 |
| EMAIL_INVALID | 400 | 邮箱格式不正确 |
| PHONE_NUMBER_INVALID | 400 | 手机号格式不正确 |
| USERNAME_ALREADY_EXISTS | 409 | 用户名已存在 |
| EMAIL_ALREADY_EXISTS | 409 | 邮箱已被使用 |
| PHONE_NUMBER_ALREADY_EXISTS | 409 | 手机号已被使用 |
| PASSWORD_INCORRECT | 400 | 密码错误 |
| PASSWORD_WEAK | 400 | 密码强度不足 |
| PASSWORD_SAME_AS_OLD | 400 | 新密码不能与旧密码相同 |
| USER_NOT_FOUND | 404 | 用户不存在 |
