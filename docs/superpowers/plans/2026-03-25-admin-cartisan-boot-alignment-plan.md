# Admin 上下文 cartisan-boot 规范对齐实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 使 Admin 上下文实现完全符合 cartisan-boot 使用手册 v0.9 规范

**Architecture:** DDD 六边形架构，枚举改造使用 BaseEnum 接口，实体字段使用 @EnumConvert 注解

**Tech Stack:** Java 21, Spring Boot 3.4, cartisan-boot 框架, JUnit 5, AssertJ, Flyway

---

## 文件结构

### 新增文件
- `server/src/main/resources/db/migration/V10__admin_enum_alignment.sql` - 数据库迁移脚本

### 修改文件
- `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java` - AdminUserStatus 枚举 + @EnumConvert
- `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminMenu.java` - type 字段 @EnumConvert
- `server/src/main/java/com/aieducenter/admin/domain/entity/MenuType.java` - 实现 BaseEnum
- `server/src/main/java/com/aieducenter/admin/application/dto/response/AdminUserResponse.java` - status 字段类型
- `server/src/main/java/com/aieducenter/admin/application/dto/query/AdminUserQuery.java` - status 字段类型
- `server/src/main/java/com/aieducenter/admin/application/mapper/AdminUserMapper.java` - 移除手动方法
- `server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java` - orElseThrow → requirePresent + updateStatus 参数
- `server/src/main/java/com/aieducenter/admin/application/AdminUserPermissionAppService.java` - orElseThrow → requirePresent
- `server/src/main/java/com/aieducenter/admin/application/RoleManagementAppService.java` - orElseThrow → requirePresent
- `server/src/main/java/com/aieducenter/admin/application/MenuManagementAppService.java` - orElseThrow → requirePresent
- `server/src/main/java/com/aieducenter/admin/application/AdminUserAuthAppService.java` - orElseThrow → requirePresent
- `server/src/main/java/com/aieducenter/admin/web/controller/AdminUserController.java` - updateStatus 参数类型
- `server/src/main/java/com/aieducenter/admin/package-info.java` - 添加 @BoundedContext

---

## Task 1: MenuType 枚举实现 BaseEnum

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/entity/MenuType.java`

- [ ] **Step 1: 修改 MenuType 枚举**

完全替换文件内容：

```java
package com.aieducenter.admin.domain.entity;

import com.cartisan.core.domain.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型枚举。
 *
 * @since 0.1.0
 */
@Getter
@AllArgsConstructor
public enum MenuType implements BaseEnum<MenuType> {
    /**
     * 普通菜单（可点击，有路由）。
     */
    MENU(1, "菜单"),

    /**
     * 分组标题（不可点击，纯展示，可带图标）。
     */
    GROUP(2, "分组"),

    /**
     * 分隔线。
     */
    DIVIDER(3, "分隔线");

    private final Integer code;
    private final String name;
}
```

- [ ] **Step 2: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/entity/MenuType.java
git commit -m "refactor: MenuType implements BaseEnum with @Getter/@AllArgsConstructor"
```

---

## Task 2: AdminUserStatus 枚举实现 BaseEnum

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java`

- [ ] **Step 1: 修改 AdminUserStatus 枚举**

将 AdminUserStatus 枚举（约在第 88-105 行）替换为：

```java
    /**
     * 管理员状态枚举。
     */
    @Getter
    @AllArgsConstructor
    public enum AdminUserStatus implements BaseEnum<AdminUserStatus> {
        /**
         * 激活。
         */
        ACTIVE(1, "激活"),

        /**
         * 禁用。
         */
        DISABLED(0, "禁用");

        private final Integer code;
        private final String name;
    }
```

- [ ] **Step 2: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java
git commit -m "refactor: AdminUserStatus implements BaseEnum with @Getter/@AllArgsConstructor"
```

---

## Task 3: AdminUser 实体字段使用 @EnumConvert

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java`

- [ ] **Step 1: 添加导入**

在文件顶部的 import 区域添加：

```java
import com.cartisan.data.jpa.annotation.EnumConvert;
```

- [ ] **Step 2: 修改 status 字段注解**

将（约在第 76-79 行）：

```java
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AdminUserStatus status;
```

替换为：

```java
    @Getter
    @EnumConvert(AdminUserStatus.class)
    @Column(name = "status", nullable = false)
    private AdminUserStatus status;
