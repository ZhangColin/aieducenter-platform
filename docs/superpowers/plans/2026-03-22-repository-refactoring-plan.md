# Repository 重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 将手动实现的 Repository 改为 Spring Data JPA 自动生成，删除 @Transient 字段，使用 JPA 关联管理

**架构:** Repository 接口继承 `BaseRepository<T, ID>`，实体使用 `@OneToMany` + `cascade` + `orphanRemoval` 管理关联

**技术栈:** Java 21, Spring Boot 3.4, Spring Data JPA, cartisan-boot (cartisan-data-jpa)

---

## 文件结构概览

### 删除的文件
- `JpaAdminRoleRepository.java` - 手动实现类
- `JpaAdminUserRepository.java` - 手动实现类
- `JpaAdminMenuRepository.java` - 手动实现类

### 移动的文件
- `infrastructure.persistence.AdminRoleMenu` → `domain.entity.AdminRoleMenu`
- `infrastructure.persistence.AdminRolePermission` → `domain.entity.AdminRolePermission`

### 修改的文件
- `domain.repository.AdminRoleRepository` - 接口继承 BaseRepository
- `domain.repository.AdminUserRepository` - 接口继承 BaseRepository
- `domain.repository.AdminMenuRepository` - 接口继承 BaseRepository
- `domain.entity.AdminRole` - 添加 JPA 关联，删除 @Transient
- `domain.entity.AdminRoleMenu` - 移至 domain.entity
- `domain.entity.AdminRolePermission` - 移至 domain.entity
- `application.RoleManagementAppService` - assignMenus/assignPermissions 改造
- `application.AdminManagementAppService` - assignRoles 改造

---

## Task 1: 移动 AdminRoleMenu 到 domain.entity

**Files:**
- Create: `server/src/main/java/com/aieducenter/admin/domain/entity/AdminRoleMenu.java`
- Delete: `server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminRoleMenu.java`
- Test: `server/src/test/java/com/aieducenter/admin/domain/entity/AdminRoleMenuTest.java`

- [ ] **Step 1: 创建 domain.entity 包下的 AdminRoleMenu**

```java
package com.aieducenter.admin.domain.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * 角色-菜单关联实体。
 *
 * <p>属于 AdminRole 聚合，无回引用到 AdminRole。</p>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_role_menus")
public class AdminRoleMenu {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "menu_id")
    private Long menuId;

    /**
     * JPA 默认构造函数。
     */
    protected AdminRoleMenu() {
    }

    /**
     * 创建关联。
     */
    public AdminRoleMenu(Long roleId, Long menuId) {
        this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
        this.menuId = Objects.requireNonNull(menuId, "menuId must not be null");
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminRoleMenu that = (AdminRoleMenu) o;
        return Objects.equals(roleId, that.roleId) && Objects.equals(menuId, that.menuId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, menuId);
    }
}
```

- [ ] **Step 2: 编写测试**

Run: `cd server && ./gradlew test --tests "*AdminRoleMenuTest"`

Expected: PASS

```java
package com.aieducenter.admin.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * AdminRoleMenu 测试。
 */
class AdminRoleMenuTest {

    @Test
    void given_valid_input_when_create_then_success() {
        // When
        AdminRoleMenu roleMenu = new AdminRoleMenu(1L, 2L);

        // Then
        assertThat(roleMenu.getRoleId()).isEqualTo(1L);
        assertThat(roleMenu.getMenuId()).isEqualTo(2L);
    }

    @Test
    void given_null_roleId_when_create_then_throw_exception() {
        // When & Then
        assertThatThrownBy(() -> new AdminRoleMenu(null, 2L))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void given_null_menuId_when_create_then_throw_exception() {
        // When & Then
        assertThatThrownBy(() -> new AdminRoleMenu(1L, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void given_same_roleId_and_menuId_when_equals_then_true() {
        // Given
        AdminRoleMenu rm1 = new AdminRoleMenu(1L, 2L);
        AdminRoleMenu rm2 = new AdminRoleMenu(1L, 2L);

        // When & Then
        assertThat(rm1).isEqualTo(rm2);
        assertThat(rm1.hashCode()).isEqualTo(rm2.hashCode());
    }
}
```

