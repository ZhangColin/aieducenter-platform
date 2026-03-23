# Admin 上下文重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 规范 Admin 上下文命名、优化 DTO 结构、使用框架服务、补全测试

**Architecture:** DDD 六边形架构，分层重构：Domain → Application → Web

**Tech Stack:** Java 21, Spring Boot 3.4, cartisan-boot 框架, JUnit 5, AssertJ, MockMvc

---

## 文件结构

### 新增文件
- `server/src/main/java/com/aieducenter/admin/application/dto/response/CurrentUserResponse.java`
- `server/src/main/java/com/aieducenter/admin/application/dto/command/UpdatePasswordCommand.java`
- `server/src/main/java/com/aieducenter/admin/application/dto/command/ResetPasswordCommand.java`
- `server/src/test/java/com/aieducenter/admin/application/AdminUserAuthAppServiceTest.java`
- `server/src/test/java/com/aieducenter/admin/application/AdminUserManagementAppServiceTest.java`
- `server/src/test/java/com/aieducenter/admin/web/controller/AdminAuthControllerTest.java`
- `server/src/test/java/com/aieducenter/admin/web/controller/AdminUserControllerTest.java`

### 重命名文件
- `AdminError.java` → `AdminMessage.java`
- `AdminDto.java` → `AdminUserResponse.java`
- `RoleDto.java` → `RoleResponse.java`
- `MenuDto.java` → `MenuResponse.java`
- `PermissionDto.java` → `PermissionResponse.java`
- `AdminLoginCommand.java` → `AdminUserLoginCommand.java`
- `AdminManagementAppService.java` → `AdminUserManagementAppService.java`
- `AdminAuthAppService.java` → `AdminUserAuthAppService.java`
- `AdminPermissionAppService.java` → `AdminUserPermissionAppService.java`

### 删除文件
- `server/src/main/java/com/aieducenter/admin/application/dto/response/LoginResult.java`
- `server/src/main/java/com/aieducenter/config/JpaAuditConfig.java`

### 修改文件
- `AdminUser.java` - 移除 isSystem 字段
- `AdminUserController.java` - 更新引用
- `AdminAuthController.java` - 更新返回类型和 @CurrentUser
- `AdminRoleController.java` - 更新引用
- `AdminMenuController.java` - 更新引用
- `PermissionController.java` - 更新引用
- 所有 Application Service 类 - 更新引用
- 所有测试文件 - 更新引用

---

## Task 1: 删除 JpaAuditConfig（框架已提供）

**Files:**
- Delete: `server/src/main/java/com/aieducenter/config/JpaAuditConfig.java`

- [ ] **Step 1: 删除 JpaAuditConfig.java**

```bash
rm server/src/main/java/com/aieducenter/config/JpaAuditConfig.java
```

- [ ] **Step 2: 运行测试确认无影响**

Run: `cd server && ./gradlew test`
Expected: PASS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/config/JpaAuditConfig.java
git commit -m "refactor: remove JpaAuditConfig, use framework's JpaAuditingConfiguration"
```

---

## Task 2: AdminError → AdminMessage

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/error/AdminError.java`

- [ ] **Step 1: 重命名类和文件**

```bash
# 重命名文件
mv server/src/main/java/com/aieducenter/admin/domain/error/AdminError.java \
   server/src/main/java/com/aieducenter/admin/domain/error/AdminMessage.java
```

- [ ] **Step 2: 修改类内容**

将 `AdminError` 改为 `AdminMessage`，删除 `ADMIN_SYSTEM_CANNOT_DELETE`，新增 `LAST_ADMIN_CANNOT_DELETE`：

