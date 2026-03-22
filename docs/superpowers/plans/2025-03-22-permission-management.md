# 权限管理重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 重构权限管理，权限从代码注解扫描而非数据库维护

**架构:** 移除 admin_permissions 表，admin_role_permissions 直接存储权限 code 和 name，通过 cartisan-boot 的 PermissionScanner 扫描注解定义的权限

**技术栈:** Java 21, Spring Boot 3.4, JPA, cartisan-boot

---

## 权限 Code 规范

- **格式:** `{scope}:{module}:{action}`
- **后台管理 scope:** `admin`
- **示例:**
  - `admin:menu:read` → 平台管理 / 菜单管理 / 查看
  - `admin:role:write` → 平台管理 / 角色管理 / 编辑

---

## 文件结构概览

### 需要修改的文件
| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `V5__create_admin_tables.sql` | 修改 | 删除 admin_permissions 表，修改 admin_role_permissions |
| `AdminRolePermission.java` | 修改 | 改为存储 permission_code 和 permission_name |
| `AdminRole.java` | 修改 | permissionIds 改为 permissionCodes |
| `AdminRoleRepository.java` | 修改 | assignPermissions 方法参数改为 List<String> |
| `JpaAdminRoleRepository.java` | 修改 | 实现变更的 assignPermissions 方法 |
| `RoleManagementAppService.java` | 修改 | 分配权限逻辑改为接收 permission codes |
| `AssignPermissionsCommand.java` | 修改 | 字段改为 permissionCodes |
| `RoleDto.java` | 修改 | 返回 permissionCodes |
| `AdminPermissionController.java` | 删除 | 不再需要独立的权限管理接口 |
| `PermissionManagementAppService.java` | 删除 | 不再需要 |
| `AdminPermissionRepository.java` | 删除 | 不再需要 |
| 所有 Controller 的 @RequirePermission | 修改 | 添加 name 和 scope 属性 |

### 需要新建的文件
| 文件 | 说明 |
|------|------|
| `PermissionScanAppService.java` | 权限扫描应用服务 |
| `PermissionDto.java` | 权限 DTO |
| `PermissionController.java` | 权限查询接口（扫描结果） |

---

### Task 1: 修改数据库迁移脚本

**文件:**
- Modify: `server/src/main/resources/db/migration/V5__create_admin_tables.sql`

- [ ] **Step 1: 修改 V5 脚本**

删除 `admin_permissions` 表，修改 `admin_role_permissions` 表结构：

```sql
-- ========================================================================
-- Platform Admin Context: 管理员、角色、菜单表
-- ========================================================================

-- 管理员表
CREATE TABLE admin_users (
    id              BIGINT PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    nickname        VARCHAR(50) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(20),
    avatar          VARCHAR(512),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    system          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- 角色表
CREATE TABLE admin_roles (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(50) NOT NULL UNIQUE,
    code            VARCHAR(50) NOT NULL UNIQUE,
    description     VARCHAR(255),
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- 菜单表
CREATE TABLE admin_menus (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    path            VARCHAR(255),
    icon            VARCHAR(50),
    parent_id       BIGINT,
    sort_order      INT NOT NULL DEFAULT 0,
    type            VARCHAR(20) NOT NULL DEFAULT 'MENU',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- 管理员-角色关联表
CREATE TABLE admin_user_roles (
    admin_id        BIGINT NOT NULL,
    role_id         BIGINT NOT NULL,
    PRIMARY KEY (admin_id, role_id)
);

-- 角色-菜单关联表
CREATE TABLE admin_role_menus (
    role_id         BIGINT NOT NULL,
    menu_id         BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 角色-权限表（存储权限快照）
CREATE TABLE admin_role_permissions (
    role_id            BIGINT NOT NULL,
    permission_code    VARCHAR(100) NOT NULL,
    permission_name    VARCHAR(255),
    PRIMARY KEY (role_id, permission_code)
);

-- 索引
CREATE INDEX idx_admin_users_username ON admin_users(username) WHERE deleted = FALSE;
CREATE INDEX idx_admin_menus_parent_id ON admin_menus(parent_id) WHERE deleted = FALSE;

-- 外键
ALTER TABLE admin_menus ADD CONSTRAINT fk_admin_menus_parent
    FOREIGN KEY (parent_id) REFERENCES admin_menus(id);

ALTER TABLE admin_user_roles ADD CONSTRAINT fk_admin_user_roles_admin
    FOREIGN KEY (admin_id) REFERENCES admin_users(id);
ALTER TABLE admin_user_roles ADD CONSTRAINT fk_admin_user_roles_role
    FOREIGN KEY (role_id) REFERENCES admin_roles(id);

ALTER TABLE admin_role_menus ADD CONSTRAINT fk_admin_role_menus_role
    FOREIGN KEY (role_id) REFERENCES admin_roles(id);
ALTER TABLE admin_role_menus ADD CONSTRAINT fk_admin_role_menus_menu
    FOREIGN KEY (menu_id) REFERENCES admin_menus(id);

ALTER TABLE admin_role_permissions ADD CONSTRAINT fk_admin_role_permissions_role
    FOREIGN KEY (role_id) REFERENCES admin_roles(id);
```

