# User 聚合根 String 类型重构实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 User 聚合根从使用值对象（Username、Email、PhoneNumber）重构为直接使用 String 类型，验证逻辑改用 hutool 工具类。

**Architecture:** 一次性重构方案：先修改 User.java 添加 hutool 验证逻辑并替换字段类型，同步修改 UserTest.java，最后删除值对象类及其测试。

**Tech Stack:** Java 21, Spring Boot 3.4, hutool-core 5.8.x, JUnit 5, AssertJ

**依赖参考:** `docs/specs/epic-002-user-authentication/F02-01-user-aggregate/`

---

## Task 1: 添加 hutool 依赖（如未添加）

**Files:**
- Check: `server/build.gradle.kts`

**Step 1: 检查 hutool 依赖是否存在**

运行: `grep hutool server/build.gradle.kts`

预期: 如果有输出说明依赖已存在，跳过此任务

---

## Task 2: 重构 User.java - 修改字段类型和添加验证逻辑

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/domain/aggregate/User.java`

**Step 1: 移除值对象导入**

删除以下 import 语句：
```java
import com.aieducenter.account.domain.valueobject.Email;
import com.aieducenter.account.domain.valueobject.PhoneNumber;
import com.aieducenter.account.domain.valueobject.Username;
```

添加 hutool 导入：
```java
import cn.hutool.core.lang.Validator;
```

**Step 2: 修改字段声明**

将以下字段：
```java
@Column(name = "username", nullable = false, length = 20, unique = true)
private Username username;

@Column(name = "email", length = 255, unique = true)
private Email email;

@Column(name = "phone_number", length = 20, unique = true)
private PhoneNumber phoneNumber;
```

改为：
```java
@Column(name = "username", nullable = false, length = 20, unique = true)
private String username;

@Column(name = "email", length = 255, unique = true)
private String email;

@Column(name = "phone_number", length = 20, unique = true)
private String phoneNumber;
```

**Step 3: 添加私有验证方法**

在 PASSWORD_ENCODER 常量后添加：
```java
/**
 * 验证用户名格式：3-20 位，字母开头，允许字母/数字/下划线
 */
private boolean isValidUsername(String value) {
    return value.matches("^[a-zA-Z][a-zA-Z0-9_]{2,19}$");
}
```

**Step 4: 修改构造函数添加验证**

将构造函数：
```java
public User(String username, String plainPassword, String nickname, String avatar) {
    this.username = new Username(username);
    // ... 其他代码
}
```

改为：
```java
public User(String username, String plainPassword, String nickname, String avatar) {
    // 验证用户名格式
    if (!isValidUsername(username)) {
        throw new DomainException(UserError.USERNAME_INVALID);
    }
    this.username = username;

    this.password = PASSWORD_ENCODER.encode(plainPassword);

    // 昵称为空则设置为用户名
    if (nickname == null || nickname.isBlank()) {
        this.nickname = username;
    } else {
        this.nickname = nickname;
    }

    this.avatar = avatar;
    this.email = null;
    this.phoneNumber = null;
}
```

**Step 5: 修改 getter 方法**

将以下方法：
```java
public String getUsername() {
    return username.value();
}

public Optional<String> getEmail() {
    return Optional.ofNullable(email).map(Email::value);
}

public Optional<String> getPhoneNumber() {
    return Optional.ofNullable(phoneNumber).map(PhoneNumber::value);
}
```

改为：
```java
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

**Step 6: 修改 updateUsername 方法**

将：
```java
public void updateUsername(String newUsername) {
    this.username = new Username(newUsername);
}
```

改为：
```java
public void updateUsername(String newUsername) {
    if (!isValidUsername(newUsername)) {
        throw new DomainException(UserError.USERNAME_INVALID);
    }
    this.username = newUsername;
}
```

**Step 7: 添加 updateEmail 方法**

在 `updateNickname` 方法前添加：
```java
/**
 * 修改邮箱（含格式验证）。
 *
 * @param email 新邮箱（null 则清空）
 * @throws DomainException 邮箱格式不正确时抛出
 */
public void updateEmail(String email) {
    if (email != null && !Validator.isEmail(email)) {
        throw new DomainException(UserError.EMAIL_INVALID);
    }
    this.email = email;
}
```

**Step 8: 添加 updatePhoneNumber 方法**