```java
package com.aieducenter.admin.domain.error;

import com.cartisan.core.exception.CodeMessage;

/**
 * Admin 模块消息定义。
 *
 * <h3>消息分类</h3>
 * <ul>
 *   <li>格式校验错误 (400): USERNAME_INVALID, PASSWORD_WEAK</li>
 *   <li>唯一性错误 (409): USERNAME_ALREADY_EXISTS, ROLE_CODE_ALREADY_EXISTS, PERMISSION_CODE_ALREADY_EXISTS</li>
 *   <li>密码错误 (400): PASSWORD_INCORRECT</li>
 *   <li>资源不存在 (404): ADMIN_NOT_FOUND, ROLE_NOT_FOUND, MENU_NOT_FOUND, PERMISSION_NOT_FOUND</li>
 *   <li>登录错误 (401): LOGIN_FAILED, ADMIN_DISABLED</li>
 *   <li>业务限制 (403): LAST_ADMIN_CANNOT_DELETE, ROLE_IN_USE, SUPER_ADMIN_CANNOT_DELETE</li>
 *   <li>菜单限制 (403): MENU_HAS_CHILDREN, MENU_DEPTH_EXCEEDED, MENU_INVALID_PARENT</li>
 * </ul>
 *
 * @since 0.1.0
 */
public enum AdminMessage implements CodeMessage {

    // ========== 格式校验错误 (400) ==========

    /**
     * 用户名格式不正确。
     */
    USERNAME_INVALID(400, "ADMIN_001", "用户名格式不正确"),

    /**
     * 密码强度不足。
     */
    PASSWORD_WEAK(400, "ADMIN_002", "密码强度不足"),

    // ========== 唯一性错误 (409) ==========

    /**
     * 用户名已存在。
     */
    USERNAME_ALREADY_EXISTS(409, "ADMIN_003", "用户名已存在"),

    /**
     * 角色编码已存在。
     */
    ROLE_CODE_ALREADY_EXISTS(409, "ADMIN_004", "角色编码已存在"),

    /**
     * 权限编码已存在。
     */
    PERMISSION_CODE_ALREADY_EXISTS(409, "ADMIN_004_1", "权限编码已存在"),

    // ========== 密码错误 (400) ==========

    /**
     * 密码错误。
     */
    PASSWORD_INCORRECT(400, "ADMIN_005", "密码错误"),

    // ========== 资源不存在 (404) ==========

    /**
     * 管理员不存在。
     */
    ADMIN_NOT_FOUND(404, "ADMIN_006", "管理员不存在"),

    /**
     * 角色不存在。
     */
    ROLE_NOT_FOUND(404, "ADMIN_007", "角色不存在"),

    /**
     * 菜单不存在。
     */
    MENU_NOT_FOUND(404, "ADMIN_008", "菜单不存在"),

    /**
     * 权限不存在。
     */
    PERMISSION_NOT_FOUND(404, "ADMIN_009", "权限不存在"),

    // ========== 登录错误 (401) ==========

    /**
     * 登录失败。
     */
    LOGIN_FAILED(401, "ADMIN_010", "用户名或密码错误"),

    /**
     * 管理员已被禁用。
     */
    ADMIN_DISABLED(401, "ADMIN_011", "管理员已被禁用"),

    // ========== 业务限制 (403) ==========

    /**
     * 不能删除最后一个管理员。
     */
    LAST_ADMIN_CANNOT_DELETE(403, "ADMIN_012", "不能删除最后一个管理员"),

    /**
     * 角色正在使用中，不能删除。
     */
    ROLE_IN_USE(403, "ADMIN_013", "角色正在使用中，不能删除"),

    /**
     * 菜单有子菜单，不能删除。
     */
    MENU_HAS_CHILDREN(403, "ADMIN_014", "菜单有子菜单，不能删除"),

    /**
     * 菜单层级超限。
     */
    MENU_DEPTH_EXCEEDED(403, "ADMIN_014_1", "菜单层级不能超过3级"),

    /**
     * 菜单父级设置无效。
     */
    MENU_INVALID_PARENT(403, "ADMIN_014_2", "不能将菜单设置为自己的父级或后代"),

    /**
     * 超级管理员角色不能删除。
     */
    SUPER_ADMIN_CANNOT_DELETE(403, "ADMIN_013_1", "超级管理员角色不能删除");

    private final int httpStatus;
    private final String code;
    private final String message;

    AdminMessage(int httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
```

- [ ] **Step 3: 更新所有引用 AdminError 的地方**

```bash
cd server
find src -name "*.java" -type f -exec sed -i '' 's/AdminError/AdminMessage/g' {} \;
```

- [ ] **Step 4: 运行测试确认**

Run: `cd server && ./gradlew test`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add -A
git commit -m "refactor: rename AdminError to AdminMessage, add LAST_ADMIN_CANNOT_DELETE"
```

---

## Task 3: AdminUser 移除 isSystem 字段

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java`
- Test: `server/src/test/java/com/aieducenter/admin/domain/aggregate/AdminUserTest.java`

- [ ] **Step 1: 先写失败的测试 - isSystem 相关测试应该被移除或修改**

当前 `AdminUserTest` 有测试 `isSystem()` 方法，需要移除相关测试。

- [ ] **Step 2: 修改 AdminUser.java**

删除以下内容：
- `@Column(name = "system", nullable = false) private boolean system = false;`
- `public boolean isSystem() { return system; }`
- `checkCanBeDeleted()` 方法中的 `Assertions.require(!system, AdminError.ADMIN_SYSTEM_CANNOT_DELETE);`

```java
// 删除 system 字段
// 删除 isSystem() 方法
// 修改 checkCanBeDeleted() - 简化为空方法或删除
```

- [ ] **Step 3: 更新 AdminUserTest.java**

移除与 `isSystem` 相关的测试。

- [ ] **Step 4: 运行测试**

Run: `cd server && ./gradlew test --tests AdminUserTest`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java
git add server/src/test/java/com/aieducenter/admin/domain/aggregate/AdminUserTest.java
git commit -m "refactor: remove isSystem field from AdminUser"
```

---

## Task 4: DTO 重命名 - AdminDto → AdminUserResponse

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/dto/response/AdminDto.java`

- [ ] **Step 1: 重命名文件**

```bash
mv server/src/main/java/com/aieducenter/admin/application/dto/response/AdminDto.java \
   server/src/main/java/com/aieducenter/admin/application/dto/response/AdminUserResponse.java
```

- [ ] **Step 2: 修改类内容**

