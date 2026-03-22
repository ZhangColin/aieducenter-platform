# Repository 重构设计文档

> **目标**: 将手动实现的 Repository 改为 Spring Data JPA 自动生成，简化代码并符合 DDD 最佳实践

**日期**: 2026-03-22

## 背景

当前平台代码使用手动实现的 Repository（`JpaAdminRoleRepository`、`JpaAdminUserRepository`、`JpaAdminMenuRepository`），通过 `EntityManager` 执行 JPQL 查询。cartisan-boot 框架现已提供 `BaseRepository<T, ID>` 接口，扩展了 `JpaRepository` 和 `JpaSpecificationExecutor`，可自动生成实现。

## 现状问题

1. **手动实现冗余** - 基本 CRUD 方法每个 Repository 都要写一遍
2. **@Transient 字段** - `AdminRole` 使用 `@Transient` 存储 `menuIds`/`permissionCodes`，需要手动 `populateTransientFields()` 填充
3. **职责混乱** - `assignMenus()`/`assignPermissions()` 在 Repository 层，违反 C 端职责边界
4. **关联实体位置错误** - `AdminRoleMenu`/`AdminRolePermission` 在 infrastructure 包，属于领域模型

## 设计

### 1. Repository 接口简化

**删除**所有 `JpaXxxRepository` 实现类。

Repository 接口继承 `BaseRepository`：

```java
// Before
@Port(PortType.REPOSITORY)
public interface AdminRoleRepository {
    Optional<AdminRole> findById(Long id);
    List<AdminRole> findAll();
    AdminRole save(AdminRole role);
    void delete(AdminRole role);
    // ...
}

// After
@Port(PortType.REPOSITORY)
public interface AdminRoleRepository extends BaseRepository<AdminRole, Long> {
    // Spring Data JPA 自动提供：findById(), findAll(), save(), delete(), etc.

    // 自定义查询方法（Spring Data JPA 命名约定自动生成）
    Optional<AdminRole> findByCode(String code);

    // 跨表查询 - 移到 Service 或 Q 端
    // List<AdminRole> findByAdminId(Long adminId);  // 删除
}
```

**自定义查询方法规则：**
- 单表查询：使用 Spring Data JPA 方法命名（如 `findByCode`）
- 跨表查询：移到 Service 层组合调用，或使用 jOOQ（Q 端）

### 2. AdminRole 实体改造

**删除 @Transient 字段，添加 JPA 关联：**

```java
@Entity
@Table(name = "admin_roles")
public class AdminRole extends SoftDeletable {

    // ... 基本字段 ...

    // Before:
    // @Transient
    // private Set<Long> menuIds = new HashSet<>();
    // @Transient
    // private Set<String> permissionCodes = new HashSet<>();

    // After:
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "role_id")
    private Set<AdminRoleMenu> roleMenus = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "role_id")
    private Set<AdminRolePermission> rolePermissions = new HashSet<>();

    // 便利方法
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

    public void addMenu(Long menuId) {
        roleMenus.add(new AdminRoleMenu(getId(), menuId));
    }

    public void clearMenus() {
        roleMenus.clear();
    }

    public void addPermission(String code, String name) {
        rolePermissions.add(new AdminRolePermission(getId(), code, name));
    }

    public void clearPermissions() {
        rolePermissions.clear();
    }
}
```

### 3. 关联实体移至 domain.entity

**移动文件：**
- `infrastructure.persistence.AdminRoleMenu` → `domain.entity.AdminRoleMenu`
- `infrastructure.persistence.AdminRolePermission` → `domain.entity.AdminRolePermission`

**关联实体不持有回引用：**

```java
@Entity
@Table(name = "admin_role_menus")
public class AdminRoleMenu {
    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "menu_id")
    private Long menuId;

    // 不添加：private AdminRole role;  // 无回引用
}
```

### 4. Service 层改造

**assignMenus/assignPermissions 从 Repository 移至 Service：**

```java
@Service
public class RoleManagementAppService {

    // Before:
    @Transactional
    public void assignMenus(Long roleId, AssignMenusCommand command) {
        findById(roleId);
        roleRepository.assignMenus(roleId, command.menuIds());  // Repository 层
    }

    // After:
    @Transactional
    public void assignMenus(Long roleId, AssignMenusCommand command) {
        AdminRole role = findById(roleId);

        // 验证菜单存在
        for (Long menuId : command.menuIds()) {
            menuRepository.findById(menuId)
                .orElseThrow(() -> new DomainException(AdminError.MENU_NOT_FOUND));
        }

        // 通过 JPA 关联管理
        role.clearMenus();
        for (Long menuId : command.menuIds()) {
            role.addMenu(menuId);
        }
        roleRepository.save(role);  // JPA cascade 自动处理
    }
}
```

**Repository 接口删除的方法：**
- `void assignMenus(Long roleId, List<Long> menuIds);`
- `void assignPermissions(Long roleId, List<String> permissionCodes);`

### 5. DTO 转换

DTO 转换保持不变，通过实体的 `getMenuIds()`/`getPermissionCodes()` 方法：

```java
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
            role.getMenuIds(),        // 调用实体方法
            role.getPermissionCodes() // 调用实体方法
        );
    }
}
```

## 影响范围

### 删除的文件
- `JpaAdminRoleRepository.java`
- `JpaAdminUserRepository.java`
- `JpaAdminMenuRepository.java`

### 修改的文件
- `AdminRoleRepository.java` - 接口继承 `BaseRepository`
- `AdminUserRepository.java` - 接口继承 `BaseRepository`
- `AdminMenuRepository.java` - 接口继承 `BaseRepository`
- `AdminRole.java` - 添加 JPA 关联，删除 @Transient
- `AdminRoleMenu.java` - 移至 domain.entity，确保无回引用
- `AdminRolePermission.java` - 移至 domain.entity
- `RoleManagementAppService.java` - assignMenus/assignPermissions 改造
- `AdminManagementAppService.java` - assignRoles 改造（类似）

### 可选：Q 端 jOOQ 查询
对于列表查询等复杂场景，可考虑使用 jOOQ（Q 端），直接返回 DTO。

## 测试策略

1. **单元测试** - 验证实体关联行为（addMenu, clearMenus 等）
2. **集成测试** - 验证 JPA cascade 和 orphanRemoval
3. **Service 测试** - 验证 assignMenus/assignPermissions 完整流程