在 `updateEmail` 方法后添加：
```java
/**
 * 修改手机号（含格式验证）。
 *
 * @param phoneNumber 新手机号（null 则清空）
 * @throws DomainException 手机号格式不正确时抛出
 */
public void updatePhoneNumber(String phoneNumber) {
    if (phoneNumber != null && !Validator.isMobile(phoneNumber)) {
        throw new DomainException(UserError.PHONE_NUMBER_INVALID);
    }
    this.phoneNumber = phoneNumber;
}
```

**Step 9: 简化 restore 方法**

将 `restore` 静态方法：
```java
public static User restore(Long id, String username, String email, String phoneNumber,
                           String password, String nickname, String avatar) {
    User user = new User();
    user.id = id;
    user.username = new Username(username);
    user.email = email != null ? new Email(email) : null;
    user.phoneNumber = phoneNumber != null ? new PhoneNumber(phoneNumber) : null;
    user.password = password;
    user.nickname = nickname;
    user.avatar = avatar;
    return user;
}
```

改为：
```java
public static User restore(Long id, String username, String email, String phoneNumber,
                           String password, String nickname, String avatar) {
    User user = new User();
    user.id = id;
    user.username = username;
    user.email = email;
    user.phoneNumber = phoneNumber;
    user.password = password;
    user.nickname = nickname;
    user.avatar = avatar;
    return user;
}
```

**Step 10: 编译验证**

运行: `./gradlew compileJava`

预期: BUILD SUCCESSFUL

**Step 11: 提交**

```bash
git add server/src/main/java/com/aieducenter/account/domain/aggregate/User.java
git commit -m "refactor(user): replace value objects with String type and hutool validation"
```

---

## Task 3: 更新 UserTest.java - 移除值对象导入

**Files:**
- Modify: `server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java`

**Step 1: 删除值对象导入**

删除以下 import 语句：
```java
import com.aieducenter.account.domain.valueobject.Email;
import com.aieducenter.account.domain.valueobject.PhoneNumber;
import com.aieducenter.account.domain.valueobject.Username;
```

**Step 2: 编译验证**

运行: `./gradlew compileTestJava`

预期: BUILD SUCCESSFUL

**Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java
git commit -m "refactor(test): remove value object imports from UserTest"
```

---

## Task 4: 添加 username 格式验证测试

**Files:**
- Modify: `server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java`

**Step 1: 添加 username 验证测试**

在 `shouldSetNicknameToUsername_whenNicknameOnlySpaces` 测试后，`// ========== 密码验证 ==========` 注释前添加：

```java
// ========== 用户名格式验证 ==========

@Test
void shouldCreateUser_whenUsernameValid() {
    // When & Then - 字母开头
    assertThat(new User("abc", "password123", null, null).getUsername()).isEqualTo("abc");

    // When & Then - 字母开头，包含数字和下划线
    assertThat(new User("user_123", "password123", null, null).getUsername()).isEqualTo("user_123");

    // When & Then - 最大长度 20
    String maxUsername = "a" + "_".repeat(18) + "b";
    assertThat(new User(maxUsername, "password123", null, null).getUsername()).hasSize(20);
}

@Test
void shouldThrow_whenUsernameStartsWithNumber() {
    // When & Then
    assertThatThrownBy(() -> new User("123invalid", "password123", null, null))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.USERNAME_INVALID);
}

@Test
void shouldThrow_whenUsernameStartsWithUnderscore() {
    // When & Then
    assertThatThrownBy(() -> new User("_invalid", "password123", null, null))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.USERNAME_INVALID);
}

@Test
void shouldThrow_whenUsernameTooShort() {
    // When & Then - 2 字符
    assertThatThrownBy(() -> new User("ab", "password123", null, null))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.USERNAME_INVALID);

    // When & Then - 1 字符
    assertThatThrownBy(() -> new User("a", "password123", null, null))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.USERNAME_INVALID);
}

@Test
void shouldThrow_whenUsernameTooLong() {
    // When & Then - 21 字符
    assertThatThrownBy(() -> new User("a".repeat(21), "password123", null, null))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.USERNAME_INVALID);
}

@Test
void shouldThrow_whenUsernameContainsInvalidChars() {
    // When & Then - 包含特殊字符
    assertThatThrownBy(() -> new User("user@name", "password123", null, null))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.USERNAME_INVALID);
}
```

**Step 2: 运行测试验证红灯**

运行: `./gradlew test --tests UserTest`

预期: 部分测试红灯（因为 Username 值对象已有自己的验证，但需要确认新测试覆盖）

**Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java
git commit -m "test(user): add username format validation tests"
```

---

## Task 5: 添加 email 验证测试

**Files:**
- Modify: `server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java`

**Step 1: 添加 email 验证测试**

在文件末尾 `}` 前添加：

```java
// ========== Email 验证 ==========

@Test
void shouldUpdateEmail_whenEmailValid() {
    // Given
    User user = new User("john_doe", "password123", null, null);

    // When
    user.updateEmail("john@example.com");

    // Then
    assertThat(user.getEmail()).isPresent();
    assertThat(user.getEmail().get()).isEqualTo("john@example.com");
}

@Test
void shouldClearEmail_whenEmailNull() {
    // Given
    User user = new User("john_doe", "password123", null, null);
    user.updateEmail("john@example.com");

    // When
    user.updateEmail(null);

    // Then
    assertThat(user.getEmail()).isEmpty();
}

@Test
void shouldThrow_whenEmailInvalid() {
    // Given
    User user = new User("john_doe", "password123", null, null);

    // When & Then - 缺少 @
    assertThatThrownBy(() -> user.updateEmail("invalidemail"))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.EMAIL_INVALID);

    // When & Then - 缺少域名
    assertThatThrownBy(() -> user.updateEmail("invalid@"))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.EMAIL_INVALID);
}

@Test
void shouldUpdateEmail_whenEmailWithSubdomain() {
    // Given
    User user = new User("john_doe", "password123", null, null);

    // When
    user.updateEmail("john@mail.example.com");

    // Then
    assertThat(user.getEmail()).isPresent();
    assertThat(user.getEmail().get()).isEqualTo("john@mail.example.com");
}
```

**Step 2: 运行测试验证红灯**

运行: `./gradlew test --tests UserTest.shouldUpdateEmail_whenEmailValid`

预期: FAIL (updateEmail 方法不存在)

**Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java
git commit -m "test(user): add email validation tests"
```

---

## Task 6: 添加 phoneNumber 验证测试

**Files:**
- Modify: `server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java`

**Step 1: 添加 phoneNumber 验证测试**

在文件末尾 `}` 前添加：

```java
// ========== PhoneNumber 验证 ==========

@Test
void shouldUpdatePhoneNumber_whenPhoneValid() {
    // Given
    User user = new User("john_doe", "password123", null, null);

    // When
    user.updatePhoneNumber("13812345678");

    // Then
    assertThat(user.getPhoneNumber()).isPresent();
    assertThat(user.getPhoneNumber().get()).isEqualTo("13812345678");
}

@Test
void shouldClearPhoneNumber_whenPhoneNull() {
    // Given
    User user = new User("john_doe", "password123", null, null);
    user.updatePhoneNumber("13812345678");

    // When
    user.updatePhoneNumber(null);

    // Then
    assertThat(user.getPhoneNumber()).isEmpty();
}

@Test
void shouldThrow_whenPhoneNumberInvalid() {
    // Given
    User user = new User("john_doe", "password123", null, null);

    // When & Then - 不是 1 开头
    assertThatThrownBy(() -> user.updatePhoneNumber("23812345678"))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.PHONE_NUMBER_INVALID);

    // When & Then - 第二位不是 3-9
    assertThatThrownBy(() -> user.updatePhoneNumber("10812345678"))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.PHONE_NUMBER_INVALID);

    // When & Then - 少于 11 位
    assertThatThrownBy(() -> user.updatePhoneNumber("1381234567"))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.PHONE_NUMBER_INVALID);

    // When & Then - 多于 11 位
    assertThatThrownBy(() -> user.updatePhoneNumber("138123456789"))
        .isInstanceOf(DomainException.class)
        .extracting("codeMessage")
        .isEqualTo(UserError.PHONE_NUMBER_INVALID);
}

@Test
void shouldUpdatePhoneNumber_whenPhoneWithValidSecondDigit() {
    // Given
    User user = new User("john_doe", "password123", null, null);

    // When - 第二位是 3
    user.updatePhoneNumber("13123456789");
    assertThat(user.getPhoneNumber().get()).isEqualTo("13123456789");

    // When - 第二位是 9
    user.updatePhoneNumber("19123456789");
    assertThat(user.getPhoneNumber().get()).isEqualTo("19123456789");
}
```

**Step 2: 运行测试验证红灯**

运行: `./gradlew test --tests UserTest.shouldUpdatePhoneNumber_whenPhoneValid`

预期: FAIL (updatePhoneNumber 方法不存在)

**Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java
git commit -m "test(user): add phone number validation tests"
```

---

## Task 7: 确保所有测试绿灯

**Step 1: 运行完整测试套件**

运行: `./gradlew test --tests UserTest`

预期: 所有测试 PASS

**Step 2: 如有失败，调试并修复**

检查失败的测试，确认：
1. User.java 的 updateEmail 和 updatePhoneNumber 方法已添加
2. 验证逻辑正确使用 hutool 的 Validator.isEmail() 和 Validator.isMobile()

**Step 3: 确认绿灯后不提交**（下一个任务再提交）

---

## Task 8: 删除值对象类

**Files:**
- Delete: `server/src/main/java/com/aieducenter/account/domain/valueobject/Email.java`
- Delete: `server/src/main/java/com/aieducenter/account/domain/valueobject/Username.java`
- Delete: `server/src/main/java/com/aieducenter/account/domain/valueobject/PhoneNumber.java`

**Step 1: 删除值对象类**

运行:
```bash
rm server/src/main/java/com/aieducenter/account/domain/valueobject/Email.java
rm server/src/main/java/com/aieducenter/account/domain/valueobject/Username.java
rm server/src/main/java/com/aieducenter/account/domain/valueobject/PhoneNumber.java
```

**Step 2: 编译验证**

运行: `./gradlew compileJava`

预期: BUILD SUCCESSFUL

**Step 3: 提交**

```bash
git add -A
git commit -m "refactor(user): delete value object classes (Email, Username, PhoneNumber)"
```

---

## Task 9: 删除值对象测试类

**Files:**
- Delete: `server/src/test/java/com/aieducenter/account/domain/valueobject/EmailTest.java`
- Delete: `server/src/test/java/com/aieducenter/account/domain/valueobject/UsernameTest.java`
- Delete: `server/src/test/java/com/aieducenter/account/domain/valueobject/PhoneNumberTest.java`

**Step 1: 删除值对象测试类**

运行:
```bash
rm server/src/test/java/com/aieducenter/account/domain/valueobject/EmailTest.java
rm server/src/test/java/com/aieducenter/account/domain/valueobject/UsernameTest.java
rm server/src/test/java/com/aieducenter/account/domain/valueobject/PhoneNumberTest.java
```

**Step 2: 运行完整测试套件**

运行: `./gradlew test`

预期: 所有测试 PASS

**Step 3: 提交**

```bash
git add -A
git commit -m "refactor(test): delete value object test classes"
```

---

## Task 10: 运行 ArchUnit 检查

**Step 1: 运行 ArchUnit 测试**

运行: `./gradlew test --tests "*ArchitectureTest"`

预期: 所有架构检查 PASS

**Step 2: 如有失败，修复问题**

如果有违反架构规则的错误，查看并修复：
1. 确认没有遗留的值对象引用
2. 确认依赖方向正确

**Step 3: 提交**

```bash
git add .
git commit -m "refactor(arch): ensure ArchUnit checks pass after refactoring"
```

---

## Task 11: 删除空目录（可选）

**Step 1: 检查 valueobject 目录是否为空**

运行: `ls -la server/src/main/java/com/aieducenter/account/domain/valueobject/`
运行: `ls -la server/src/test/java/com/aieducenter/account/domain/valueobject/`

**Step 2: 如果目录为空，删除目录**

运行:
```bash
rmdir server/src/main/java/com/aieducenter/account/domain/valueobject/
rmdir server/src/test/java/com/aieducenter/account/domain/valueobject/
```

**Step 3: 提交**

```bash
git add -A
git commit -m "refactor(user): remove empty valueobject directories"
```

---

## 验收标准

完成所有任务后，确认：

- [ ] `User.java` 字段类型全部为 `String`
- [ ] 验证逻辑使用 hutool `Validator.isEmail()` 和 `Validator.isMobile()`
- [ ] username 验证使用正则 `^[a-zA-Z][a-zA-Z0-9_]{2,19}$`
- [ ] 所有测试绿灯 (`./gradlew test`)
- [ ] ArchUnit 检查通过
- [ ] 值对象类已删除
- [ ] 值对象测试类已删除

---

## 参考文档

- 需求文档: `docs/specs/epic-002-user-authentication/F02-01-user-aggregate/01_requirement.md`
- 接口文档: `docs/specs/epic-002-user-authentication/F02-01-user-aggregate/02_interface.md`
- 实施文档: `docs/specs/epic-002-user-authentication/F02-01-user-aggregate/03_implementation.md`
- 设计文档: `docs/plans/2026-03-18-user-aggregate-string-refactoring-design.md`