```

- [ ] **Step 3: 移除不再需要的导入**

删除以下导入（如果存在）：
```java
import jakarta.persistence.Enumerated;
```

- [ ] **Step 4: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java
git commit -m "refactor: use @EnumConvert for AdminUser.status field"
```

---

## Task 4: AdminMenu 实体字段使用 @EnumConvert

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminMenu.java`

- [ ] **Step 1: 添加导入**

在文件顶部的 import 区域添加：

```java
import com.cartisan.data.jpa.annotation.EnumConvert;
```

- [ ] **Step 2: 修改 type 字段注解**

将（约在第 65-67 行）：

```java
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MenuType type = MenuType.MENU;
```

替换为：

```java
    @EnumConvert(MenuType.class)
    @Column(name = "type", nullable = false)
    private MenuType type = MenuType.MENU;
```

- [ ] **Step 3: 移除不再需要的导入**

删除以下导入（如果存在）：
```java
import jakarta.persistence.Enumerated;
```

- [ ] **Step 4: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminMenu.java
git commit -m "refactor: use @EnumConvert for AdminMenu.type field"
```

---

## Task 5: AdminUserResponse DTO 字段类型修改

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/dto/response/AdminUserResponse.java`

- [ ] **Step 1: 添加导入**

在文件顶部添加：

```java
import com.aieducenter.admin.domain.aggregate.AdminUser;
```

- [ ] **Step 2: 修改 status 字段**

将：
```java
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
) {}
```

替换为：
```java
public record AdminUserResponse(
    Long id,
    String username,
    String nickname,
    String email,
    String phone,
    String avatar,
    AdminUser.AdminUserStatus status,
    String statusName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

- [ ] **Step 3: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

**说明**：MapStruct 会自动将 `BaseEnum.getName()` 映射到 `statusName` 字段，无需额外配置。

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/dto/response/AdminUserResponse.java
git commit -m "refactor: AdminUserResponse.status uses enum type with auto serialization"
```

---

## Task 6: AdminUserQuery DTO 字段类型修改

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/dto/query/AdminUserQuery.java`

- [ ] **Step 1: 修改 status 字段类型**

将：
```java
public record AdminUserQuery(
    @Condition(type = ConditionType.INNER_LIKE) String username,
    @Condition(type = ConditionType.EQUAL) AdminUser.AdminUserStatus status,
    @Condition(blurry = "username,nickname,email") String keyword
) {}
```

替换为：
```java
public record AdminUserQuery(
    @Condition(type = ConditionType.INNER_LIKE) String username,
    @Condition(type = ConditionType.EQUAL) AdminUser.AdminUserStatus status,
    @Condition(blurry = "username,nickname,email") String keyword
) {}
```

**说明**：AdminUserStatus 保持为 `AdminUser` 的内部枚举，因此 `AdminUserQuery` 无需修改。前端传参时传整型 code（如 `1`），Jackson 会自动反序列化为 `AdminUserStatus.ACTIVE`。

- [ ] **Step 2: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/dto/query/AdminUserQuery.java
git commit -m "refactor: confirm AdminUserQuery.status uses enum type"
```

---

## Task 7: AdminUserMapper 移除手动方法

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/mapper/AdminUserMapper.java`

- [ ] **Step 1: 删除手动 mapStatus 方法**

删除以下内容（约在第 22-31 行）：

```java
    /**
     * 将 Optional&lt;String&gt; 转换为 String（null 处理）。
     */
    default String mapOptionalString(Optional<String> optional) {
        return optional.orElse(null);
    }

    /**
     * 将 AdminUserStatus 转换为字符串。
     */
    default String mapStatus(AdminUser.AdminUserStatus status) {
        return status != null ? status.name() : null;
    }
```

**说明**：AdminUserResponse 中所有字段都是 `String` 类型，无 `Optional` 字段，因此 `mapOptionalString` 方法也应删除。

- [ ] **Step 2: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/mapper/AdminUserMapper.java
git commit -m "refactor: remove manual mapStatus method, MapStruct handles BaseEnum automatically"
```

---

## Task 8: AdminUserManagementAppService.updateStatus 方法参数类型修改

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java`
- Modify: `server/src/main/java/com/aieducenter/admin/web/controller/AdminUserController.java`

- [ ] **Step 1: 修改 updateStatus 方法签名**

将（约在第 147-162 行）：

```java
    /**
     * 修改管理员状态。
     *
     * @param id 管理员 ID
     * @param status 状态字符串（ACTIVE/DISABLED）
     */
    @Transactional
    public void updateStatus(Long id, String status) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        AdminUser.AdminUserStatus statusEnum = AdminUser.AdminUserStatus.fromString(status);
        if (statusEnum == AdminUser.AdminUserStatus.ACTIVE) {
            adminUser.enable();
        } else {
            adminUser.disable();
        }

        adminUserRepository.save(adminUser);
    }
```

替换为：

```java
    /**
     * 修改管理员状态。
     *
     * @param id 管理员 ID
     * @param status 状态枚举
     */
    @Transactional
    public void updateStatus(Long id, AdminUser.AdminUserStatus status) {
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(id),
                AdminMessage.ADMIN_NOT_FOUND
        );

        if (status == AdminUser.AdminUserStatus.ACTIVE) {
            adminUser.enable();
        } else {
            adminUser.disable();
        }

        adminUserRepository.save(adminUser);
    }
```

- [ ] **Step 2: 修改 Controller 方法参数**

修改 `AdminUserController.updateStatus()` 方法（约在第 112-115 行）：

```java
// 修改前
@PutMapping("/{id}/status")
@RequireAuth
@RequirePermission(
    value = "admin:user:write",
    name = "平台管理 / 用户管理 / 编辑",
    scope = "admin"
)
@Operation(summary = "修改管理员状态")
public void updateStatus(
        @PathVariable Long id,
        @RequestParam String status) {
    adminManagementAppService.updateStatus(id, status);
}

// 修改后
@PutMapping("/{id}/status")
@RequireAuth
@RequirePermission(
    value = "admin:user:write",
    name = "平台管理 / 用户管理 / 编辑",
    scope = "admin"
)
@Operation(summary = "修改管理员状态")
public void updateStatus(
        @PathVariable Long id,
        @RequestParam AdminUser.AdminUserStatus status) {
    adminManagementAppService.updateStatus(id, status);
}
```

**说明**：Jackson 会自动将前端传的整型 code（如 `1`）反序列化为 `AdminUserStatus.ACTIVE`。

- [ ] **Step 3: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java
git add server/src/main/java/com/aieducenter/admin/web/controller/AdminUserController.java
git commit -m "refactor: updateStatus accepts enum instead of string"
```

---

## Task 9: AdminUserManagementAppService 使用 Assertions.requirePresent

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java`

- [ ] **Step 1: 修改 findById 方法（第 74-78 行）**

将：
```java
    @Transactional(readOnly = true)
    public AdminUserResponse findById(Long id) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        return adminUserMapper.convert(adminUser);
    }
```

替换为：
```java
    @Transactional(readOnly = true)
    public AdminUserResponse findById(Long id) {
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(id),
                AdminMessage.ADMIN_NOT_FOUND
        );

        return adminUserMapper.convert(adminUser);
    }
```

- [ ] **Step 2: 修改 update 方法（第 107-109 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(id),
                AdminMessage.ADMIN_NOT_FOUND
        );