- [ ] **Step 3: 删除 infrastructure.persistence 包下的旧文件**

Run: `rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminRoleMenu.java`

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/entity/AdminRoleMenu.java
git add server/src/test/java/com/aieducenter/admin/domain/entity/AdminRoleMenuTest.java
git rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminRoleMenu.java
git commit -m "refactor: move AdminRoleMenu to domain.entity package"
```

---

## Task 2: 移动 AdminRolePermission 到 domain.entity

**Files:**
- Create: `server/src/main/java/com/aieducenter/admin/domain/entity/AdminRolePermission.java`
- Delete: `server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminRolePermission.java`
- Test: `server/src/test/java/com/aieducenter/admin/domain/entity/AdminRolePermissionTest.java`

- [ ] **Step 1: 创建 domain.entity 包下的 AdminRolePermission**

```java
package com.aieducenter.admin.domain.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * 角色-权限关联实体。
 *
 * <p>属于 AdminRole 聚合，无回引用到 AdminRole。</p>
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

    /**
     * JPA 默认构造函数。
     */
    protected AdminRolePermission() {
    }

    /**
     * 创建关联。
     */
    public AdminRolePermission(Long roleId, String permissionCode, String permissionName) {
        this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
        this.permissionCode = Objects.requireNonNull(permissionCode, "permissionCode must not be null");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminRolePermission that = (AdminRolePermission) o;
        return Objects.equals(roleId, that.roleId) &&
               Objects.equals(permissionCode, that.permissionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionCode);
    }
}
```

- [ ] **Step 2: 编写测试**

Run: `cd server && ./gradlew test --tests "*AdminRolePermissionTest"`

Expected: PASS

```java
package com.aieducenter.admin.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * AdminRolePermission 测试。
 */
class AdminRolePermissionTest {

    @Test
    void given_valid_input_when_create_then_success() {
        // When
        AdminRolePermission rp = new AdminRolePermission(1L, "admin:user:read", "用户管理-查看");

        // Then
        assertThat(rp.getRoleId()).isEqualTo(1L);
        assertThat(rp.getPermissionCode()).isEqualTo("admin:user:read");
        assertThat(rp.getPermissionName()).isEqualTo("用户管理-查看");
    }