---

### Task 2: 修改 AdminRolePermission 实体

**文件:**
- Modify: `server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminRolePermission.java`

- [ ] **Step 1: 修改实体类**

```java
package com.aieducenter.admin.infrastructure.persistence;

import jakarta.persistence.*;

/**
 * 角色-权限关联实体。
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_role_permissions")
public class AdminRolePermission {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "permission_code")
    private String permissionCode;

    @Column(name = "permission_name")
    private String permissionName;

    public AdminRolePermission() {
    }

    public AdminRolePermission(Long roleId, String permissionCode, String permissionName) {
        this.roleId = roleId;
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }
}
```

- [ ] **Step 2: 删除 ID 类文件**

Delete: `server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminRolePermissionId.java`

- [ ] **Step 3: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminRolePermission.java
git rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminRolePermissionId.java
git commit -m "refactor: modify AdminRolePermission to store permission code and name"
```

---

### Task 3: 修改 AdminRole 实体

**文件:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/entity/AdminRole.java`

- [ ] **Step 1: 修改 permissionIds 为 permissionCodes**

```java
// 关联的权限 Code（不持久化，仅用于查询时组装）
@Transient
private Set<String> permissionCodes = new HashSet<>();

// 删除原来的 permissionIds 字段

// 修改 getter/setter
public Set<String> getPermissionCodes() {
    return permissionCodes;
}

public void setPermissionCodes(Set<String> permissionCodes) {
    this.permissionCodes = permissionCodes != null ? permissionCodes : new HashSet<>();
}
```

完整修改后的类：

```java
package com.aieducenter.admin.domain.entity;

import java.util.HashSet;
import java.util.Set;

import com.cartisan.data.jpa.domain.SoftDeletable;

import jakarta.persistence.*;

/**
 * AdminRole 实体。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>封装角色状态</li>
 *   <li>管理角色关联的菜单和权限</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_roles")
public class AdminRole extends SoftDeletable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // 关联的菜单（不持久化，仅用于查询时组装）
    @Transient
    private Set<Long> menuIds = new HashSet<>();

    // 关联的权限 Code（不持久化，仅用于查询时组装）
    @Transient
    private Set<String> permissionCodes = new HashSet<>();

    /**
     * 创建角色。
     */
    public AdminRole(String name, String code, String description, Integer sortOrder) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    /**
     * JPA 默认构造函数。
     */
    protected AdminRole() {
    }

    /**
     * JPA 保存前生成 ID。
     */
    @PrePersist
    void prePersist() {
        if (id == null) {
            this.id = com.cartisan.data.jpa.id.TsidGenerator.newInstance().generate();
        }
    }

    // ========== Getter ==========

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Set<Long> getMenuIds() {
        return menuIds;
    }

    public Set<String> getPermissionCodes() {
        return permissionCodes;
    }

    // ========== Setter ==========

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setMenuIds(Set<Long> menuIds) {
        this.menuIds = menuIds != null ? menuIds : new HashSet<>();
    }

    public void setPermissionCodes(Set<String> permissionCodes) {
        this.permissionCodes = permissionCodes != null ? permissionCodes : new HashSet<>();
    }

    // ========== 业务行为 ==========

    /**
     * 是否为超级管理员角色。
     */
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(this.code);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/entity/AdminRole.java
git commit -m "refactor: change permissionIds to permissionCodes in AdminRole"
```

---

### Task 4: 修改 AdminRoleRepository 接口