```

- [ ] **Step 3: 修改 delete 方法（第 137-138 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(id),
                AdminMessage.ADMIN_NOT_FOUND
        );
```

- [ ] **Step 4: 修改 updateStatus 方法（第 151-152 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(id),
                AdminMessage.ADMIN_NOT_FOUND
        );
```

- [ ] **Step 5: 修改 assignRoles 方法（第 169-170 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(id),
                AdminMessage.ADMIN_NOT_FOUND
        );
```

- [ ] **Step 6: 修改 assignRoles 中的角色验证（第 174-175 行）**

将：
```java
            adminRoleRepository.findById(roleId)
                    .orElseThrow(() -> new ApplicationException(AdminMessage.ROLE_NOT_FOUND));
```

替换为：
```java
            Assertions.requirePresent(
                    adminRoleRepository.findById(roleId),
                    AdminMessage.ROLE_NOT_FOUND
            );
```

- [ ] **Step 7: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/AdminUserManagementAppService.java
git commit -m "refactor: use Assertions.requirePresent in AdminUserManagementAppService"
```

---

## Task 10: AdminUserPermissionAppService 使用 Assertions.requirePresent

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminUserPermissionAppService.java`

- [ ] **Step 1: 添加导入**

添加：
```java
import com.cartisan.core.util.Assertions;
```

