# Password Domain Service Implementation Plan (Corrected)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构 AdminUser 聚合根的密码处理逻辑，使用领域服务 + 南向接口模式，符合六边形架构和 DDD 设计原则。

**Architecture:**
- 领域服务 `PasswordEncoderService` 封装密码编码和验证逻辑
- 南向接口 `PasswordEncoderPort` 定义端口（使用 `PortType.CLIENT`）
- 基础设施适配器 `BCryptPasswordEncoderAdapter` 实现端口（标注 `@Component` 和 `@Adapter`）
- 应用服务层通过领域服务处理密码，聚合根只保存已加密的密码

**Tech Stack:** Java 21, Spring Boot 3.4, cartisan-boot framework (Core, Data-JPA), Spring Security BCrypt

---

## File Structure

```
admin/
├── domain/
│   ├── service/
│   │   └── PasswordEncoderService.java        [CREATE] 领域服务
│   ├── port/
│   │   └── PasswordEncoderPort.java           [CREATE] 南向接口 (PortType.CLIENT)
│   └── aggregate/
│       └── AdminUser.java                      [MODIFY] 移除 BCrypt 依赖
├── infrastructure/
│   └── BCryptPasswordEncoderAdapter.java      [CREATE] 基础设施适配器
├── application/
│   ├── AdminUserAuthAppService.java           [MODIFY] 使用领域服务
│   └── AdminUserManagementAppService.java     [MODIFY] 使用领域服务 + 密码强度验证
└── tests/
    ├── domain/aggregate/AdminUserTest.java    [MODIFY] 调整测试
    └── application/AdminUserAuthAppServiceTest.java [MODIFY] 调整测试
```

---

## Task 1: Create PasswordEncoderPort Interface

**Files:**
- Create: `server/src/main/java/com/aieducenter/admin/domain/port/PasswordEncoderPort.java`

- [ ] **Step 1: Create the port interface file**

```java
package com.aieducenter.admin.domain.port;

import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;

/**
 * 密码编码器端口。
 *
 * <p>南向接口，定义密码编码能力。</p>
 * <p>使用 PortType.CLIENT 表示这是对基础设施服务的端口。</p>
 *
 * @since 0.1.0
 */
@Port(PortType.CLIENT)
public interface PasswordEncoderPort {

    /**
     * 加密密码。
     *
     * @param plainPassword 明文密码
     * @return 加密后的密码
     */
    String encode(String plainPassword);

    /**
     * 验证密码。
     *
     * @param plainPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean matches(String plainPassword, String encodedPassword);
}
```

- [ ] **Step 2: Verify file compiles**

Run: `cd server && ./gradlew compileJava`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/port/PasswordEncoderPort.java
git commit -m "feat(admin): add PasswordEncoderPort interface with PortType.CLIENT"
```

---

## Task 2: Create PasswordEncoderService Domain Service

**Files:**
- Create: `server/src/main/java/com/aieducenter/admin/domain/service/PasswordEncoderService.java`

- [ ] **Step 1: Create the domain service file**

```java
package com.aieducenter.admin.domain.service;

import com.cartisan.core.stereotype.DomainService;
import com.aieducenter.admin.domain.port.PasswordEncoderPort;

/**
 * 密码编码领域服务。
 *
 * <p>负责密码加密和验证逻辑。</p>
 * <p>封装南向端口，提供领域层的密码处理能力。</p>
 *
 * @since 0.1.0
 */
@DomainService
public class PasswordEncoderService {

    private final PasswordEncoderPort encoder;

    public PasswordEncoderService(PasswordEncoderPort encoder) {
        this.encoder = encoder;
    }

