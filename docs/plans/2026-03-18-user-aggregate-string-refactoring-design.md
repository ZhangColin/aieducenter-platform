# User 聚合根从值对象迁移到 String 类型

> 日期：2026-03-18
> 状态：设计已批准
> 参考：F02-01 User 聚合根与领域模型

---

## 背景

F02-01 文档已更新，决定采用更简单的设计：User 聚合根直接使用 String 类型存储 `username`/`email`/`phoneNumber`，验证逻辑使用 hutool 工具类。当前实现使用了值对象（Username、Email、PhoneNumber），需要重构以匹配新设计。

---

## 设计决策

### 字段类型变更

| 字段 | 变更前 | 变更后 |
|------|--------|--------|
| username | `Username` | `String` |
| email | `Email` | `String` |
| phoneNumber | `PhoneNumber` | `String` |

### 验证策略

| 字段 | 验证方式 | 错误码 |
|------|----------|--------|
| username | 正则 `^[a-zA-Z][a-zA-Z0-9_]{2,19}$` | `USERNAME_INVALID` |
| email | hutool `Validator.isEmail()` | `EMAIL_INVALID` |
| phoneNumber | hutool `Validator.isMobile()` | `PHONE_NUMBER_INVALID` |

验证位置：构造函数、`updateUsername()`、`updateEmail()`、`updatePhoneNumber()`

---

## 变更范围

### 删除的文件

```
server/src/main/java/com/aieducenter/account/domain/valueobject/
├── Email.java
├── Username.java
└── PhoneNumber.java

server/src/test/java/com/aieducenter/account/domain/valueobject/
├── EmailTest.java
├── UsernameTest.java
└── PhoneNumberTest.java
```

### 修改的文件

```
server/src/main/java/com/aieducenter/account/domain/aggregate/
└── User.java

server/src/test/java/com/aieducenter/account/domain/aggregate/
└── UserTest.java
```

---

## User.java 核心变更

### 字段声明

```java
// 变更前
private Username username;
private Email email;
private PhoneNumber phoneNumber;

// 变更后
private String username;
private String email;
private String phoneNumber;
```

### Getter 方法

```java
// 变更前
public String getUsername() {
    return username.value();
}

public Optional<String> getEmail() {
    return Optional.ofNullable(email).map(Email::value);
}

public Optional<String> getPhoneNumber() {
    return Optional.ofNullable(phoneNumber).map(PhoneNumber::value);
}

// 变更后
public String getUsername() {
    return username;
}

public Optional<String> getEmail() {
    return Optional.ofNullable(email);
}

public Optional<String> getPhoneNumber() {
    return Optional.ofNullable(phoneNumber);
}
```

### 构造函数验证

```java
public User(String username, String plainPassword, String nickname, String avatar) {
    // username 格式验证
    if (!isValidUsername(username)) {
        throw new DomainException(UserError.USERNAME_INVALID);
    }
    this.username = username;

    this.password = PASSWORD_ENCODER.encode(plainPassword);
    this.nickname = (nickname == null || nickname.isBlank()) ? username : nickname;
    this.avatar = avatar;
    this.email = null;
    this.phoneNumber = null;
}

private boolean isValidUsername(String value) {
    return value.matches("^[a-zA-Z][a-zA-Z0-9_]{2,19}$");
}
```

### 更新方法验证

```java
public void updateEmail(String email) {
    if (email != null && !Validator.isEmail(email)) {
        throw new DomainException(UserError.EMAIL_INVALID);
    }
    this.email = email;
}

public void updatePhoneNumber(String phoneNumber) {
    if (phoneNumber != null && !Validator.isMobile(phoneNumber)) {
        throw new DomainException(UserError.PHONE_NUMBER_INVALID);
    }
    this.phoneNumber = phoneNumber;
}
```

---

## UserTest.java 测试变更

需要更新的测试用例：

1. **创建用户测试**：移除对值对象构造的依赖
2. **Getter 测试**：直接断言 String 值
3. **更新方法测试**：验证 hutool 格式校验行为
4. **restore() 方法**：简化为直接设置 String 字段

---

## 实施方案

采用**方案 A：一次性重构**

1. 修改 User.java，替换字段类型，添加 hutool 验证
2. 同步修改 UserTest.java
3. 删除值对象类及其测试
4. 运行完整测试套件验证

---

## 验收标准

- [ ] User.java 字段类型全部改为 String
- [ ] 验证逻辑使用 hutool Validator
- [ ] 所有测试绿灯通过
- [ ] 值对象类已删除
- [ ] ArchUnit 检查通过