    @Test
    void given_null_roleId_when_create_then_throw_exception() {
        // When & Then
        assertThatThrownBy(() -> new AdminRolePermission(null, "admin:user:read", "用户管理-查看"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void given_null_permissionCode_when_create_then_throw_exception() {
        // When & Then
        assertThatThrownBy(() -> new AdminRolePermission(1L, null, "用户管理-查看"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void given_null_permissionName_when_create_then_success() {
        // When
        AdminRolePermission rp = new AdminRolePermission(1L, "admin:user:read", null);

        // Then
        assertThat(rp.getPermissionName()).isNull();
    }
}
```

- [ ] **Step 3: 删除 infrastructure.persistence 包下的旧文件**

Run: `rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminRolePermission.java`

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/entity/AdminRolePermission.java
git add server/src/test/java/com/aieducenter/admin/domain/entity/AdminRolePermissionTest.java
git rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminRolePermission.java
git commit -m "refactor: move AdminRolePermission to domain.entity package"
```

---

## Task 3: 改造 AdminRole 实体（添加 JPA 关联）

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/entity/AdminRole.java`
- Test: `server/src/test/java/com/aieducenter/admin/domain/entity/AdminRoleTest.java`

- [ ] **Step 1: 重写 AdminRole 实体**

完整替换为以下内容：

```java
package com.aieducenter.admin.domain.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    // 关联的菜单
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "role_id")
    private Set<AdminRoleMenu> roleMenus = new HashSet<>();

    // 关联的权限
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "role_id")
    private Set<AdminRolePermission> rolePermissions = new HashSet<>();

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
        return roleMenus.stream()
                .map(AdminRoleMenu::getMenuId)
                .collect(Collectors.toSet());
    }

    public Set<String> getPermissionCodes() {
        return rolePermissions.stream()
                .map(AdminRolePermission::getPermissionCode)
                .collect(Collectors.toSet());
    }

    public Set<AdminRoleMenu> getRoleMenus() {
        return roleMenus;
    }

    public Set<AdminRolePermission> getRolePermissions() {
        return rolePermissions;
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

    // ========== 业务行为 ==========

    /**
     * 是否为超级管理员角色。
     */
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(this.code);
    }

    /**
     * 添加菜单关联。
     */
    public void addMenu(Long menuId) {
        roleMenus.add(new AdminRoleMenu(this.id, menuId));
    }

    /**
     * 清除所有菜单关联。
     */
    public void clearMenus() {
        roleMenus.clear();
    }

    /**
     * 添加权限关联。
     */
    public void addPermission(String permissionCode, String permissionName) {
        rolePermissions.add(new AdminRolePermission(this.id, permissionCode, permissionName));
    }

    /**
     * 清除所有权限关联。
     */
    public void clearPermissions() {
        rolePermissions.clear();
    }
}
```

- [ ] **Step 2: 编写测试**

Run: `cd server && ./gradlew test --tests "*AdminRoleTest"`

Expected: PASS

```java
package com.aieducenter.admin.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * AdminRole 测试。
 */
class AdminRoleTest {

    @Test
    void given_valid_input_when_create_role_then_success() {
        // When
        AdminRole role = new AdminRole("管理员", "ADMIN", "系统管理员", 1);

        // Then
        assertThat(role.getName()).isEqualTo("管理员");
        assertThat(role.getCode()).isEqualTo("ADMIN");
        assertThat(role.getRoleMenus()).isEmpty();
        assertThat(role.getRolePermissions()).isEmpty();
    }

    @Test
    void given_role_when_add_menu_then_menuId_in_set() {
        // Given
        AdminRole role = new AdminRole("管理员", "ADMIN", "系统管理员", 1);
        role.addMenu(1L);
        role.addMenu(2L);

        // When & Then
        assertThat(role.getMenuIds()).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void given_role_with_menus_when_clear_menus_then_empty() {
        // Given
        AdminRole role = new AdminRole("管理员", "ADMIN", "系统管理员", 1);
        role.addMenu(1L);
        role.addMenu(2L);

        // When
        role.clearMenus();

        // Then
        assertThat(role.getMenuIds()).isEmpty();
    }

    @Test
    void given_role_when_add_permission_then_permissionCode_in_set() {
        // Given
        AdminRole role = new AdminRole("管理员", "ADMIN", "系统管理员", 1);
        role.addPermission("admin:user:read", "用户管理-查看");
        role.addPermission("admin:user:write", "用户管理-编辑");

        // When & Then
        assertThat(role.getPermissionCodes()).containsExactlyInAnyOrder("admin:user:read", "admin:user:write");
    }

    @Test
    void given_role_with_permissions_when_clear_permissions_then_empty() {
        // Given
        AdminRole role = new AdminRole("管理员", "ADMIN", "系统管理员", 1);
        role.addPermission("admin:user:read", "用户管理-查看");

        // When
        role.clearPermissions();

        // Then
        assertThat(role.getPermissionCodes()).isEmpty();
    }

    @Test
    void given_super_admin_code_when_isSuperAdmin_then_true() {
        // Given
        AdminRole role = new AdminRole("超级管理员", "SUPER_ADMIN", "超级管理员", 0);

        // When & Then
        assertThat(role.isSuperAdmin()).isTrue();
    }

    @Test
    void given_normal_role_code_when_isSuperAdmin_then_false() {
        // Given
        AdminRole role = new AdminRole("管理员", "ADMIN", "系统管理员", 1);

        // When & Then
        assertThat(role.isSuperAdmin()).isFalse();
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/entity/AdminRole.java
git add server/src/test/java/com/aieducenter/admin/domain/entity/AdminRoleTest.java
git commit -m "refactor: add JPA associations to AdminRole entity"
```

---

## Task 4: 简化 AdminRoleRepository 接口

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/repository/AdminRoleRepository.java`
- Delete: `server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminRoleRepository.java`

- [ ] **Step 1: 重写 AdminRoleRepository 接口**

```java
package com.aieducenter.admin.domain.repository;

import java.util.List;
import java.util.Optional;

import com.aieducenter.admin.domain.entity.AdminRole;
import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * 角色仓储接口。
 *
 * @since 0.1.0
 */
@Port(PortType.REPOSITORY)
public interface AdminRoleRepository extends BaseRepository<AdminRole, Long> {

    /**
     * 根据编码查询角色。
     */
    Optional<AdminRole> findByCode(String code);

    /**
     * 查询所有角色（按排序字段排序）。
     */
    List<AdminRole> findAllByOrderBySortOrderAsc();

    /**
     * 检查角色是否被管理员使用。
     *
     * <p>注意：此方法跨表查询，保留在 Repository 层是因为是领域概念。
     * 实现时使用 @Query 注解。</p>
     */
    boolean isUsedByAnyAdmin(Long roleId);
}
```

- [ ] **Step 2: 删除 JpaAdminRoleRepository 实现类**

Run: `rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminRoleRepository.java`

- [ ] **Step 3: 为 isUsedByAnyAdmin 添加实现**

在接口中添加 @Query 注解：

```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 在 AdminRoleRepository 接口中添加：

@Query("SELECT CASE WHEN COUNT(aur) > 0 THEN true ELSE false END " +
       "FROM AdminUserRole aur WHERE aur.roleId = :roleId")
@Override
boolean isUsedByAnyAdmin(@Param("roleId") Long roleId);
```

- [ ] **Step 4: 更新 AdminUserRole 实体的包名**

由于 `AdminUserRole` 在 `@Query` 中被引用，需要确保它在正确的包下。移动到 domain.entity：

```bash
mv server/src/main/java/com/aieducenter/admin/infrastructure/persistence/AdminUserRole.java \
   server/src/main/java/com/aieducenter/admin/domain/entity/AdminUserRole.java
```

然后修改包名：
```java
package com.aieducenter.admin.domain.entity;
```

- [ ] **Step 5: 运行测试验证**

Run: `cd server && ./gradlew test --tests "*AdminRole*Test"`

Expected: PASS

- [ ] **Step 6: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/repository/AdminRoleRepository.java
git add server/src/main/java/com/aieducenter/admin/domain/entity/AdminUserRole.java
git rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminRoleRepository.java
git commit -m "refactor: simplify AdminRoleRepository to extend BaseRepository"
```

---

## Task 5: 简化 AdminMenuRepository 接口

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/repository/AdminMenuRepository.java`
- Delete: `server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminMenuRepository.java`

- [ ] **Step 1: 重写 AdminMenuRepository 接口**

```java
package com.aieducenter.admin.domain.repository;

import java.util.List;
import java.util.Optional;

import com.aieducenter.admin.domain.entity.AdminMenu;
import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * 菜单仓储接口。
 *
 * @since 0.1.0
 */
@Port(PortType.REPOSITORY)
public interface AdminMenuRepository extends BaseRepository<AdminMenu, Long> {

    /**
     * 根据父 ID 查询菜单。
     */
    List<AdminMenu> findByParentIdOrderBySortOrderAsc(Long parentId);

    /**
     * 查询所有菜单（按排序字段排序）。
     */
    List<AdminMenu> findAllByOrderBySortOrderAsc();

    /**
     * 检查菜单是否有子菜单。
     */
    boolean existsByParentId(Long parentId);
}
```

- [ ] **Step 2: 删除 JpaAdminMenuRepository 实现类**

Run: `rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminMenuRepository.java`

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/repository/AdminMenuRepository.java
git rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminMenuRepository.java
git commit -m "refactor: simplify AdminMenuRepository to extend BaseRepository"
```

---

## Task 6: 简化 AdminUserRepository 接口

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/repository/AdminUserRepository.java`
- Delete: `server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminUserRepository.java`

- [ ] **Step 1: 重写 AdminUserRepository 接口**

```java
package com.aieducenter.admin.domain.repository;

import java.util.List;
import java.util.Optional;

import com.aieducenter.admin.domain.aggregate.Admin;
import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * 管理员仓储接口。
 *
 * @since 0.1.0
 */
@Port(PortType.REPOSITORY)
public interface AdminUserRepository extends BaseRepository<Admin, Long> {

    /**
     * 根据用户名查询管理员。
     */
    Optional<Admin> findByUsername(String username);

    /**
     * 检查用户名是否存在。
     */
    boolean existsByUsername(String username);

    /**
     * 查询所有管理员（按创建时间倒序）。
     */
    List<Admin> findAllByOrderByCreatedAtDesc();

    /**
     * 查询管理员总数。
     */
    long countBy();
}
```

- [ ] **Step 2: 删除 JpaAdminUserRepository 实现类**

Run: `rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminUserRepository.java`

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/repository/AdminUserRepository.java
git rm server/src/main/java/com/aieducenter/admin/infrastructure/persistence/JpaAdminUserRepository.java
git commit -m "refactor: simplify AdminUserRepository to extend BaseRepository"
```

---

## Task 7: 改造 RoleManagementAppService（assignMenus）

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/RoleManagementAppService.java`

- [ ] **Step 1: 修改 assignMenus 方法**

找到 `assignMenus` 方法，替换为：

```java
/**
 * 为角色分配菜单。
 */
@Transactional
public void assignMenus(Long roleId, AssignMenusCommand command) {
    AdminRole role = findById(roleId);

    // 验证所有菜单 ID 存在
    for (Long menuId : command.menuIds()) {
        menuRepository.findById(menuId)
                .orElseThrow(() -> new DomainException(AdminError.MENU_NOT_FOUND));
    }

    // 清除现有菜单关联
    role.clearMenus();

    // 添加新菜单关联
    for (Long menuId : command.menuIds()) {
        role.addMenu(menuId);
    }

    roleRepository.save(role);
}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/RoleManagementAppService.java
git commit -m "refactor: move assignMenus logic to Service layer"
```

---

## Task 8: 改造 RoleManagementAppService（assignPermissions）

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/RoleManagementAppService.java`

- [ ] **Step 1: 修改 assignPermissions 方法**

找到 `assignPermissions` 方法，替换为：

```java
/**
 * 为角色分配权限。
 */
@Transactional
public void assignPermissions(Long roleId, AssignPermissionsCommand command) {
    AdminRole role = findById(roleId);

    // 清除现有权限关联
    role.clearPermissions();

    // 添加新权限关联
    for (String permissionCode : command.permissionCodes()) {
        role.addPermission(permissionCode, null);  // name 可以后续通过 PermissionScanner 获取
    }

    roleRepository.save(role);
}
```

- [ ] **Step 2: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/RoleManagementAppService.java
git commit -m "refactor: move assignPermissions logic to Service layer"
```

---

## Task 9: 改造 AdminManagementAppService（assignRoles）

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminManagementAppService.java`

- [ ] **Step 1: 首先读取 Admin 聚合，确认是否需要添加角色关联**

Run: `cat server/src/main/java/com/aieducenter/admin/domain/aggregate/Admin.java`

查看 Admin 是否有类似 AdminRole 的 @Transient 字段需要改造。

如果 Admin 有 `@Transient private Set<Long> roleIds`，需要添加 JPA 关联：

```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "admin_id")
private Set<AdminUserRole> userRoles = new HashSet<>();

public void addRole(Long roleId) {
    userRoles.add(new AdminUserRole(this.id, roleId));
}

public void clearRoles() {
    userRoles.clear();
}

public Set<Long> getRoleIds() {
    return userRoles.stream()
            .map(AdminUserRole::getRoleId)
            .collect(Collectors.toSet());
}
```

- [ ] **Step 2: 修改 assignRoles 方法**

```java
/**
 * 为管理员分配角色。
 */
@Transactional
public void assignRoles(Long adminId, AssignRolesCommand command) {
    Admin admin = findById(adminId);

    // 验证所有角色 ID 存在
    for (Long roleId : command.roleIds()) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> new DomainException(AdminError.ROLE_NOT_FOUND));
    }

    // 清除现有角色关联
    admin.clearRoles();

    // 添加新角色关联
    for (Long roleId : command.roleIds()) {
        admin.addRole(roleId);
    }

    adminUserRepository.save(admin);
}
```

- [ ] **Step 3: 编写集成测试**

Run: `cd server && ./gradlew test --tests "*AdminManagementAppServiceTest"`

Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/aggregate/Admin.java
git add server/src/main/java/com/aieducenter/admin/application/AdminManagementAppService.java
git commit -m "refactor: move assignRoles logic to Service layer"
```

---

## Task 10: 更新 Service 层使用新的 Repository 方法

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/application/RoleManagementAppService.java`
- Modify: `server/src/main/java/com/aieducenter/admin/application/AdminManagementAppService.java`
- Modify: `server/src/main/java/com/aieducenter/admin/application/MenuManagementAppService.java`

- [ ] **Step 1: 更新 RoleManagementAppService 中的 findAll() 调用**

如果之前调用 `findAll()` 并期望排序，现在调用 `findAllByOrderBySortOrderAsc()`：

```java
// Before
public List<AdminRole> findAll() {
    return roleRepository.findAll();
}

// After
public List<AdminRole> findAll() {
    return roleRepository.findAllByOrderBySortOrderAsc();
}
```

- [ ] **Step 2: 更新 MenuManagementAppService 中的 findAll() 调用**

类似地，使用 `findAllByOrderBySortOrderAsc()`。

- [ ] **Step 3: 更新 hasChildren() 方法调用**

```java
// Before
boolean hasChildren = menuRepository.hasChildren(menuId);

// After
boolean hasChildren = menuRepository.existsByParentId(menuId);
```

- [ ] **Step 4: 运行全量测试**

Run: `cd server && ./gradlew test`

Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/application/*.java
git commit -m "refactor: update Service layer to use new Repository methods"
```

---

## Task 11: 移除不再需要的 Repository 方法

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/repository/AdminRoleRepository.java`
- Modify: `server/src/main/java/com/aieducenter/admin/domain/repository/AdminUserRepository.java`

- [ ] **Step 1: 从 AdminRoleRepository 中删除跨表查询方法**

由于 `findByAdminId()` 是跨表查询，应该从 Repository 中删除，改在 Service 层组合实现：

```java
// 从 AdminRoleRepository 接口中删除：
// List<AdminRole> findByAdminId(Long adminId);
```

在 Service 层添加：

```java
public List<AdminRole> findByAdminId(Long adminId) {
    // 通过 AdminUserRole 查询 roleId，再查询角色
    List<Long> roleIds = em.createQuery(
            "SELECT aur.roleId FROM AdminUserRole aur WHERE aur.adminId = :adminId", Long.class)
            .setParameter("adminId", adminId)
            .getResultList();

    return roleIds.stream()
            .map(roleId -> roleRepository.findById(roleId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
}
```

或者使用 Q 端 jOOQ 直接返回 DTO。

- [ ] **Step 2: 从 AdminUserRepository 中删除跨表查询方法**

类似地，`findRoleCodesByAdminId()` 和 `findPermissionCodesByAdminId()` 应该移到 Service 层或 Q 端。

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/repository/*.java
git commit -m "refactor: remove cross-table query methods from Repository"
```

---

## Task 12: 清理 infrastructure.persistence 包

**Files:**
- Delete: `server/src/main/java/com/aieducenter/admin/infrastructure/persistence/` (如果为空)

- [ ] **Step 1: 检查 infrastructure.persistence 包是否还有文件**

Run: `ls server/src/main/java/com/aieducenter/admin/infrastructure/persistence/`

- [ ] **Step 2: 如果为空，删除包目录**

Run: `rm -rf server/src/main/java/com/aieducenter/admin/infrastructure/persistence/`

- [ ] **Step 3: 运行全量测试**

Run: `cd server && ./gradlew check`

Expected: PASS（包括 ArchUnit 检查）

- [ ] **Step 4: 提交**

```bash
git add -A
git commit -m "refactor: clean up empty infrastructure.persistence package"
```

---

## 验证步骤

完成所有任务后：

1. **编译检查**
   ```bash
   cd server && ./gradlew compileJava
   ```

2. **全量测试**
   ```bash
   cd server && ./gradlew test
   ```

3. **ArchUnit 检查**
   ```bash
   cd server && ./gradlew check
   ```

4. **手动测试**
   - 启动应用
   - 测试角色 CRUD
   - 测试菜单分配
   - 测试权限分配