    /**
     * 加密明文密码。
     *
     * @param plainPassword 明文密码
     * @return 加密后的密码
     */
    public String encodePassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    /**
     * 验证密码。
     *
     * @param plainPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean verifyPassword(String plainPassword, String encodedPassword) {
        return encoder.matches(plainPassword, encodedPassword);
    }
}
```

- [ ] **Step 2: Verify file compiles**

Run: `cd server && ./gradlew compileJava`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/service/PasswordEncoderService.java
git commit -m "feat(admin): add PasswordEncoderService domain service"
```

---

## Task 3: Create BCryptPasswordEncoderAdapter

**Files:**
- Create: `server/src/main/java/com/aieducenter/admin/infrastructure/BCryptPasswordEncoderAdapter.java`

- [ ] **Step 1: Create the infrastructure adapter file**

```java
package com.aieducenter.admin.infrastructure;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;
import com.aieducenter.admin.domain.port.PasswordEncoderPort;

/**
 * BCrypt 密码编码器适配器。
 *
 * <p>使用 Spring Security 的 BCryptPasswordEncoder 实现密码编码端口。</p>
 *
 * @since 0.1.0
 */
@Component("adminBCryptPasswordEncoder")
@Adapter(PortType.CLIENT)
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private static final int STRENGTH = 10;
    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordEncoderAdapter() {
        this.encoder = new BCryptPasswordEncoder(STRENGTH);
    }