**文件:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/repository/AdminRoleRepository.java`

- [ ] **Step 1: 修改 assignPermissions 方法签名**

```java
package com.aieducenter.admin.domain.repository;

import java.util.List;
import java.util.Optional;

import com.aieducenter.admin.domain.entity.AdminRole;
import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;

/**
 * 角色仓储接口。
 *
 * @since 0.1.0
 */
@Port(PortType.REPOSITORY)
public interface AdminRoleRepository {

    Optional<AdminRole> findById(Long id);

    Optional<AdminRole> findByCode(String code);

    List<AdminRole> findAll();

    List<AdminRole> findByAdminId(Long adminId);

    AdminRole save(AdminRole role);

    void delete(AdminRole role);

    /**
     * 检查角色是否被管理员使用。
     */
    boolean isUsedByAnyAdmin(Long roleId);

    /**
     * 分配菜单给角色。
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 分配权限给角色。
     *
     * @param roleId 角色 ID
     * @param permissionCodes 权限 Code 列表
     */
    void assignPermissions(Long roleId, List<String> permissionCodes);
}
```

- [ ] **Step 2: 编译验证（预期失败，实现类未修改）**

Run: `cd server && ./gradlew compileJava`
Expected: FAIL (implementation not updated yet)

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/repository/AdminRoleRepository.java
git commit -m "refactor: change assignPermissions parameter to permission codes"
```

---

### Task 5: 修改 JpaAdminRoleRepository 实现

**文件:**
- Modify: `server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminRoleRepository.java`

- [ ] **Step 1: 先查看现有实现**

Run: `cat server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminRoleRepository.java`

- [ ] **Step 2: 修改 assignPermissions 实现**

根据现有实现，修改为接收 `List<String> permissionCodes`，存储到 `admin_role_permissions` 表。

```java
@Override
public void assignPermissions(Long roleId, List<String> permissionCodes) {
    // 删除原有权限
    entityManager.createQuery(
            "DELETE FROM AdminRolePermission rp WHERE rp.roleId = :roleId")
            .setParameter("roleId", roleId)
            .executeUpdate();

    // 添加新权限
    if (permissionCodes != null && !permissionCodes.isEmpty()) {
        // TODO: 通过 PermissionScanner 获取权限名称
        for (String permissionCode : permissionCodes) {
            AdminRolePermission rp = new AdminRolePermission(roleId, permissionCode, null);
            entityManager.persist(rp);
        }
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminRoleRepository.java
git commit -m "refactor: update assignPermissions to use permission codes"
```

---

### Task 6: 创建权限 DTO

**文件:**
- Create: `server/src/main/java/com/aieducenter/admin/application/dto/query/PermissionDto.java`

- [ ] **Step 1: 创建 PermissionDto**

```java
package com.aieducenter.admin.application.dto.query;

/**
 * 权限 DTO。
 *
 * @since 0.1.0
 */
public record PermissionDto(
    String code,
    String name
) {
}
```

- [ ] **Step 2: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/dto/query/PermissionDto.java
git commit -m "feat: add PermissionDto"
```

---

### Task 7: 创建权限扫描应用服务

**文件:**
- Create: `server/src/main/java/com/aieducenter/admin/application/PermissionScanAppService.java`

- [ ] **Step 1: 创建 PermissionScanAppService**

```java
package com.aieducenter.admin.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aieducenter.admin.application.dto.query.PermissionDto;
import com.cartisan.security.permission.Permission;
import com.cartisan.security.permission.PermissionScanner;

/**
 * 权限扫描应用服务。
 */
@Service
public class PermissionScanAppService {

    private final PermissionScanner permissionScanner;

    public PermissionScanAppService(PermissionScanner permissionScanner) {
        this.permissionScanner = permissionScanner;
    }