```java
package com.aieducenter.admin.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import com.aieducenter.admin.domain.aggregate.AdminUser;

/**
 * 管理员响应 DTO。
 *
 * @since 0.1.0
 */
public record AdminUserResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String phone,
        String avatar,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminUserResponse from(AdminUser adminUser) {
        return new AdminUserResponse(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getNickname(),
                adminUser.getEmail().orElse(null),
                adminUser.getPhone().orElse(null),
                adminUser.getAvatar().orElse(null),
                adminUser.getStatus().name(),
                adminUser.getCreatedAt(),
                adminUser.getUpdatedAt()
        );
    }
}
```

注意：删除了 `system` 字段和 `roles` 字段（roles 将在 CurrentUserResponse 中）。

- [ ] **Step 3: 更新所有引用**

```bash
cd server
find src -name "*.java" -type f -exec sed -i '' 's/AdminDto/AdminUserResponse/g' {} \;
```

- [ ] **Step 4: 修复因 roles 字段删除导致的编译错误**

需要修复 `AdminUserManagementAppService` 等地方对 `AdminDto.from(adminUser, roles)` 的调用。

改为：`AdminUserResponse.from(adminUser)`

- [ ] **Step 5: 运行测试**

Run: `cd server && ./gradlew compileJava`
Expected: PASS

- [ ] **Step 6: 提交**

```bash
git add -A
git commit -m "refactor: rename AdminDto to AdminUserResponse, remove system and roles fields"
```

---

## Task 5: DTO 重命名 - RoleDto, MenuDto, PermissionDto

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/dto/response/RoleDto.java`
- Modify: `server/src/main/java/com/aieducenter/admin/application/dto/response/MenuDto.java`
- Modify: `server/src/main/java/com/aieducenter/admin/application/dto/response/PermissionDto.java`

- [ ] **Step 1: 重命名 RoleDto → RoleResponse**

```bash
mv server/src/main/java/com/aieducenter/admin/application/dto/response/RoleDto.java \
   server/src/main/java/com/aieducenter/admin/application/dto/response/RoleResponse.java
```

修改类名为 `RoleResponse`。

- [ ] **Step 2: 重命名 MenuDto → MenuResponse**

```bash
mv server/src/main/java/com/aieducenter/admin/application/dto/response/MenuDto.java \
   server/src/main/java/com/aieducenter/admin/application/dto/response/MenuResponse.java
```

修改类名为 `MenuResponse`。

- [ ] **Step 3: 重命名 PermissionDto → PermissionResponse**

```bash
mv server/src/main/java/com/aieducenter/admin/application/dto/response/PermissionDto.java \
   server/src/main/java/com/aieducenter/admin/application/dto/response/PermissionResponse.java
```

修改类名为 `PermissionResponse`。

- [ ] **Step 4: 更新所有引用**

```bash
cd server
find src -name "*.java" -type f -exec sed -i '' 's/RoleDto/RoleResponse/g' {} \;
find src -name "*.java" -type f -exec sed -i '' 's/MenuDto/MenuResponse/g' {} \;
find src -name "*.java" -type f -exec sed -i '' 's/PermissionDto/PermissionResponse/g' {} \;
```

- [ ] **Step 5: 运行测试**

Run: `cd server && ./gradlew compileJava`
Expected: PASS

- [ ] **Step 6: 提交**

```bash
git add -A
git commit -m "refactor: rename RoleDto/MenuDto/PermissionDto to *Response"
```

---

## Task 6: 删除 LoginResult，创建 CurrentUserResponse

**Files:**
- Delete: `server/src/main/java/com/aieducenter/admin/application/dto/response/LoginResult.java`
- Create: `server/src/main/java/com/aieducenter/admin/application/dto/response/CurrentUserResponse.java`

- [ ] **Step 1: 创建 CurrentUserResponse.java**

```java
package com.aieducenter.admin.application.dto.response;

import java.util.List;

/**
 * 当前用户响应 DTO。
 *
 * <p>包含用户基本信息、角色、菜单和权限。
 *
 * @since 0.1.0
 */
public record CurrentUserResponse(
        AdminUserResponse user,
        List<String> roleCodes,
        List<MenuResponse> menus,
        List<String> permissions
) {}
```

- [ ] **Step 2: 删除 LoginResult.java**

```bash
rm server/src/main/java/com/aieducenter/admin/application/dto/response/LoginResult.java
```

- [ ] **Step 3: 运行测试确认编译**

Run: `cd server && ./gradlew compileJava`
Expected: 可能有编译错误（因为还有地方引用 LoginResult），下一步修复

- [ ] **Step 4: 提交**

```bash
git add -A
git commit -m "refactor: replace LoginResult with CurrentUserResponse"
```

---

## Task 7: Command 重命名 - AdminLoginCommand → AdminUserLoginCommand

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/dto/command/AdminLoginCommand.java`

- [ ] **Step 1: 重命名文件**

```bash
mv server/src/main/java/com/aieducenter/admin/application/dto/command/AdminLoginCommand.java \
   server/src/main/java/com/aieducenter/admin/application/dto/command/AdminUserLoginCommand.java
```

- [ ] **Step 2: 修改类名为 AdminUserLoginCommand**

