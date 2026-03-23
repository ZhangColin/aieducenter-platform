# Admin 上下文重构设计文档

> **目标**: 规范命名、优化 DTO 结构、使用框架服务、补全测试

**日期**: 2026-03-23

## 背景

Admin 上下文在代码 Review 后发现多处需要改进：
1. 命名不一致：`AdminXxx` 应该统一为 `AdminUserXxx`
2. DTO 结构混乱：`LoginResult` 职责不清，应按用途拆分
3. 直接使用 Sa-Token 的 `StpLogic`，应使用框架的 `AuthenticationService`
4. 系统管理员删除保护用 `isSystem` 字段太重，应改为检查至少保留一个
5. 测试覆盖不足：只有 Domain 层有测试，Application 和 Controller 层缺失

## 设计

### 1. DTO 结构重构

**登录响应简化**：

```
Before: LoginResult(token, expireTime, admin, roles, menus, permissions)

After:
- POST /login → TokenInfo (框架提供)
- GET /current → CurrentUserResponse
```

**CurrentUserResponse 结构**：

```java
public record CurrentUserResponse(
    AdminUserResponse user,
    List<String> roles,           // 只需要 code 列表
    List<MenuResponse> menus,
    List<String> permissions
) {}
```

**DTO 统一使用 Response 后缀**：

| Before | After |
|--------|-------|
| AdminDto | AdminUserResponse |
| RoleDto | RoleResponse |
| MenuDto | MenuResponse |
| PermissionDto | PermissionResponse |
| LoginResult | 删除（使用框架 TokenInfo） |

### 2. 命名规范统一

**Service 类**：

| Before | After |
|--------|-------|
| AdminManagementAppService | AdminUserManagementAppService |
| AdminAuthAppService | AdminUserAuthAppService |
| AdminPermissionAppService | AdminUserPermissionAppService |

**Command 类**：

| Before | After |
|--------|-------|
| AdminLoginCommand | AdminUserLoginCommand |

**保持不变**：`AdminUserController`、`AdminUser`、`AdminRole` 等已经是正确命名

### 3. 错误码重命名

**AdminError → AdminMessage**

不只包含错误码，以后还可以放提醒、提示等消息。

**新增错误码**：
- 删除：`ADMIN_SYSTEM_CANNOT_DELETE`
- 新增：`LAST_ADMIN_CANNOT_DELETE` - 不能删除最后一个管理员

### 4. 系统管理员删除保护

**实现位置**：Application Service 层

```java
@Transactional
public void delete(Long id) {
    if (adminUserRepository.count() <= 1) {
        throw new ApplicationException(AdminMessage.LAST_ADMIN_CANNOT_DELETE);
    }
    AdminUser adminUser = adminUserRepository.findById(id)
        .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));
    adminUserRepository.delete(adminUser);
}
```

**清理**：
- 删除 `AdminUser.isSystem` 字段
- 删除相关构造函数参数和逻辑

### 5. 使用框架 AuthenticationService

**Service 层改造**：

```java
// Before
private final StpLogic adminStpLogic;

// After
private final AuthenticationService authenticationService;
```

**Controller 层改造**：

```java
// 登录 - 返回 TokenInfo
@PostMapping("/login")
public ApiResponse<TokenInfo> login(@Valid @RequestBody AdminUserLoginCommand command) {
    return ApiResponse.ok(adminAuthAppService.login(command));
}

// 获取当前用户 - 使用 @CurrentUser 注解
@GetMapping("/current")
@RequireAuth
public ApiResponse<CurrentUserResponse> getCurrentAdmin(@CurrentUser Long userId) {
    return ApiResponse.ok(adminAuthAppService.getCurrentAdmin(userId));
}

// 当前用户改自己的密码
@PutMapping("/current/password")
@RequireAuth
public ApiResponse<Void> updatePassword(
        @CurrentUser Long userId,
        @Valid @RequestBody UpdatePasswordCommand command) {
    adminAuthAppService.updatePassword(userId, command.oldPassword(), command.newPassword());
    return ApiResponse.ok();
}

// 管理员重置某个用户的密码
@PutMapping("/{id}/password")
@RequireAuth
@RequirePermission("admin:user:write")
public ApiResponse<Void> resetPassword(
        @PathVariable Long id,
        @Valid @RequestBody ResetPasswordCommand command) {
    adminAuthAppService.resetPassword(id, command.newPassword());
    return ApiResponse.ok();
}
```

**新增 Command**：

```java
public record UpdatePasswordCommand(
    @NotBlank String oldPassword,
    @NotBlank String newPassword
) {}

public record ResetPasswordCommand(
    @NotBlank String newPassword
) {}
```

### 6. 清理工作

| 任务 | 说明 |
|------|------|
| 更新 import | `.dto.query.` → `.dto.response.` |
| 删除 JpaAuditConfig | 框架已有 JpaAuditingConfiguration |
| 清理 isSystem 字段 | 从 AdminUser 中移除 |

## 影响范围

### 重命名的文件
- `AdminError.java` → `AdminMessage.java`
- `AdminDto.java` → `AdminUserResponse.java`
- `RoleDto.java` → `RoleResponse.java`
- `MenuDto.java` → `MenuResponse.java`
- `PermissionDto.java` → `PermissionResponse.java`
- `LoginResult.java` → 删除
- `AdminLoginCommand.java` → `AdminUserLoginCommand.java`
- `AdminManagementAppService.java` → `AdminUserManagementAppService.java`
- `AdminAuthAppService.java` → `AdminUserAuthAppService.java`
- `AdminPermissionAppService.java` → `AdminUserPermissionAppService.java`

### 新增的文件
- `CurrentUserResponse.java`
- `UpdatePasswordCommand.java`
- `ResetPasswordCommand.java`

### 删除的文件
- `JpaAuditConfig.java`

### 修改的文件
- `AdminUser.java` - 移除 isSystem 字段
- `AdminUserController.java` - 使用 @CurrentUser 注解
- `AdminAuthController.java` - 返回类型改为 TokenInfo，使用 @CurrentUser
- 所有引用 `.dto.query.` 的文件 - 改为 `.dto.response.`

## 框架依赖

需要 cartisan-boot 框架提供以下功能（已沟通）：

1. **cartisan-security**
   - `AuthenticationService.login(Long loginId, long timeoutSeconds)` - 支持自定义超时
   - `AuthenticationService.getCurrentUserId()` - 获取当前用户 ID 的便捷方法

## 测试策略

1. **Application Service 层测试** - 验证业务逻辑
2. **Controller 层测试** - 使用 MockMvc 验证 API 行为
3. **集成测试** - 验证完整流程
