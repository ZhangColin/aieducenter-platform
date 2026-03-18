# Feature: F02-01 User 聚合根与领域模型 — 接口契约

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 2 完成

---

## 技术方案

### 类与接口职责

| 类/接口 | 职责 | 说明 |
|---------|------|------|
| `User` | 聚合根 + JPA 实体 | 封装用户状态和行为，含 JPA 注解 |
| `UserRepository` | 仓储接口 | 用户持久化抽象 |
| `UserError` | 错误码枚举 | 用户相关错误定义 |
| `JpaUserRepository` | 仓储实现 | UserRepository 的 JPA 实现 |

### 依赖关系

```
┌─────────────────────────────────────────────────────────┐
│                    domain layer                         │
│                  ┌──────────────────┐                   │
│                  │      User        │                   │
│                  │ (AggregateRoot)  │                   │
│                  │     + @Entity    │                   │
│                  └────────┬─────────┘                   │
│                           │                             │
│                  ┌────────▼────────┐                    │
│                  │ UserRepository  │                    │
│                  │   (interface)   │                    │
│                  └────────┬────────┘                    │
└──────────────────────────┼─────────────────────────────┘
                           │ Port
┌──────────────────────────▼─────────────────────────────┐
│                infrastructure layer                     │
│                  ┌────────────────┐                     │
│                  │JpaUserRepository│                    │
│                  │   (implements)  │                     │
│                  └────────────────┘                     │
└─────────────────────────────────────────────────────────┘
```

> **设计说明**：User 聚合根直接使用 String 类型存储 username/email/phoneNumber，验证逻辑在构造函数和更新方法中使用 hutool 工具类完成。不需要单独的值对象类。

---

## 领域接口描述（伪代码）

### 1. User 聚合根

```java
// 聚合根：用户（同时作为 JPA 实体）
@Entity
@Table(name = "users")
aggregate User extends SoftDeletable {
    // 字段
    @Id
    @Column(name = "id")
    field: Long id                      // TSID 生成

    @Column(name = "username", nullable = false, length = 20, unique = true)
    field: String username              // 必填，3-20位字母开头

    @Column(name = "email", length = 255, unique = true)
    field: String email                 // 可选，标准邮箱格式

    @Column(name = "phone_number", length = 20, unique = true)
    field: String phoneNumber           // 可选，中国大陆手机号

    @Column(name = "password", nullable = false, length = 255)
    field: String password              // BCrypt hash

    @Column(name = "nickname", length = 50)
    field: String nickname              // 可选，空则=username

    @Column(name = "avatar", length = 512)
    field: String avatar                // 可选

    // 依赖（注入构造函数）
    dependency: PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10)

    // 构造函数：创建新用户
    constructor(String username, String plainPassword, String nickname, String avatar) {
        // 验证用户名格式：3-20 位，字母开头
        if (!isValidUsername(username)) {
            throw DomainException(UserError.USERNAME_INVALID)
        }
        this.username = username

        // 密码加密
        this.password = passwordEncoder.encode(plainPassword)

        // nickname 为空则设置为 username
        this.nickname = (nickname == null || nickname.isBlank()) ? username : nickname
        this.avatar = avatar
        this.email = null
        this.phoneNumber = null
    }

    // 私有验证方法：用户名格式
    private isValidUsername(String value): boolean {
        return value.matches("^[a-zA-Z][a-zA-Z0-9_]{2,19}$")
    }

    // 方法：验证密码
    method matchesPassword(String plainPassword): boolean {
        return passwordEncoder.matches(plainPassword, this.password)
    }

    // 方法：修改用户名（含格式验证）
    method updateUsername(String newUsername) {
        if (!isValidUsername(newUsername)) {
            throw DomainException(UserError.USERNAME_INVALID)
        }
        this.username = newUsername
    }

    // 方法：修改邮箱（含格式验证，使用 hutool）
    method updateEmail(String email) {
        if (email != null && !Validator.isEmail(email)) {
            throw DomainException(UserError.EMAIL_INVALID)
        }
        this.email = email
    }

    // 方法：修改手机号（含格式验证，使用 hutool）
    method updatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !Validator.isMobile(phoneNumber)) {
            throw DomainException(UserError.PHONE_NUMBER_INVALID)
        }
        this.phoneNumber = phoneNumber
    }

    // 方法：修改昵称
    method updateNickname(String nickname) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname
        }
    }

    // 方法：修改头像
    method updateAvatar(String avatar) {
        this.avatar = avatar
    }

    // 方法：修改密码
    method updatePassword(String oldPassword, String newPassword) {
        if (!matchesPassword(oldPassword)) {
            throw DomainException(UserError.PASSWORD_INCORRECT)
        }
        this.password = passwordEncoder.encode(newPassword)
    }

    // JPA @PrePersist 生成 TSID
    @PrePersist
    method prePersist() {
        if (this.id == null) {
            this.id = TsidGenerator.newInstance().generate()
        }
    }

    // Getter
    method getId(): Long
    method getUsername(): String
    method getEmail(): String
    method getPhoneNumber(): String
    method getNickname(): String
    method getAvatar(): String
}
```

### 2. UserRepository 接口