- [ ] **Step 3: 更新所有引用**

```bash
cd server
find src -name "*.java" -type f -exec sed -i '' 's/AdminLoginCommand/AdminUserLoginCommand/g' {} \;
```

- [ ] **Step 4: 运行测试**

Run: `cd server && ./gradlew compileJava`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add -A
git commit -m "refactor: rename AdminLoginCommand to AdminUserLoginCommand"
```

---

## Task 8: 新增密码相关 Command

**Files:**
- Create: `server/src/main/java/com/aieducenter/admin/application/dto/command/UpdatePasswordCommand.java`
- Create: `server/src/main/java/com/aieducenter/admin/application/dto/command/ResetPasswordCommand.java`

- [ ] **Step 1: 创建 UpdatePasswordCommand.java**

```java
package com.aieducenter.admin.application.dto.command;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码命令。
 *
 * @since 0.1.0
 */
public record UpdatePasswordCommand(
        @NotBlank(message = "旧密码不能为空")
        String oldPassword,

        @NotBlank(message = "新密码不能为空")
        String newPassword
) {}
```

- [ ] **Step 2: 创建 ResetPasswordCommand.java**

```java
package com.aieducenter.admin.application.dto.command;

import jakarta.validation.constraints.NotBlank;

/**
 * 重置密码命令（管理员操作）。
 *
 * @since 0.1.0
 */
public record ResetPasswordCommand(
        @NotBlank(message = "新密码不能为空")
        String newPassword
) {}
```

- [ ] **Step 3: 运行测试**

Run: `cd server && ./gradlew compileJava`
Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/dto/command/UpdatePasswordCommand.java
git add server/src/main/java/com/aieducenter/admin/application/dto/command/ResetPasswordCommand.java
git commit -m "feat: add UpdatePasswordCommand and ResetPasswordCommand"
```

---

## Task 9: Service 重命名 - AdminXxxAppService → AdminUserXxxAppService

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminManagementAppService.java`
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminAuthAppService.java`
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminPermissionAppService.java`

- [ ] **Step 1: 重命名 AdminManagementAppService**

```bash
mv server/src/main/java/com/aieducenter/admin/application/AdminManagementAppService.java \
   server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java
```

修改类名为 `AdminUserManagementAppService`。

- [ ] **Step 2: 重命名 AdminAuthAppService**

```bash
mv server/src/main/java/com/aieducenter/admin/application/AdminAuthAppService.java \
   server/src/main/java/com/aieducenter/admin/application/AdminUserAuthAppService.java
```

修改类名为 `AdminUserAuthAppService`。

- [ ] **Step 3: 重命名 AdminPermissionAppService**

```bash
mv server/src/main/java/com/aieducenter/admin/application/AdminPermissionAppService.java \
   server/src/main/java/com/aieducenter/admin/application/AdminUserPermissionAppService.java
```

修改类名为 `AdminUserPermissionAppService`。

- [ ] **Step 4: 更新所有引用**

```bash
cd server
find src -name "*.java" -type f -exec sed -i '' 's/AdminManagementAppService/AdminUserManagementAppService/g' {} \;
find src -name "*.java" -type f -exec sed -i '' 's/AdminAuthAppService/AdminUserAuthAppService/g' {} \;
find src -name "*.java" -type f -exec sed -i '' 's/AdminPermissionAppService/AdminUserPermissionAppService/g' {} \;
```

- [ ] **Step 5: 运行测试**

Run: `cd server && ./gradlew compileJava`
Expected: PASS

- [ ] **Step 6: 提交**

```bash
git add -A
git commit -m "refactor: rename Service classes to AdminUserXxxAppService"
```

---

## Task 10: AdminUserAuthAppService 改造 - 使用框架 AuthenticationService

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminUserAuthAppService.java`

- [ ] **Step 1: 修改 AdminUserAuthAppService.java**

