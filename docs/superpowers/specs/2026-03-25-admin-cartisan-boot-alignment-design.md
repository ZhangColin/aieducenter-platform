# Admin 上下文 cartisan-boot 规范对齐设计

> **日期**: 2026-03-25
> **类型**: 代码规范对齐
> **范围**: admin 限界上下文

## 一、背景

cartisan-boot 使用手册已更新到 v0.9 版本，检查发现 admin 上下文存在以下不符合手册规范的问题：

1. 枚举未实现 `BaseEnum<T>` 接口（ENUM-001）
2. 实体枚举字段未使用 `@EnumConvert` 注解（ENUM-002）
3. Optional 处理未使用 `Assertions.requirePresent()`
4. Response DTO 枚举序列化为字符串
5. 缺少 `@BoundedContext` 注解

## 二、目标

使 admin 上下文的实现完全符合 cartisan-boot 使用手册规范，提高代码一致性和可维护性。

## 三、设计

### 3.1 枚举改造（ENUM-001, ENUM-002）

#### AdminStatus 枚举

**文件**: `AdminUser.java`

```java
/**
 * 管理员状态枚举。
 */
@Getter
@AllArgsConstructor
public enum AdminStatus implements BaseEnum<AdminStatus> {
    ACTIVE(1, "激活"),
    DISABLED(0, "禁用");

    private final Integer code;
    private final String name;
}
```

#### MenuType 枚举

**文件**: `MenuType.java`

```java
/**
 * 菜单类型枚举。
 */
@Getter
@AllArgsConstructor
public enum MenuType implements BaseEnum<MenuType> {
    MENU(1, "菜单"),
    GROUP(2, "分组"),
    DIVIDER(3, "分隔线");

    private final Integer code;
    private final String name;
}
```

#### AdminUser 实体字段修改

**文件**: `AdminUser.java`

```java
// 修改前
@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
private AdminStatus status;

// 修改后（需导入 com.cartisan.data.jpa.annotation.EnumConvert）
@EnumConvert(AdminStatus.class)
@Column(name = "status", nullable = false)
private AdminStatus status;
```

#### AdminMenu 实体字段修改

**文件**: `AdminMenu.java`

```java
// 修改前
@Enumerated(EnumType.STRING)
@Column(name = "type", nullable = false, length = 20)
private MenuType type = MenuType.MENU;

// 修改后（需导入 com.cartisan.data.jpa.annotation.EnumConvert）
@EnumConvert(MenuType.class)
@Column(name = "type", nullable = false)
private MenuType type = MenuType.MENU;
```

### 3.2 数据库迁移

**Flyway 迁移脚本**: `V1__admin_enum_alignment.sql`

```sql
-- 修改 sys_admin_users.status 列
ALTER TABLE sys_admin_users
  ALTER COLUMN status TYPE INTEGER USING (
    CASE status
      WHEN 'ACTIVE' THEN 1
      WHEN 'DISABLED' THEN 0
      ELSE 1
    END
  );

-- 修改 sys_admin_menus.type 列
ALTER TABLE sys_admin_menus
  ALTER COLUMN type TYPE INTEGER USING (
    CASE type
      WHEN 'MENU' THEN 1
      WHEN 'GROUP' THEN 2
      WHEN 'DIVIDER' THEN 3
      ELSE 1
    END
  );
```

### 3.3 Optional 处理统一

**文件**: 各 AppService

```java
// 无参数版本 → 快捷版 requirePresent
// 修改前
AdminUser adminUser = adminUserRepository.findById(adminId).orElseThrow();

// 修改后
AdminUser adminUser = Assertions.requirePresent(
    adminUserRepository.findById(adminId)
);

// 有 Lambda 版本 → 完整版 requirePresent
// 修改前
AdminUser adminUser = adminUserRepository.findById(id)
    .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

// 修改后
AdminUser adminUser = Assertions.requirePresent(
    adminUserRepository.findById(id),
    AdminMessage.ADMIN_NOT_FOUND
);
```

**影响文件**:
- `AdminUserManagementAppService.java` (4处)
- `AdminUserPermissionAppService.java` (4处)
- `RoleManagementAppService.java` (5处)
- `MenuManagementAppService.java` (4处)
- `AdminUserAuthAppService.java` (4处)

### 3.4 Response DTO 枚举序列化

**文件**: `AdminUserResponse.java`