```java
// 仓储接口：用户持久化
port UserRepository extends BaseRepository<User, Long> {
    // 唯一性检查
    method existsByUsername(String username): boolean
    method existsByEmail(String email): boolean
    method existsByPhoneNumber(String phoneNumber): boolean

    // 查询方法
    method findByUsername(String username): Optional<User>
    method findByEmail(String email): Optional<User>
    method findByPhoneNumber(String phoneNumber): Optional<User>
}
```

### 3. UserError 错误码

```java
// 错误码枚举
enum UserError implements CodeMessage {
    // 格式校验错误（400）
    USERNAME_INVALID(400, "USER_001", "用户名格式不正确"),
    EMAIL_INVALID(400, "USER_002", "邮箱格式不正确"),
    PHONE_NUMBER_INVALID(400, "USER_003", "手机号格式不正确"),

    // 唯一性错误（409）
    USERNAME_ALREADY_EXISTS(409, "USER_004", "用户名已存在"),
    EMAIL_ALREADY_EXISTS(409, "USER_005", "邮箱已被使用"),
    PHONE_NUMBER_ALREADY_EXISTS(409, "USER_006", "手机号已被使用"),

    // 密码错误（400）
    PASSWORD_INCORRECT(400, "USER_007", "密码错误"),
    PASSWORD_WEAK(400, "USER_008", "密码强度不足"),
    PASSWORD_SAME_AS_OLD(400, "USER_009", "新密码不能与旧密码相同"),

    // 资源不存在（404）
    USER_NOT_FOUND(404, "USER_010", "用户不存在");

    field: int httpStatus
    field: String code
    field: String message
}
```

---

## 核心流程

### 创建用户流程

```java
// 应用层伪代码
applicationService {
    method createUser(command: CreateUserCommand): Long {
        // 1. 唯一性校验（应用层）
        if (userRepository.existsByUsername(command.username())) {
            throw DomainException(UserError.USERNAME_ALREADY_EXISTS)
        }
        if (command.email() != null) {
            if (userRepository.existsByEmail(command.email())) {
                throw DomainException(UserError.EMAIL_ALREADY_EXISTS)
            }
        }

        // 2. 创建聚合根
        user = new User(
            command.username(),
            command.password(),
            command.nickname(),
            command.avatar()
        )

        // 3. 持久化
        userRepository.save(user)

        return user.getId()
    }
}
```

### 密码验证流程

```java
// User 聚合根内部
method matchesPassword(plainPassword: String): boolean {
    // BCrypt 验证：
    // - 从 hash 字符串中提取盐值
    // - 用相同盐值加密明文
    // - 比较结果
    return passwordEncoder.matches(plainPassword, this.password)
}
```

---

## 数据库变更

### users 表

```sql
-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(20) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(512),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 索引
CREATE INDEX idx_users_username ON users(username) WHERE deleted = FALSE;
CREATE INDEX idx_users_email ON users(email) WHERE deleted = FALSE AND email IS NOT NULL;
CREATE INDEX idx_users_phone ON users(phone_number) WHERE deleted = FALSE AND phone_number IS NOT NULL;

-- 注释
COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户ID（TSID）';
COMMENT ON COLUMN users.username IS '用户名（必填，登录凭证）';
COMMENT ON COLUMN users.email IS '邮箱（可选，登录凭证）';
COMMENT ON COLUMN users.phone_number IS '手机号（可选，登录凭证）';
COMMENT ON COLUMN users.password IS '密码（BCrypt hash）';
COMMENT ON COLUMN users.nickname IS '昵称（显示名称）';
COMMENT ON COLUMN users.avatar IS '头像URL';
COMMENT ON COLUMN users.deleted IS '软删除标记';
```

---

## 技术选型

| 技术点 | 选项 | 理由 |
|--------|------|------|
| 密码加密 | BCrypt | 行业标准，自动加盐，strength=10 |
| JPA 继承 | SoftDeletable | 包含审计字段 + 软删除 |
| ID 生成 | TSID | 时间排序、分布式唯一 |
| 格式校验 | hutool Validator | 成熟工具库，支持邮箱/手机号等验证 |
| 用户名校验 | 自定义正则 | 3-20位，字母开头 |

---

## 依赖说明

### Maven 依赖

```kotlin
// server/build.gradle.kts
dependencies {
    // 已有
    implementation("com.cartisan:cartisan-core:0.1.0-SNAPSHOT")
    implementation("com.cartisan:cartisan-web:0.1.0-SNAPSHOT")

    // 新增
    implementation("com.cartisan:cartisan-data-jpa:0.1.0-SNAPSHOT")
    implementation("org.springframework.security:spring-security-crypto:6.3.0")
    implementation("cn.hutool:hutool-core:5.8.+")  // 用于验证

    // 测试
    testImplementation("com.cartisan:cartisan-test:0.1.0-SNAPSHOT")
}
```

### PasswordEncoder

使用 Spring Security Crypto 的 `BCryptPasswordEncoder`：

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// 实例化（strength=10，默认）
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

// 加密
String hash = encoder.encode("plainPassword");

// 验证
boolean matches = encoder.matches("plainPassword", hash);
```