    /**
     * 扫描指定 scope 的权限。
     */
    public List<PermissionDto> scanByScope(String scope) {
        return permissionScanner.scanByScope(scope).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 扫描所有权限。
     */
    public List<PermissionDto> scanAll() {
        return permissionScanner.scanAll().stream()
                .map(this::toDto)
                .toList();
    }

    private PermissionDto toDto(Permission permission) {
        return new PermissionDto(permission.code(), permission.name());
    }
}
```

- [ ] **Step 2: 编译验证（预期失败，PermissionScanner 不存在）**

Run: `cd server && ./gradlew compileJava`
Expected: May FAIL if PermissionScanner not available in cartisan-security yet

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/PermissionScanAppService.java
git commit -m "feat: add PermissionScanAppService"
```

---

### Task 8: 创建权限查询控制器

**文件:**
- Create: `server/src/main/java/com/aieducenter/admin/web/controller/PermissionController.java`
- Delete: `server/src/main/java/com/aieducenter/admin/web/controller/AdminPermissionController.java`

- [ ] **Step 1: 创建新的 PermissionController**

```java
package com.aieducenter.admin.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.aieducenter.admin.application.PermissionScanAppService;
import com.aieducenter.admin.application.dto.query.PermissionDto;
import com.cartisan.security.annotation.RequireAuth;
import com.cartisan.security.annotation.RequirePermission;
import com.cartisan.web.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 权限查询控制器。
 */
@RestController
@RequestMapping("/api/v1/admin/permissions")
@Tag(name = "Admin Permissions", description = "权限查询")
public class PermissionController {

    private final PermissionScanAppService permissionScanAppService;

    public PermissionController(PermissionScanAppService permissionScanAppService) {
        this.permissionScanAppService = permissionScanAppService;
    }

    @GetMapping
    @RequireAuth
    @RequirePermission(
        value = "admin:permission:read",
        name = "平台管理 / 权限管理 / 查看",
        scope = "admin"
    )
    @Operation(summary = "查询权限列表（扫描结果）")
    public ApiResponse<List<PermissionDto>> scanPermissions(
            @RequestParam(defaultValue = "admin") String scope) {
        return ApiResponse.ok(permissionScanAppService.scanByScope(scope));
    }
}
```

- [ ] **Step 2: 删除旧的 AdminPermissionController**

Run: `git rm server/src/main/java/com/aieducenter/admin/web/controller/AdminPermissionController.java`

- [ ] **Step 3: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/web/controller/PermissionController.java
git rm server/src/main/java/com/aieducenter/admin/web/controller/AdminPermissionController.java
git commit -m "refactor: replace AdminPermissionController with PermissionController"
```

---

### Task 9: 修改 AssignPermissionsCommand

**文件:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/dto/command/AssignPermissionsCommand.java`

- [ ] **Step 1: 修改字段**

```java
package com.aieducenter.admin.application.dto.command;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

/**
 * 分配权限命令。
 *
 * @since 0.1.0
 */
public record AssignPermissionsCommand(

    @NotEmpty(message = "权限列表不能为空")
    List<String> permissionCodes

) {
}
```

- [ ] **Step 2: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/dto/command/AssignPermissionsCommand.java
git commit -m "refactor: change AssignPermissionsCommand to use permission codes"
```

---

### Task 10: 修改 RoleManagementAppService

**文件:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/RoleManagementAppService.java`

- [ ] **Step 1: 修改 assignPermissions 方法**

```java
/**
 * 为角色分配权限。
 */
@Transactional
public void assignPermissions(Long roleId, AssignPermissionsCommand command) {
    // 验证角色存在
    findById(roleId);

    // 直接使用 permission codes，不再验证权限表
    roleRepository.assignPermissions(roleId, command.permissionCodes());
}
```

完整修改后的方法：

```java
@Transactional
public void assignPermissions(Long roleId, AssignPermissionsCommand command) {
    // 验证角色存在
    findById(roleId);

    roleRepository.assignPermissions(roleId, command.permissionCodes());
}
```

- [ ] **Step 2: 删除 permissionRepository 依赖**

从构造函数和字段中删除 `AdminPermissionRepository`

- [ ] **Step 3: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/RoleManagementAppService.java
git commit -m "refactor: update assignPermissions to use permission codes"
```

---

### Task 11: 修改 RoleDto

**文件:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/dto/query/RoleDto.java`

- [ ] **Step 1: 修改 permissionIds 为 permissionCodes**

```java
package com.aieducenter.admin.application.dto.query;

import java.util.Set;

import com.aieducenter.admin.domain.entity.AdminRole;

/**
 * 角色 DTO。
 *
 * @since 0.1.0
 */
public record RoleDto(
    Long id,
    String name,
    String code,
    String description,
    Integer sortOrder,
    Set<Long> menuIds,
    Set<String> permissionCodes
) {

    public static RoleDto from(AdminRole role) {
        return new RoleDto(
            role.getId(),
            role.getName(),
            role.getCode(),
            role.getDescription(),
            role.getSortOrder(),
            role.getMenuIds(),
            role.getPermissionCodes()
        );
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/dto/query/RoleDto.java
git commit -m "refactor: change RoleDto to use permission codes"
```

---

### Task 12: 更新所有 @RequirePermission 注解

**文件:**
- Modify: 所有 Controller 文件

- [ ] **Step 1: 更新 AdminMenuController**

```java
// 查询菜单详情
@RequirePermission(
    value = "admin:menu:read",
    name = "平台管理 / 菜单管理 / 查看",
    scope = "admin"
)

// 创建/更新/删除菜单
@RequirePermission(
    value = "admin:menu:write",
    name = "平台管理 / 菜单管理 / 编辑",
    scope = "admin"
)
```

- [ ] **Step 2: 更新 AdminRoleController**

```java
// 查询角色
@RequirePermission(
    value = "admin:role:read",
    name = "平台管理 / 角色管理 / 查看",
    scope = "admin"
)

// 创建/更新/删除/分配角色
@RequirePermission(
    value = "admin:role:write",
    name = "平台管理 / 角色管理 / 编辑",
    scope = "admin"
)
```

- [ ] **Step 3: 更新 AdminUserController**

```java
// 查询用户
@RequirePermission(
    value = "admin:user:read",
    name = "平台管理 / 用户管理 / 查看",
    scope = "admin"
)

// 创建/更新/删除用户
@RequirePermission(
    value = "admin:user:write",
    name = "平台管理 / 用户管理 / 编辑",
    scope = "admin"
)
```

- [ ] **Step 4: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 5: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/web/controller/
git commit -m "refactor: add name and scope to all @RequirePermission annotations"
```

---

### Task 13: 删除不再需要的文件

**文件:**
- Delete: `AdminPermissionRepository.java`
- Delete: `JpaAdminPermissionRepository.java` (如果存在)
- Delete: `AdminPermission.java` 实体
- Delete: `PermissionManagementAppService.java`
- Delete: `CreatePermissionCommand.java`

- [ ] **Step 1: 查找并删除权限相关文件**

```bash
# 查找需要删除的文件
find server/src/main/java -name "*Permission*.java" | grep -v "PermissionScanAppService"
find server/src/main/java -name "*Permission*.java" | grep -v "PermissionController"
find server/src/main/java -name "*Permission*.java" | grep -v "PermissionDto"
```

- [ ] **Step 2: 删除文件**

```bash
git rm server/src/main/java/com/aieducenter/admin/domain/repository/AdminPermissionRepository.java
git rm server/src/main/java/com/aieducenter/admin/domain/entity/AdminPermission.java
git rm server/src/main/java/com/aieducenter/admin/application/PermissionManagementAppService.java
git rm server/src/main/java/com/aieducenter/admin/application/dto/command/CreatePermissionCommand.java
# 删除其他相关文件...
```

- [ ] **Step 3: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 4: 提交**

```bash
git commit -m "refactor: remove unused permission-related files"
```

---

### Task 14: 更新 JpaAdminRoleRepository 中的查询逻辑

**文件:**
- Modify: `server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminRoleRepository.java`

- [ ] **Step 1: 更新查询方法以填充 permissionCodes**

修改查询方法，从 `admin_role_permissions` 表中加载 `permission_code` 而不是 `permission_id`。

- [ ] **Step 2: 编译验证**

Run: `cd server && ./gradlew compileJava`
Expected: SUCCESS

- [ ] **Step 3: 运行测试**

Run: `cd server && ./gradlew test`
Expected: Tests pass

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminRoleRepository.java
git commit -m "refactor: update role query to load permission codes"
```

---

## 验证步骤

完成所有任务后，执行以下验证：

1. **编译检查**
   ```bash
   cd server && ./gradlew compileJava
   ```

2. **运行测试**
   ```bash
   cd server && ./gradlew test
   ```

3. **启动应用**
   ```bash
   cd server && ./gradlew bootRun
   ```

4. **验证权限扫描接口**
   ```bash
   curl http://localhost:8080/api/v1/admin/permissions?scope=admin
   ```

5. **验证角色分配权限**
   ```bash
   curl -X PUT http://localhost:8080/api/v1/admin/roles/1/permissions \
     -H "Content-Type: application/json" \
     -d '{"permissionCodes": ["admin:menu:read", "admin:menu:write"]}'
   ```