```java
// 修改前
public record AdminUserResponse(
    Long id,
    String username,
    String nickname,
    String email,
    String phone,
    String avatar,
    String status,              // 字符串
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

// 修改后：保持枚举类型，Jackson 自动序列化为 code
public record AdminUserResponse(
    Long id,
    String username,
    String nickname,
    String email,
    String phone,
    String avatar,
    AdminStatus status,         // 自动序列化为 code (Integer)
    String statusName,          // MapStruct 自动调用 getName()
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

**文件**: `AdminUserMapper.java`

```java
// MapStruct 会自动处理 BaseEnum 的 getCode()/getName() 映射
@Mapper(componentModel = "spring")
public interface AdminUserMapper extends DomainMapper<AdminUser, AdminUserResponse> {
    // 无需手动配置，自动映射：
    // AdminStatus status -> Integer code (Jackson 序列化)
    // String statusName -> getName() (MapStruct)
}
```

**文件**: `AdminUserQuery.java`

```java
// 修改前
public record AdminUserQuery(
    @Condition(type = ConditionType.INNER_LIKE) String username,
    @Condition(type = ConditionType.EQUAL) AdminUser.AdminStatus status,
    @Condition(blurry = "username,nickname,email") String keyword
) {}

// 修改后：保持枚举类型，维持类型安全
public record AdminUserQuery(
    @Condition(type = ConditionType.INNER_LIKE) String username,
    @Condition(type = ConditionType.EQUAL) AdminStatus status,
    @Condition(blurry = "username,nickname,email") String keyword
) {}
```

**说明**：前端传参时传整型 code（如 `1`），Jackson 自动反序列化为 `AdminStatus.ACTIVE`。

### 3.5 @BoundedContext 注解

**文件**: `package-info.java`

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
 * @since 0.1.0
 */
@BoundedContext(name = "PlatformAdmin", subDomain = SubDomain.SUPPORT)
package com.aieducenter.admin;

import com.cartisan.core.stereotype.BoundedContext;
import com.cartisan.core.domain.SubDomain;
```

## 四、影响范围

| 文件 | 修改类型 | 说明 |
|------|----------|------|
| `AdminUser.java` | 枚举 + 注解 | AdminStatus 实现 BaseEnum，字段改用 @EnumConvert |
| `AdminMenu.java` | 注解 | type 字段改用 @EnumConvert |
| `MenuType.java` | 枚举 | 实现 BaseEnum，添加 @Getter/@AllArgsConstructor |
| `AdminUserResponse.java` | 字段类型 | status: String → AdminStatus，新增 statusName |
| `AdminUserQuery.java` | 字段类型 | status: AdminUser.AdminStatus → AdminStatus |
| `AdminUserMapper.java` | 删除方法 | 移除手动 mapStatus（MapStruct 自动处理） |
| `AdminUserManagementAppService.java` | 工具方法 | orElseThrow → requirePresent (4处) |
| `AdminUserPermissionAppService.java` | 工具方法 | orElseThrow → requirePresent (4处) |
| `RoleManagementAppService.java` | 工具方法 | orElseThrow → requirePresent (5处) |
| `MenuManagementAppService.java` | 工具方法 | orElseThrow → requirePresent (4处) |
| `AdminUserAuthAppService.java` | 工具方法 | orElseThrow → requirePresent (4处) |
| `package-info.java` | 新增注解 | 添加 @BoundedContext |
| Flyway 迁移脚本 | 新增 | V1__admin_enum_alignment.sql |

## 五、注意事项

1. **数据库可清空重建**，迁移脚本仅作为参考
2. **前端接口变化**：
   - 响应中 `status` 从字符串（`"ACTIVE"`）变为整型（`1`）
   - 响应中新增 `statusName` 字段（如 `"激活"`）
   - 请求中传整型 code（如 `status=1`），Jackson 自动反序列化为枚举
3. **Jackson 序列化**：BaseEnum 自动序列化为 code（Integer），无需额外配置
4. **MapStruct 自动处理**：BaseEnum 的 getCode()/getName() 自动映射
5. **导入路径**：
   - `@EnumConvert`: `com.cartisan.data.jpa.annotation.EnumConvert`
   - `BaseEnum`: `com.cartisan.core.domain.BaseEnum`

## 六、验证

1. ArchUnit 规则检查通过
2. 单元测试通过
3. API 接口测试验证 status 序列化格式