```java
package com.aieducenter.admin.application;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.admin.application.dto.command.AdminUserLoginCommand;
import com.aieducenter.admin.application.dto.command.UpdatePasswordCommand;
import com.aieducenter.admin.application.dto.command.ResetPasswordCommand;
import com.aieducenter.admin.application.dto.response.AdminUserResponse;
import com.aieducenter.admin.application.dto.response.CurrentUserResponse;
import com.aieducenter.admin.application.dto.response.MenuResponse;
import com.aieducenter.admin.application.dto.response.RoleResponse;
import com.aieducenter.admin.domain.aggregate.AdminUser;
import com.aieducenter.admin.domain.repository.AdminUserRepository;
import com.cartisan.core.exception.ApplicationException;
import com.aieducenter.admin.domain.error.AdminMessage;
import com.cartisan.security.authentication.AuthenticationService;
import com.cartisan.security.authentication.TokenInfo;

/**
 * 管理员认证应用服务。
 *
 * @since 0.1.0
 */
@Service
public class AdminUserAuthAppService {

    private static final long DEFAULT_TIMEOUT = 86400; // 24 小时
    private static final long REMEMBER_TIMEOUT = 604800; // 7 天

    private final AdminUserRepository adminUserRepository;
    private final AdminUserPermissionAppService adminPermissionAppService;
    private final AuthenticationService authenticationService;

    public AdminUserAuthAppService(
            AdminUserRepository adminUserRepository,
            AdminUserPermissionAppService adminPermissionAppService,
            AuthenticationService authenticationService) {
        this.adminUserRepository = adminUserRepository;
        this.adminPermissionAppService = adminPermissionAppService;
        this.authenticationService = authenticationService;
    }

    /**
     * 管理员登录。
     */
    @Transactional
    public TokenInfo login(AdminUserLoginCommand command) {
        // 验证用户名和密码
        AdminUser adminUser = adminUserRepository.findByUsername(command.username())
                .orElseThrow(() -> new ApplicationException(AdminMessage.LOGIN_FAILED));

        if (!adminUser.isActive()) {
            throw new ApplicationException(AdminMessage.ADMIN_DISABLED);
        }

        if (!adminUser.matchesPassword(command.password())) {
            throw new ApplicationException(AdminMessage.LOGIN_FAILED);
        }

        // 登录（使用框架的 AuthenticationService）
        long timeout = command.rememberMe() ? REMEMBER_TIMEOUT : DEFAULT_TIMEOUT;
        return authenticationService.login(adminUser.getId(), timeout);
    }

    /**
     * 管理员登出。
     */
    public void logout() {
        authenticationService.logout();
    }

    /**
     * 修改当前管理员密码。
     */
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        AdminUser adminUser = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        adminUser.updatePassword(oldPassword, newPassword);
        adminUserRepository.save(adminUser);
    }

    /**
     * 重置管理员密码（管理员操作）。
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        AdminUser adminUser = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        adminUser.resetPassword(newPassword);
        adminUserRepository.save(adminUser);
    }

    /**
     * 获取当前管理员信息。
     */
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentAdmin(Long userId) {
        AdminUser adminUser = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        // 获取角色、菜单、权限
        List<String> roleCodes = adminPermissionAppService.getRoleCodes(userId);
        List<String> permissionCodes = adminPermissionAppService.getPermissions(userId);
        List<MenuResponse> menus = adminPermissionAppService.getMenus(userId);

        AdminUserResponse user = AdminUserResponse.from(adminUser);

        return new CurrentUserResponse(user, roleCodes, menus, permissionCodes);
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `cd server && ./gradlew compileJava`
Expected: PASS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/AdminUserAuthAppService.java
git commit -m "refactor: use AuthenticationService instead of StpLogic in AdminUserAuthAppService"
```

---

## Task 11: AdminUserManagementAppService - 系统管理员删除保护

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java`

- [ ] **Step 1: 修改 delete 方法**

```java
/**
 * 删除管理员。
 */
@Transactional
public void delete(Long id) {
    // 检查是否是最后一个管理员
    if (adminUserRepository.count() <= 1) {
        throw new ApplicationException(AdminMessage.LAST_ADMIN_CANNOT_DELETE);
    }

    AdminUser adminUser = adminUserRepository.findById(id)
            .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

    adminUserRepository.delete(adminUser);
}
```

- [ ] **Step 2: 移除对 adminUser.checkCanBeDeleted() 的调用（如果有）**

- [ ] **Step 3: 运行测试**

Run: `cd server && ./gradlew compileJava`
Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java
git commit -m "feat: add last admin deletion protection in Application layer"
```

---

## Task 12: AdminAuthController 改造

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/web/controller/AdminAuthController.java`

- [ ] **Step 1: 修改 AdminAuthController.java**

```java
package com.aieducenter.admin.web.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.aieducenter.admin.application.AdminUserAuthAppService;
import com.aieducenter.admin.application.dto.command.AdminUserLoginCommand;
import com.aieducenter.admin.application.dto.command.UpdatePasswordCommand;
import com.aieducenter.admin.application.dto.command.ResetPasswordCommand;
import com.aieducenter.admin.application.dto.response.CurrentUserResponse;
import com.cartisan.security.annotation.RequireAuth;
import com.cartisan.security.annotation.CurrentUser;
import com.cartisan.security.authentication.TokenInfo;
import com.cartisan.web.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 管理员认证控制器。
 */
@RestController
@RequestMapping("/api/v1/admin/auth")
@Validated
@Tag(name = "Admin Auth", description = "管理员认证")
public class AdminAuthController {

    private final AdminUserAuthAppService adminAuthAppService;