- [ ] **Step 2: 修改 getPermissions 方法（第 53-54 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow();
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(adminId)
        );
```

- [ ] **Step 3: 修改 getRoleCodes 方法（第 80-81 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow();
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(adminId)
        );
```

- [ ] **Step 4: 修改 getRoleCodes 中的角色查找（第 85 行）**

将：
```java
        return adminUser.getRoleIds().stream()
                .map(roleId -> adminRoleRepository.findById(roleId).orElseThrow())
```

替换为：
```java
        return adminUser.getRoleIds().stream()
                .map(roleId -> Assertions.requirePresent(adminRoleRepository.findById(roleId)))
```

- [ ] **Step 5: 修改 getMenus 方法（第 102-103 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow();
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(adminId)
        );
```

- [ ] **Step 6: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/AdminUserPermissionAppService.java
git commit -m "refactor: use Assertions.requirePresent in AdminUserPermissionAppService"
```

---

## Task 11: RoleManagementAppService 使用 Assertions.requirePresent

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/RoleManagementAppService.java`

- [ ] **Step 1: 添加导入**

添加：
```java
import com.cartisan.core.util.Assertions;
```

- [ ] **Step 2: 修改 update 方法（第 90-91 行）**

将：
```java
        AdminRole role = roleRepository.findById(id)
                .orElseThrow(() -> new DomainException(AdminMessage.ROLE_NOT_FOUND));
```

替换为：
```java
        AdminRole role = Assertions.requirePresent(
                roleRepository.findById(id),
                AdminMessage.ROLE_NOT_FOUND
        );
```

- [ ] **Step 3: 修改 update 中的 code 唯一性检查（第 95-97 行）**

将：
```java
            roleRepository.findByCode(command.code()).ifPresent(existing -> {
                throw new DomainException(AdminMessage.ROLE_CODE_ALREADY_EXISTS);
            });
```

保持不变（这不是 orElseThrow 模式）。

- [ ] **Step 4: 修改 delete 方法（第 112-113 行）**

将：
```java
        AdminRole role = roleRepository.findById(id)
                .orElseThrow(() -> new DomainException(AdminMessage.ROLE_NOT_FOUND));
```

替换为：
```java
        AdminRole role = Assertions.requirePresent(
                roleRepository.findById(id),
                AdminMessage.ROLE_NOT_FOUND
        );
```

- [ ] **Step 5: 修改 assignMenus 方法（第 134-135 行）**

将：
```java
        AdminRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new DomainException(AdminMessage.ROLE_NOT_FOUND));
```

替换为：
```java
        AdminRole role = Assertions.requirePresent(
                roleRepository.findById(roleId),
                AdminMessage.ROLE_NOT_FOUND
        );
```

- [ ] **Step 6: 修改 assignMenus 中的菜单验证（第 139-141 行）**

将：
```java
            if (menuRepository.findById(menuId).isEmpty()) {
                throw new DomainException(AdminMessage.MENU_NOT_FOUND);
            }
```

替换为：
```java
            Assertions.requirePresent(
                    menuRepository.findById(menuId),
                    AdminMessage.MENU_NOT_FOUND
            );
```

- [ ] **Step 7: 修改 assignPermissions 方法（第 158-159 行）**

将：
```java
        AdminRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new DomainException(AdminMessage.ROLE_NOT_FOUND));
```

替换为：
```java
        AdminRole role = Assertions.requirePresent(
                roleRepository.findById(roleId),
                AdminMessage.ROLE_NOT_FOUND
        );
```

- [ ] **Step 8: 修改 findById 方法（第 184-185 行）**

将：
```java
        AdminRole role = roleRepository.findById(id)
                .orElseThrow(() -> new DomainException(AdminMessage.ROLE_NOT_FOUND));
```

替换为：
```java
        AdminRole role = Assertions.requirePresent(
                roleRepository.findById(id),
                AdminMessage.ROLE_NOT_FOUND
        );
```

- [ ] **Step 9: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 10: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/RoleManagementAppService.java
git commit -m "refactor: use Assertions.requirePresent in RoleManagementAppService"
```

---