    @Override
    public String encode(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    @Override
    public boolean matches(String plainPassword, String encodedPassword) {
        return encoder.matches(plainPassword, encodedPassword);
    }
}
```

Note: `@Component` annotation is required for Spring to discover this bean. The bean name "adminBCryptPasswordEncoder" avoids conflicts.

- [ ] **Step 2: Verify Spring can scan the infrastructure package**

Check: The main application class should scan `com.aieducenter` package, which includes `admin.infrastructure`.

Run: `cd server && ./gradlew compileJava`

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/aieducenter/admin/infrastructure/BCryptPasswordEncoderAdapter.java
git commit -m "feat(admin): add BCryptPasswordEncoderAdapter with @Component annotation"
```

---

## Task 4a: Remove BCrypt Imports and Fields from AdminUser

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java`

- [ ] **Step 1: Remove BCryptPasswordEncoder import**

Remove line:
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
```

- [ ] **Step 2: Remove PASSWORD_ENCODER static field**

Remove field:
```java
private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(10);
```

- [ ] **Step 3: Remove PASSWORD_PATTERN constant**

Remove constant (will be used in application service layer):
```java
private static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$";
```

- [ ] **Step 4: Verify file compiles**

Run: `cd server && ./gradlew compileJava`

Expected: BUILD SUCCESSFUL (but with compilation errors in methods that use PASSWORD_ENCODER)

- [ ] **Step 5: Commit**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java
git commit -m "refactor(admin): remove BCrypt imports and fields from AdminUser"
```

---

## Task 4b: Update AdminUser Constructor

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java`

- [ ] **Step 1: Update constructor to accept encoded password**

Change constructor from:
```java
/**
 * 创建管理员。
 *
 * @param username 用户名（必填）
 * @param plainPassword 明文密码
 * @param nickname 昵称
 */
public AdminUser(String username, String plainPassword, String nickname) {
    validateUsername(username);
    validatePasswordStrength(plainPassword);
    this.username = username;
    this.password = PASSWORD_ENCODER.encode(plainPassword);
    this.nickname = nickname != null && !nickname.isBlank() ? nickname : username;
    this.status = AdminUserStatus.ACTIVE;
}
```

To:
```java
/**
 * 创建管理员。
 *
 * @param username 用户名（必填）
 * @param encodedPassword 加密后的密码（应用服务层已加密）
 * @param nickname 昵称
 */
public AdminUser(String username, String encodedPassword, String nickname) {
    validateUsername(username);
    this.username = username;
    this.password = Objects.requireNonNull(encodedPassword, "encodedPassword cannot be null");
    this.nickname = nickname != null && !nickname.isBlank() ? nickname : username;
    this.status = AdminUserStatus.ACTIVE;
}
```

- [ ] **Step 2: Verify file compiles**

Run: `cd server && ./gradlew compileJava`

Expected: BUILD SUCCESSFUL (but still has errors in methods that use PASSWORD_ENCODER)

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java
git commit -m "refactor(admin): update AdminUser constructor to accept encoded password"
```

---

## Task 4c: Remove Password Methods and Add changePassword

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java`

- [ ] **Step 1: Remove validatePasswordStrength method**

Remove entire method:
```java
private void validatePasswordStrength(String plainPassword) {
    Assertions.require(
        plainPassword != null && plainPassword.matches(PASSWORD_PATTERN),
        AdminMessage.PASSWORD_WEAK
    );
}
```

Rationale: Password strength validation moves to application service layer.

- [ ] **Step 2: Remove matchesPassword method**

Remove entire method:
```java
/**
 * 验证密码。
 */
public boolean matchesPassword(String plainPassword) {
    return PASSWORD_ENCODER.matches(plainPassword, this.password);
}
```

Rationale: Password verification moves to application service layer via PasswordEncoderService.

- [ ] **Step 3: Remove updatePassword method**

Remove entire method:
```java
/**
 * 修改密码。
 */
public void updatePassword(String oldPassword, String newPassword) {
    Assertions.require(matchesPassword(oldPassword), AdminMessage.PASSWORD_INCORRECT);
    validatePasswordStrength(newPassword);
    this.password = PASSWORD_ENCODER.encode(newPassword);
}
```

Rationale: Password update logic moves to application service layer.

- [ ] **Step 4: Remove resetPassword method**

Remove entire method:
```java
/**
 * 重置密码（管理员操作）。
 */
public void resetPassword(String plainPassword) {
    validatePasswordStrength(plainPassword);
    this.password = PASSWORD_ENCODER.encode(plainPassword);
}
```

Rationale: Password reset logic moves to application service layer.

- [ ] **Step 5: Add changePassword method**

Add new method that accepts already encoded password:
```java
/**
 * 修改密码（已加密）。
 *
 * @param encodedPassword 加密后的密码
 */
public void changePassword(String encodedPassword) {
    this.password = Objects.requireNonNull(encodedPassword, "encodedPassword cannot be null");
}
```

- [ ] **Step 6: Verify file compiles**

Run: `cd server && ./gradlew compileJava`

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Run existing tests to see failures**

Run: `cd server && ./gradlew test --tests "*AdminUserTest"`

Expected: Some tests will fail (this is expected, we'll fix them later)

- [ ] **Step 8: Commit**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java
git commit -m "refactor(admin): remove password methods, add changePassword to AdminUser"
```

---

## Task 5: Update AdminUserAuthAppService

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminUserAuthAppService.java`

- [ ] **Step 1: Add PasswordEncoderService dependency**

Add import:
```java
import com.aieducenter.admin.domain.service.PasswordEncoderService;
```

Add field:
```java
private final PasswordEncoderService passwordEncoderService;
```

Update constructor:
```java
public AdminUserAuthAppService(
        AdminUserRepository adminUserRepository,
        AdminUserPermissionAppService adminPermissionAppService,
        AuthenticationService authenticationService,
        AdminUserMapper adminUserMapper,
        PasswordEncoderService passwordEncoderService) {
    this.adminUserRepository = adminUserRepository;
    this.adminPermissionAppService = adminPermissionAppService;
    this.authenticationService = authenticationService;
    this.adminUserMapper = adminUserMapper;
    this.passwordEncoderService = passwordEncoderService;
}
```

- [ ] **Step 2: Update login method to use domain service**

Change from:
```java
if (!adminUser.matchesPassword(command.password())) {
    throw new ApplicationException(AdminMessage.LOGIN_FAILED);
}
```

To:
```java
if (!passwordEncoderService.verifyPassword(command.password(), adminUser.getPassword())) {
    throw new ApplicationException(AdminMessage.LOGIN_FAILED);
}
```

- [ ] **Step 3: Update updatePassword method**

Change from:
```java
public void updatePassword(Long userId, UpdatePasswordCommand command) {
    AdminUser adminUser = adminUserRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

    adminUser.updatePassword(command.oldPassword(), command.newPassword());
    adminUserRepository.save(adminUser);
}
```

To:
```java
public void updatePassword(Long userId, UpdatePasswordCommand command) {
    AdminUser adminUser = adminUserRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

    // Verify old password using domain service
    if (!passwordEncoderService.verifyPassword(command.oldPassword(), adminUser.getPassword())) {
        throw new ApplicationException(AdminMessage.PASSWORD_INCORRECT);
    }

    // Encode and update new password using domain service
    String newEncodedPassword = passwordEncoderService.encodePassword(command.newPassword());
    adminUser.changePassword(newEncodedPassword);
    adminUserRepository.save(adminUser);
}
```

- [ ] **Step 4: Update resetPassword method**

Change from:
```java
public void resetPassword(Long userId, ResetPasswordCommand command) {
    AdminUser adminUser = adminUserRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

    adminUser.resetPassword(command.newPassword());
    adminUserRepository.save(adminUser);
}
```

To:
```java
public void resetPassword(Long userId, ResetPasswordCommand command) {
    AdminUser adminUser = adminUserRepository.findById(userId)
            .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

    String encodedPassword = passwordEncoderService.encodePassword(command.newPassword());
    adminUser.changePassword(encodedPassword);
    adminUserRepository.save(adminUser);
}
```

- [ ] **Step 5: Verify file compiles**

Run: `cd server && ./gradlew compileJava`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/src/main/java/com/aieducenter/admin/application/AdminUserAuthAppService.java
git commit -m "refactor(admin): use PasswordEncoderService in AdminUserAuthAppService"
```

---

## Task 6: Update AdminUserManagementAppService

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java`

- [ ] **Step 1: Add PasswordEncoderService dependency and import**

Add import:
```java
import com.aieducenter.admin.domain.service.PasswordEncoderService;
```

Add field and update constructor:
```java
private final PasswordEncoderService passwordEncoderService;

public AdminUserManagementAppService(
        AdminUserRepository adminUserRepository,
        AdminRoleRepository adminRoleRepository,
        PasswordEncoderService passwordEncoderService) {
    this.adminUserRepository = adminUserRepository;
    this.adminRoleRepository = adminRoleRepository;
    this.passwordEncoderService = passwordEncoderService;
}
```

- [ ] **Step 2: Add password strength validation constant**

Add constant at class level:
```java
private static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$";
```

- [ ] **Step 3: Update create method to validate and encode password**

Change from:
```java
public Long create(CreateAdminUserCommand command) {
    adminUserRepository.existsByUsername(command.username())
            .thenThrow(() -> new ApplicationException(AdminMessage.USERNAME_ALREADY_EXISTS));

    AdminUser adminUser = new AdminUser(
        command.username(),
        command.password(),
        command.nickname()
    );
    // ...
}
```

To:
```java
public Long create(CreateAdminUserCommand command) {
    adminUserRepository.existsByUsername(command.username())
            .thenThrow(() -> new ApplicationException(AdminMessage.USERNAME_ALREADY_EXISTS));

    // Validate password strength in application service
    if (command.password() == null || !command.password().matches(PASSWORD_PATTERN)) {
        throw new ApplicationException(AdminMessage.PASSWORD_WEAK);
    }

    // Encode password in application service
    String encodedPassword = passwordEncoderService.encodePassword(command.password());

    AdminUser adminUser = new AdminUser(
        command.username(),
        encodedPassword,  // Pass encoded password
        command.nickname()
    );
    // ...
}
```

- [ ] **Step 4: Verify file compiles**

Run: `cd server && ./gradlew compileJava`

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java
git commit -m "refactor(admin): use PasswordEncoderService and add password validation in AdminUserManagementAppService"
```

---

## Task 7: Update AdminUserTest

**Files:**
- Modify: `server/src/test/java/com/aieducenter/admin/domain/aggregate/AdminUserTest.java`

- [ ] **Step 1: Add BCryptPasswordEncoder to generate test data**

Add field and setup:
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AdminUserTest {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    // Helper method to generate encoded password for tests
    private String encodePassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }
```

- [ ] **Step 2: Update test for admin creation with encoded password**

Change from:
```java
@Test
void given_valid_input_when_create_admin_then_success() {
    // When
    AdminUser adminUser = new AdminUser("testuser", "Test1234", "测试用户");

    // Then
    assertThat(adminUser.getUsername()).isEqualTo("testuser");
    assertThat(adminUser.getNickname()).isEqualTo("测试用户");
    assertThat(adminUser.getStatus()).isEqualTo(AdminUser.AdminUserStatus.ACTIVE);
}
```

To:
```java
@Test
void given_valid_input_when_create_admin_then_success() {
    // Given - pre-encoded password (simulating what application service does)
    String encodedPassword = encodePassword("Test1234");

    // When
    AdminUser adminUser = new AdminUser("testuser", encodedPassword, "测试用户");

    // Then
    assertThat(adminUser.getUsername()).isEqualTo("testuser");
    assertThat(adminUser.getNickname()).isEqualTo("测试用户");
    assertThat(adminUser.getStatus()).isEqualTo(AdminUser.AdminUserStatus.ACTIVE);
    assertThat(adminUser.getPassword()).isEqualTo(encodedPassword);
}
```

- [ ] **Step 3: Remove tests for password validation**

Remove tests:
- `given_invalid_username_when_create_admin_then_throw_exception` (keep this one, username validation is still in domain)
- `given_weak_password_when_create_admin_then_throw_exception` (remove, moved to application layer)

- [ ] **Step 4: Remove tests for password matching and update**

Remove tests (moved to application service layer):
- `given_correct_password_when_matches_password_then_true`
- `given_wrong_password_when_matches_password_then_false`
- `given_old_password_correct_when_update_password_then_success`
- `given_old_password_incorrect_when_update_password_then_throw_exception`
- `given_admin_when_reset_password_then_success`

- [ ] **Step 5: Add tests for changePassword method**

```java
@Test
void given_encoded_password_when_changePassword_then_success() {
    // Given
    String oldPassword = encodePassword("OldPass123");
    String newPassword = encodePassword("NewPass456");
    AdminUser adminUser = new AdminUser("testuser", oldPassword, "测试用户");

    // When
    adminUser.changePassword(newPassword);

    // Then
    assertThat(adminUser.getPassword()).isEqualTo(newPassword);
}

@Test
void given_null_password_when_changePassword_then_throw_exception() {
    // Given
    AdminUser adminUser = new AdminUser("testuser", encodePassword("Test1234"), "测试用户");

    // When & Then
    assertThatThrownBy(() -> adminUser.changePassword(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("encodedPassword cannot be null");
}
```

- [ ] **Step 6: Run tests**

Run: `cd server && ./gradlew test --tests "*AdminUserTest"`

Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add server/src/test/java/com/aieducenter/admin/domain/aggregate/AdminUserTest.java
git commit -m "test(admin): update AdminUserTest for refactored password handling"
```

---

## Task 8: Update AdminUserAuthAppServiceTest

**Files:**
- Modify: `server/src/test/java/com/aieducenter/admin/application/AdminUserAuthAppServiceTest.java`

- [ ] **Step 1: Add BCryptPasswordEncoder and PasswordEncoderService setup**

Add imports and field:
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
```

Add field:
```java
@Mock
private PasswordEncoderService passwordEncoderService;

private static final BCryptPasswordEncoder testEncoder = new BCryptPasswordEncoder(10);
```

- [ ] **Step 2: Update setUp method**

Update constructor call:
```java
adminAuthAppService = new AdminUserAuthAppService(
    adminUserRepository,
    adminPermissionAppService,
    authenticationService,
    adminUserMapper,
    passwordEncoderService  // Add this
);
```

- [ ] **Step 3: Update login test for password verification**

Change from:
```java
@Test
void given_valid_credentials_when_login_then_return_token_info() {
    // Given
    AdminUser adminUser = new AdminUser("admin", "Test1234", "管理员");
    var tokenInfo = new TokenInfo("test-token", 1L, Instant.now().plusSeconds(86400));

    when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
    when(authenticationService.login(any(), eq(86400L))).thenReturn(tokenInfo);

    AdminUserLoginCommand command = new AdminUserLoginCommand("admin", "Test1234", false);

    // When
    TokenInfo result = adminAuthAppService.login(command);

    // Then
    assertThat(result.token()).isEqualTo("test-token");
}
```

To:
```java
@Test
void given_valid_credentials_when_login_then_return_token_info() {
    // Given
    String plainPassword = "Test1234";
    String encodedPassword = testEncoder.encode(plainPassword);
    AdminUser adminUser = new AdminUser("admin", encodedPassword, "管理员");
    var tokenInfo = new TokenInfo("test-token", 1L, Instant.now().plusSeconds(86400));

    when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
    when(passwordEncoderService.verifyPassword(plainPassword, encodedPassword)).thenReturn(true);
    when(authenticationService.login(any(), eq(86400L))).thenReturn(tokenInfo);

    AdminUserLoginCommand command = new AdminUserLoginCommand("admin", plainPassword, false);

    // When
    TokenInfo result = adminAuthAppService.login(command);

    // Then
    assertThat(result.token()).isEqualTo("test-token");
    verify(passwordEncoderService).verifyPassword(plainPassword, encodedPassword);
}
```

- [ ] **Step 4: Update wrong password login test**

Change to mock passwordEncoderService:
```java
@Test
void given_wrong_password_when_login_then_throw_login_failed_exception() {
    // Given
    String encodedPassword = testEncoder.encode("Test1234");
    AdminUser adminUser = new AdminUser("admin", encodedPassword, "管理员");
    when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
    when(passwordEncoderService.verifyPassword("WrongPass123", encodedPassword)).thenReturn(false);

    AdminUserLoginCommand command = new AdminUserLoginCommand("admin", "WrongPass123", false);

    // When & Then
    assertThatThrownBy(() -> adminAuthAppService.login(command))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining(AdminMessage.LOGIN_FAILED.message());
}
```

- [ ] **Step 5: Update updatePassword tests**

Change to use passwordEncoderService mocks:
```java
@Test
void given_valid_input_when_updatePassword_then_success() {
    // Given
    String oldEncodedPassword = testEncoder.encode("Test1234");
    String newEncodedPassword = testEncoder.encode("NewPass56");
    AdminUser adminUser = new AdminUser("admin", oldEncodedPassword, "管理员");

    when(adminUserRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    when(passwordEncoderService.verifyPassword("Test1234", oldEncodedPassword)).thenReturn(true);
    when(passwordEncoderService.encodePassword("NewPass56")).thenReturn(newEncodedPassword);
    UpdatePasswordCommand command = new UpdatePasswordCommand("Test1234", "NewPass56");

    // When
    adminAuthAppService.updatePassword(1L, command);

    // Then
    assertThat(adminUser.matchesPassword(newEncodedPassword)).isTrue();
    verify(adminUserRepository).save(adminUser);
}
```

Wait, this test references `matchesPassword` which no longer exists. Change to:
```java
// Then
assertThat(adminUser.getPassword()).isEqualTo(newEncodedPassword);
verify(adminUserRepository).save(adminUser);
```

- [ ] **Step 6: Update resetPassword tests**

Similar pattern - mock passwordEncoderService.encodePassword().

- [ ] **Step 7: Run tests**

Run: `cd server && ./gradlew test --tests "*AdminUserAuthAppServiceTest"`

Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add server/src/test/java/com/aieducenter/admin/application/AdminUserAuthAppServiceTest.java
git commit -m "test(admin): update AdminUserAuthAppServiceTest for PasswordEncoderService"
```

---

## Task 9: Add Application Service Tests for Password Validation

**Files:**
- Modify: `server/src/test/java/com/aieducenter/admin/application/AdminUserManagementAppServiceTest.java`

- [ ] **Step 1: Add test for password strength validation in create method**

```java
@Test
void given_weak_password_when_create_then_throw_application_exception() {
    // Given
    CreateAdminUserCommand command = new CreateAdminUserCommand("testuser", "weak", "测试用户");

    // When & Then
    assertThatThrownBy(() -> adminManagementAppService.create(command))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining(AdminMessage.PASSWORD_WEAK.message());
}
```

Note: Password validation is now in application service, not domain.

- [ ] **Step 2: Run tests**

Run: `cd server && ./gradlew test --tests "*AdminUserManagementAppServiceTest"`

Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add server/src/test/java/com/aieducenter/admin/application/AdminUserManagementAppServiceTest.java
git commit -m "test(admin): add password validation test to AdminUserManagementAppServiceTest"
```

---

## Task 10: Full Test Suite Run

**Files:**
- All test files

- [ ] **Step 1: Run all admin context tests**

Run: `cd server && ./gradlew test --tests "*.admin.*"`

Expected: ALL PASS

- [ ] **Step 2: Run full test suite**

Run: `cd server && ./gradlew test`

Expected: ALL PASS (no regressions in other contexts)

- [ ] **Step 3: Verify architecture rules still pass**

Run: `cd server && ./gradlew test --tests "*ArchitectureTest"`

Expected: PASS (domain layer should not depend on Spring Security anymore)

Check: The architecture test should show that AdminUser no longer depends on BCryptPasswordEncoder.

- [ ] **Step 4: Verify BCryptPasswordEncoderAdapter is discoverable**

Run: `cd server && ./gradlew check`

Expected: Spring context loads successfully with the BCryptPasswordEncoderAdapter bean.

- [ ] **Step 5: Final commit**

```bash
git add .
git commit -m "test(admin): all tests pass after password domain service refactoring"
```

---

## Task 11: Documentation Update (Optional)

**Files:**
- Update: `docs/guide/aieducenter-platform-开发手册.md` (if needed)

- [ ] **Step 1: Document the PasswordEncoderService pattern**

Add section explaining:
- Domain service + Port pattern for external dependencies
- How it differs from Repository pattern (service port vs data port)
- When to use PortType.CLIENT for infrastructure services
- The importance of @Component annotation on adapters

- [ ] **Step 2: Commit docs**

```bash
git add docs/
git commit -m "docs(admin): document PasswordEncoderService pattern"
```

---

## Verification Checklist

After completing all tasks:

- [ ] All tests pass: `./gradlew test`
- [ ] Architecture rules pass: domain layer has no Spring Security dependencies
- [ ] AdminUser aggregate has zero external dependencies
- [ ] BCryptPasswordEncoderAdapter is in infrastructure package with @Component
- [ ] Application services use PasswordEncoderService for all password operations
- [ ] Password strength validation is in application service layer
- [ ] No regression in existing functionality
- [ ] BCryptPasswordEncoderAdapter bean is discoverable by Spring

---

## Rollback Plan

If critical issues arise:

```bash
# Revert all commits in this refactoring
git revert <commit-hash>..HEAD

# Or reset to before refactoring started
git reset --hard <commit-before-refactoring>
```

---

## References

- cartisan-boot 使用手册: Section 2.3 (架构注解), Section 4.1.2 (设计原则)
- DDD 六边形架构: North/South ports, Primary/Secondary actors
- 《解构领域驱动设计》: Port/Adapter 模式
- Review feedback: Use PortType.CLIENT for service ports, add @Component to adapters