    public AdminAuthController(AdminUserAuthAppService adminAuthAppService) {
        this.adminAuthAppService = adminAuthAppService;
    }

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public ApiResponse<TokenInfo> login(@Valid @RequestBody AdminUserLoginCommand command) {
        return ApiResponse.ok(adminAuthAppService.login(command));
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员登出")
    public ApiResponse<Void> logout() {
        adminAuthAppService.logout();
        return ApiResponse.ok();
    }

    @GetMapping("/current")
    @RequireAuth
    @Operation(summary = "获取当前管理员信息")
    public ApiResponse<CurrentUserResponse> getCurrentAdmin(@CurrentUser Long userId) {
        return ApiResponse.ok(adminAuthAppService.getCurrentAdmin(userId));
    }

    @PutMapping("/current/password")
    @RequireAuth
    @Operation(summary = "修改当前管理员密码")
    public ApiResponse<Void> updatePassword(
            @CurrentUser Long userId,
            @Valid @RequestBody UpdatePasswordCommand command) {
        adminAuthAppService.updatePassword(userId, command.oldPassword(), command.newPassword());
        return ApiResponse.ok();
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `cd server && ./gradlew compileJava`
Expected: PASS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/web/controller/AdminAuthController.java
git commit -m "refactor: AdminAuthController use TokenInfo and @CurrentUser annotation"
```

---

## Task 13: AdminUserController 改造 - 添加重置密码接口

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/web/controller/AdminUserController.java`

- [ ] **Step 1: 添加重置密码接口**

在 `AdminUserController` 中添加：

```java
@PutMapping("/{id}/password")
@RequireAuth
@RequirePermission(
    value = "admin:user:write",
    name = "平台管理 / 用户管理 / 编辑",
    scope = "admin"
)
@Operation(summary = "重置管理员密码")
public ApiResponse<Void> resetPassword(
        @PathVariable Long id,
        @Valid @RequestBody ResetPasswordCommand command) {
    adminUserManagementAppService.resetPassword(id, command.newPassword());
    return ApiResponse.ok();
}
```

同时需要添加 import：
```java
import com.aieducenter.admin.application.dto.command.ResetPasswordCommand;
import com.cartisan.security.annotation.CurrentUser;
```

- [ ] **Step 2: 在 AdminUserManagementAppService 中添加 resetPassword 方法**

```java
/**
 * 重置管理员密码。
 */
@Transactional
public void resetPassword(Long id, String newPassword) {
    AdminUser adminUser = adminUserRepository.findById(id)
            .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

    adminUser.resetPassword(newPassword);
    adminUserRepository.save(adminUser);
}
```

- [ ] **Step 3: 运行测试**

Run: `cd server && ./gradlew compileJava`
Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/web/controller/AdminUserController.java
git add server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java
git commit -m "feat: add reset password API endpoint"
```

---

## Task 14: 更新所有 import 引用

**Files:**
- 所有 `.dto.query.` 引用改为 `.dto.response.`

- [ ] **Step 1: 批量更新 import**

```bash
cd server
find src -name "*.java" -type f -exec sed -i '' 's/com\.aieducenter\.admin\.application\.dto\.query/com.aieducenter.admin.application.dto.response/g' {} \;
```

- [ ] **Step 2: 检查并修复 Command 类的 import（如果被错误替换）**

Command 类应该在 `.dto.command` 包下，如果有被错误替换的，需要修复。

- [ ] **Step 3: 运行测试**

Run: `cd server && ./gradlew compileJava`
Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add -A
git commit -m "refactor: update import statements from .dto.query to .dto.response"
```

---

## Task 15: 编写 AdminUserAuthAppServiceTest

**Files:**
- Create: `server/src/test/java/com/aieducenter/admin/application/AdminUserAuthAppServiceTest.java`

- [ ] **Step 1: 编写测试**

```java
package com.aieducenter.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.admin.application.dto.command.AdminUserLoginCommand;
import com.aieducenter.admin.application.dto.command.UpdatePasswordCommand;
import com.aieducenter.admin.application.dto.command.ResetPasswordCommand;
import com.aieducenter.admin.application.dto.response.CurrentUserResponse;
import com.aieducenter.admin.domain.aggregate.AdminUser;
import com.aieducenter.admin.domain.error.AdminMessage;
import com.aieducenter.admin.domain.repository.AdminUserRepository;
import com.cartisan.core.exception.ApplicationException;
import com.cartisan.security.authentication.AuthenticationService;
import com.cartisan.security.authentication.TokenInfo;

@ExtendWith(MockitoExtension.class)
class AdminUserAuthAppServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AdminUserPermissionAppService adminPermissionAppService;

    @Mock
    private AuthenticationService authenticationService;

    private AdminUserAuthAppService adminAuthAppService;

    @BeforeEach
    void setUp() {
        adminAuthAppService = new AdminUserAuthAppService(
            adminUserRepository,
            adminPermissionAppService,
            authenticationService
        );
    }

    @Test
    void given_valid_credentials_when_login_then_return_token_info() {
        // Given
        AdminUser adminUser = new AdminUser("admin", "Test1234", "管理员");
        when(adminUserRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(adminUser));
        when(authenticationService.login(adminUser.getId(), 86400))
            .thenReturn(new TokenInfo("test-token", adminUser.getId(), Instant.now().plusSeconds(86400)));

        AdminUserLoginCommand command = new AdminUserLoginCommand("admin", "Test1234", false);

        // When
        TokenInfo result = adminAuthAppService.login(command);

        // Then
        assertThat(result.token()).isEqualTo("test-token");
    }

    @Test
    void given_wrong_password_when_login_then_throw_exception() {
        // Given
        AdminUser adminUser = new AdminUser("admin", "Test1234", "管理员");
        when(adminUserRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(adminUser));

        AdminUserLoginCommand command = new AdminUserLoginCommand("admin", "WrongPass123", false);

        // When & Then
        assertThatThrownBy(() -> adminAuthAppService.login(command))
            .isInstanceOf(ApplicationException.class)
            .extracting("code")
            .isEqualTo(AdminMessage.LOGIN_FAILED.code());
    }

    @Test
    void given_disabled_user_when_login_then_throw_exception() {
        // Given
        AdminUser adminUser = new AdminUser("admin", "Test1234", "管理员");
        adminUser.disable();
        when(adminUserRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(adminUser));

        AdminUserLoginCommand command = new AdminUserLoginCommand("admin", "Test1234", false);

        // When & Then
        assertThatThrownBy(() -> adminAuthAppService.login(command))
            .isInstanceOf(ApplicationException.class)
            .extracting("code")
            .isEqualTo(AdminMessage.ADMIN_DISABLED.code());
    }

    @Test
    void given_valid_input_when_updatePassword_then_success() {
        // Given
        AdminUser adminUser = new AdminUser("admin", "Test1234", "管理员");
        when(adminUserRepository.findById(1L)).thenReturn(java.util.Optional.of(adminUser));

        // When
        adminAuthAppService.updatePassword(1L, "Test1234", "NewPass567");

        // Then
        assertThat(adminUser.matchesPassword("NewPass567")).isTrue();
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `cd server && ./gradlew test --tests AdminUserAuthAppServiceTest`
Expected: PASS

- [ ] **Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/admin/application/AdminUserAuthAppServiceTest.java
git commit -m "test: add AdminUserAuthAppServiceTest"
```

---

## Task 16: 编写 AdminUserManagementAppServiceTest

**Files:**
- Create: `server/src/test/java/com/aieducenter/admin/application/AdminUserManagementAppServiceTest.java`

- [ ] **Step 1: 编写测试**

```java
package com.aieducenter.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.admin.application.dto.command.CreateAdminUserCommand;
import com.aieducenter.admin.application.dto.command.UpdateAdminUserCommand;
import com.aieducenter.admin.domain.aggregate.AdminUser;
import com.aieducenter.admin.domain.error.AdminMessage;
import com.aieducenter.admin.domain.repository.AdminRoleRepository;
import com.aieducenter.admin.domain.repository.AdminUserRepository;
import com.cartisan.core.exception.ApplicationException;

@ExtendWith(MockitoExtension.class)
class AdminUserManagementAppServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AdminRoleRepository adminRoleRepository;

    private AdminUserManagementAppService adminUserManagementAppService;

    @BeforeEach
    void setUp() {
        adminUserManagementAppService = new AdminUserManagementAppService(
            adminUserRepository,
            adminRoleRepository
        );
    }

    @Test
    void given_valid_input_when_createAdminUser_then_success() {
        // Given
        CreateAdminUserCommand command = new CreateAdminUserCommand(
            "testuser", "Test1234", "测试用户", "test@example.com", null, null
        );
        when(adminUserRepository.existsByUsername("testuser")).thenReturn(false);
        when(adminUserRepository.save(any(AdminUser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Long id = adminUserManagementAppService.create(command);

        // Then
        assertThat(id).isNotNull();
    }

    @Test
    void given_duplicate_username_when_createAdminUser_then_throw_exception() {
        // Given
        CreateAdminUserCommand command = new CreateAdminUserCommand(
            "testuser", "Test1234", "测试用户", null, null, null
        );
        when(adminUserRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> adminUserManagementAppService.create(command))
            .isInstanceOf(ApplicationException.class)
            .extracting("code")
            .isEqualTo(AdminMessage.USERNAME_ALREADY_EXISTS.code());
    }

    @Test
    void given_only_one_admin_when_delete_then_throw_exception() {
        // Given
        when(adminUserRepository.count()).thenReturn(1L);

        // When & Then
        assertThatThrownBy(() -> adminUserManagementAppService.delete(1L))
            .isInstanceOf(ApplicationException.class)
            .extracting("code")
            .isEqualTo(AdminMessage.LAST_ADMIN_CANNOT_DELETE.code());
    }

    @Test
    void given_multiple_admins_when_delete_then_success() {
        // Given
        when(adminUserRepository.count()).thenReturn(2L);
        AdminUser adminUser = new AdminUser("testuser", "Test1234", "测试用户");
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        // When
        adminUserManagementAppService.delete(1L);

        // Then
        verify(adminUserRepository).delete(adminUser);
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `cd server && ./gradlew test --tests AdminUserManagementAppServiceTest`
Expected: PASS

- [ ] **Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/admin/application/AdminUserManagementAppServiceTest.java
git commit -m "test: add AdminUserManagementAppServiceTest"
```

---

## Task 17: 编写 AdminAuthControllerTest

**Files:**
- Create: `server/src/test/java/com/aieducenter/admin/web/controller/AdminAuthControllerTest.java`

- [ ] **Step 1: 编写测试**

```java
package com.aieducenter.admin.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.aieducenter.admin.application.AdminUserAuthAppService;
import com.aieducenter.admin.application.dto.command.AdminUserLoginCommand;
import com.aieducenter.admin.application.dto.command.UpdatePasswordCommand;
import com.aieducenter.admin.application.dto.response.AdminUserResponse;
import com.aieducenter.admin.application.dto.response.CurrentUserResponse;
import com.aieducenter.admin.domain.aggregate.AdminUser;
import com.cartisan.security.authentication.AuthenticationService;
import com.cartisan.security.authentication.TokenInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AdminAuthController.class)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserAuthAppService adminAuthAppService;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void given_valid_credentials_when_login_then_return_token_info() throws Exception {
        // Given
        TokenInfo tokenInfo = new TokenInfo("test-token", 1L, Instant.now().plusSeconds(86400));
        when(adminAuthAppService.login(any(AdminUserLoginCommand.class))).thenReturn(tokenInfo);

        AdminUserLoginCommand command = new AdminUserLoginCommand("admin", "Test1234", false);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("test-token"));
    }

    @Test
    void given_authenticated_user_when_getCurrentAdmin_then_return_user_info() throws Exception {
        // Given
        AdminUser adminUser = new AdminUser("admin", "Test1234", "管理员");
        AdminUserResponse userResponse = AdminUserResponse.from(adminUser);
        CurrentUserResponse response = new CurrentUserResponse(
            userResponse, List.of("ADMIN"), List.of(), List.of("admin:user:read")
        );
        when(adminAuthAppService.getCurrentAdmin(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/auth/current")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.username").value("admin"));
    }

    @Test
    void given_authenticated_user_when_logout_then_success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/admin/auth/logout"))
                .andExpect(status().isOk());

        verify(adminAuthAppService).logout();
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `cd server && ./gradlew test --tests AdminAuthControllerTest`
Expected: PASS

- [ ] **Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/admin/web/controller/AdminAuthControllerTest.java
git commit -m "test: add AdminAuthControllerTest"
```

---

## Task 18: 编写 AdminUserControllerTest

**Files:**
- Create: `server/src/test/java/com/aieducenter/admin/web/controller/AdminUserControllerTest.java`

- [ ] **Step 1: 编写测试**

```java
package com.aieducenter.admin.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.aieducenter.admin.application.AdminUserManagementAppService;
import com.aieducenter.admin.application.dto.command.CreateAdminUserCommand;
import com.aieducenter.admin.application.dto.command.ResetPasswordCommand;
import com.aieducenter.admin.application.dto.command.UpdateAdminUserCommand;
import com.aieducenter.admin.application.dto.response.AdminUserResponse;
import com.aieducenter.admin.application.dto.response.RoleResponse;
import com.aieducenter.admin.domain.aggregate.AdminUser;
import com.cartisan.web.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserManagementAppService adminUserManagementAppService;

    @Test
    void given_valid_input_when_createAdminUser_then_return_id() throws Exception {
        // Given
        CreateAdminUserCommand command = new CreateAdminUserCommand(
            "testuser", "Test1234", "测试用户", "test@example.com", null, null
        );
        when(adminUserManagementAppService.create(any(CreateAdminUserCommand.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void given_existing_user_when_delete_then_success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isOk());

        verify(adminUserManagementAppService).delete(1L);
    }

    @Test
    void given_valid_input_when_resetPassword_then_success() throws Exception {
        // Given
        ResetPasswordCommand command = new ResetPasswordCommand("NewPass567");

        // When & Then
        mockMvc.perform(put("/api/v1/admin/users/1/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());

        verify(adminUserManagementAppService).resetPassword(1L, "NewPass567");
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `cd server && ./gradlew test --tests AdminUserControllerTest`
Expected: PASS

- [ ] **Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/admin/web/controller/AdminUserControllerTest.java
git commit -m "test: add AdminUserControllerTest"
```

---

## Task 19: 运行全量测试并修复问题

**Files:**
- All

- [ ] **Step 1: 运行全量测试**

Run: `cd server && ./gradlew test`
Expected: PASS

- [ ] **Step 2: 如有失败，修复问题并重新运行**

- [ ] **Step 3: 运行 check 任务**

Run: `cd server && ./gradlew check`
Expected: PASS

- [ ] **Step 4: 提交最终修复**

```bash
git add -A
git commit -m "test: fix test issues"
```

---

## Task 20: 更新测试文件中的引用

**Files:**
- Test files that reference renamed classes

- [ ] **Step 1: 更新 AdminUserTest**

将 `AdminError` 改为 `AdminMessage`。

- [ ] **Step 2: 更新 AdminRoleTest**

将 `AdminError` 改为 `AdminMessage`。

- [ ] **Step 3: 运行测试**

Run: `cd server && ./gradlew test`
Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add server/src/test/java/com/aieducenter/admin/domain/
git commit -m "test: update test files to use AdminMessage"
```

---

## 总结

完成以上所有任务后：

1. 命名规范统一：AdminXxx → AdminUserXxx
2. DTO 结构优化：LoginResult → TokenInfo，新增 CurrentUserResponse
3. 使用框架服务：AuthenticationService 替换 StpLogic
4. 系统管理员删除保护：检查至少保留一个
5. 测试覆盖：Application Service 和 Controller 层
6. 清理工作：删除 JpaAuditConfig、移除 isSystem 字段