## Task 12: MenuManagementAppService 使用 Assertions.requirePresent

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/MenuManagementAppService.java`

- [ ] **Step 1: 添加导入**

添加：
```java
import com.cartisan.core.util.Assertions;
```

- [ ] **Step 2: 修改 create 方法中的父菜单验证（第 77-78 行）**

将：
```java
            if (menuRepository.findById(command.parentId()).isEmpty()) {
                throw new DomainException(AdminMessage.MENU_NOT_FOUND);
            }
```

替换为：
```java
            Assertions.requirePresent(
                    menuRepository.findById(command.parentId()),
                    AdminMessage.MENU_NOT_FOUND
            );
```

- [ ] **Step 3: 修改 update 方法（第 98-99 行）**

将：
```java
        AdminMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new DomainException(AdminMessage.MENU_NOT_FOUND));
```

替换为：
```java
        AdminMenu menu = Assertions.requirePresent(
                menuRepository.findById(id),
                AdminMessage.MENU_NOT_FOUND
        );
```

- [ ] **Step 4: 修改 update 中的父菜单验证（第 108-109 行）**

将：
```java
            AdminMenu parent = menuRepository.findById(command.parentId())
                    .orElseThrow(() -> new DomainException(AdminMessage.MENU_NOT_FOUND));
```

替换为：
```java
            AdminMenu parent = Assertions.requirePresent(
                    menuRepository.findById(command.parentId()),
                    AdminMessage.MENU_NOT_FOUND
            );
```

- [ ] **Step 5: 修改 delete 方法（第 136-137 行）**

将：
```java
        AdminMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new DomainException(AdminMessage.MENU_NOT_FOUND));
```

替换为：
```java
        AdminMenu menu = Assertions.requirePresent(
                menuRepository.findById(id),
                AdminMessage.MENU_NOT_FOUND
        );
```

- [ ] **Step 6: 修改 findById 方法（第 206-207 行）**

将：
```java
        AdminMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new DomainException(AdminMessage.MENU_NOT_FOUND));
```

替换为：
```java
        AdminMenu menu = Assertions.requirePresent(
                menuRepository.findById(id),
                AdminMessage.MENU_NOT_FOUND
        );
```

- [ ] **Step 7: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/MenuManagementAppService.java
git commit -m "refactor: use Assertions.requirePresent in MenuManagementAppService"
```

---

## Task 13: AdminUserAuthAppService 使用 Assertions.requirePresent

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminUserAuthAppService.java`

- [ ] **Step 1: 添加导入**

添加：
```java
import com.cartisan.core.util.Assertions;
```

- [ ] **Step 2: 修改 login 方法（第 57-58 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findByUsername(command.username())
                .orElseThrow(() -> new ApplicationException(AdminMessage.LOGIN_FAILED));
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findByUsername(command.username()),
                AdminMessage.LOGIN_FAILED
        );
```

- [ ] **Step 3: 修改 updatePassword 方法（第 85-86 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(userId),
                AdminMessage.ADMIN_NOT_FOUND
        );
```

- [ ] **Step 4: 修改 resetPassword 方法（第 97-98 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(userId),
                AdminMessage.ADMIN_NOT_FOUND
        );
```

- [ ] **Step 5: 修改 getCurrentUser 方法（第 109-110 行）**

将：
```java
        AdminUser adminUser = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));
```

替换为：
```java
        AdminUser adminUser = Assertions.requirePresent(
                adminUserRepository.findById(userId),
                AdminMessage.ADMIN_NOT_FOUND
        );
```

- [ ] **Step 6: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/AdminUserAuthAppService.java
git commit -m "refactor: use Assertions.requirePresent in AdminUserAuthAppService"
```

---

## Task 14: 添加 @BoundedContext 注解

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/package-info.java`

- [ ] **Step 1: 修改 package-info.java**

将文件内容替换为：

```java
/**
 * Platform Admin Context。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>平台运营人员管理（管理员、角色、权限、菜单）</li>
 *   <li>管理员登录认证</li>
 *   <li>RBAC 权限体系</li>
 * </ul>
 *
 * <h3>限界上下文</h3>
 * <p>平台运营后台，独立于 Account Context 和 Tenant Context</p>
 *
 * <h3>包结构</h3>
 * <ul>
 *   <li>domain - 领域层：聚合根、实体、仓储接口、领域服务</li>
 *   <li>application - 应用层：应用服务、DTO</li>
 *   <li>infrastructure - 基础设施层：仓储实现</li>
 *   <li>web - 表现层：控制器</li>
 * </ul>
 *
 * @since 0.1.0
 */
@BoundedContext(name = "PlatformAdmin", subDomain = SubDomain.SUPPORT)
package com.aieducenter.admin;

import com.cartisan.core.domain.SubDomain;
import com.cartisan.core.stereotype.BoundedContext;
```

- [ ] **Step 2: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/package-info.java
git commit -m "refactor: add @BoundedContext annotation to admin package"
```

---

## Task 15: 创建 Flyway 迁移脚本

**Files:**
- Create: `server/src/main/resources/db/migration/V10__admin_enum_alignment.sql`

- [ ] **Step 1: 创建迁移脚本**

创建文件 `server/src/main/resources/db/migration/V10__admin_enum_alignment.sql`：

```sql
-- 修改 sys_admin_users.status 列：VARCHAR(20) → INTEGER
ALTER TABLE sys_admin_users
  ALTER COLUMN status TYPE INTEGER USING (
    CASE status
      WHEN 'ACTIVE' THEN 1
      WHEN 'DISABLED' THEN 0
      ELSE 1
    END
  );

-- 修改 sys_admin_users.status 默认值
ALTER TABLE sys_admin_users
  ALTER COLUMN status SET DEFAULT 1;

-- 修改 sys_admin_menus.type 列：VARCHAR(20) → INTEGER
ALTER TABLE sys_admin_menus
  ALTER COLUMN type TYPE INTEGER USING (
    CASE type
      WHEN 'MENU' THEN 1
      WHEN 'GROUP' THEN 2
      WHEN 'DIVIDER' THEN 3
      ELSE 1
    END
  );

-- 修改 sys_admin_menus.type 默认值
ALTER TABLE sys_admin_menus
  ALTER COLUMN type SET DEFAULT 1;
```

**注意**：版本号 V10 是基于现有迁移版本 V9 的后续版本。如果已有 V10 或更高版本，需要相应调整。

- [ ] **Step 2: 验证迁移脚本语法**

Run: `cd server && ./gradlew flywayValidate`
Expected: SUCCESS (或根据现有配置输出)

- [ ] **Step 3: 提交**

```bash
git add server/src/main/resources/db/migration/V10__admin_enum_alignment.sql
git commit -m "refactor: add Flyway migration for enum alignment (VARCHAR to INTEGER)"
```

---

## Task 16: 全量测试和验证

**Files:**
- Test: All modified files

- [ ] **Step 1: 运行全量检查**

Run: `cd server && ./gradlew check`
Expected: BUILD SUCCESSFUL（包含 ArchUnit 规则检查）

- [ ] **Step 2: 运行单元测试**

Run: `cd server && ./gradlew test`
Expected: ALL TESTS PASSED

- [ ] **Step 3: 启动应用验证**

Run: `cd server && ./gradlew bootRun`
Expected: 应用成功启动，无异常

- [ ] **Step 4: API 接口测试（手动或 MockMvc）**

验证以下接口：
1. `GET /api/v1/admin/users` - 检查 status 返回整型
2. `GET /api/v1/admin/users/{id}` - 检查 status 和 statusName 字段
3. `POST /api/v1/admin/users` - 检查请求参数

- [ ] **Step 5: 提交最终修复（如有）**

```bash
# 如果测试过程中发现问题，修复后提交
git add .
git commit -m "test: fix issues found during testing"
```

---

## 注意事项

1. **Flyway 版本号**：Task 14 中的 V1 需要根据现有迁移版本调整
2. **数据库可清空重建**：如果数据库有数据，需要先导出备份
3. **前端接口变化**：
   - `status` 从字符串变为整型（如 `"ACTIVE"` → `1`）
   - 新增 `statusName` 字段（如 `"激活"`）
4. **MapStruct 自动处理**：BaseEnum 的 `getCode()`/`getName()` 无需手动配置
5. **Jackson 序列化**：BaseEnum 自动序列化为 code，无需额外配置

## 完成标准

- [ ] 所有枚举实现 BaseEnum 接口
- [ ] 所有实体枚举字段使用 @EnumConvert 注解
- [ ] 所有 orElseThrow 替换为 Assertions.requirePresent
- [ ] @BoundedContext 注解已添加
- [ ] Flyway 迁移脚本已创建
- [ ] ArchUnit 规则检查通过
- [ ] 所有单元测试通过
- [ ] API 接口测试验证通过
