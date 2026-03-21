# Platform Admin Context 设计文档

> **版本**：v1.0
> **日期**：2026-03-21
> **状态**：待评审

---

## 一、概述

### 1.1 目标

实现平台运营后台的基础管理功能，包括管理员管理、角色管理、权限管理、菜单管理，以及完整的 RBAC 权限体系。

### 1.2 范围

- **本次实现**：Platform Admin Context 基础设施（管理员、角色、权限、菜单、登录）
- **不包含**：具体业务模块（租户管理、模型管理等）后续接入

### 1.3 约束

- 遵循 cartisan-boot 框架的 DDD 架构规范
- 使用 Sa-Token 多账号体系隔离管理员和普通用户会话
- 管理员使用独立的 `admin_users` 表，与 `users` 表完全分离

---

## 二、领域模型

### 2.1 聚合根与实体

```
Platform Admin Context
  ├── Admin（聚合根）
  ├── AdminRole（实体）
  ├── AdminPermission（实体）
  └── AdminMenu（实体）
```

### 2.2 Admin 聚合根

```java
@Entity
@Table(name = "admin_users")
@Aggregate
public class Admin extends SoftDeletable implements AggregateRoot<Admin> {

    // 主键
    private Long id;

    // 登录凭证
    private String username;      // 登录名，唯一
    private String password;      // BCrypt 加密

    // 基本信息
    private String nickname;      // 显示名称
    private String email;         // 邮箱（可选）
    private String phone;         // 手机号（可选）
    private String avatar;        // 头像 URL

    // 状态
    private AdminStatus status;   // ACTIVE / DISABLED
    private boolean system;       // 系统内置用户，不可删除

    // 审计字段（继承自 Auditable）
    // created_at, updated_at, created_by, updated_by
}
```

### 2.3 状态枚举

```java
public enum AdminStatus {
    ACTIVE,      // 启用
    DISABLED     // 禁用
}
```

### 2.4 关系设计

```
Admin ──┬── AdminRole (多对多，通过 admin_user_roles)
        │
        └── 通过角色获取权限和菜单

AdminRole ──┬── AdminMenu (多对多，通过 admin_role_menus)
            │
            └── AdminPermission (多对多，通过 admin_role_permissions)

AdminMenu (自关联树结构，parent_id，最多3级)
AdminPermission (归属于 menu_id)
```

---

## 三、数据库设计

### 3.1 表清单

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `admin_users` | 管理员 | id, username, password, nickname, email, phone, avatar, status, system |
| `admin_roles` | 角色 | id, name, code, description, sort_order |
| `admin_permissions` | 权限 | id, name, code, menu_id, sort_order |
| `admin_menus` | 菜单 | id, name, path, icon, parent_id, sort_order |
| `admin_user_roles` | 管理员-角色 | admin_id, role_id |
| `admin_role_menus` | 角色-菜单 | role_id, menu_id |
| `admin_role_permissions` | 角色-权限 | role_id, permission_id |

### 3.2 admin_users 表

```sql
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
```

### 3.3 admin_menus 表

```sql
CREATE TABLE admin_menus (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    path            VARCHAR(255),
    icon            VARCHAR(50),
    parent_id       BIGINT,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_admin_menus_parent FOREIGN KEY (parent_id) REFERENCES admin_menus(id)
);
```

### 3.4 超级管理员逻辑

- 超管通过 `role.code = 'SUPER_ADMIN'` 判断
- 超管天然拥有所有权限，无需配置
- 代码中优先判断超管角色，直接放行

---

## 四、API 接口设计

### 4.1 接口路径规范

```
/api/v1/admin/
├── auth/
│   ├── POST   /login              # 登录
│   ├── POST   /logout             # 登出
│   ├── GET    /current            # 获取当前管理员信息
│   └── PUT    /current/password   # 修改当前管理员密码
│
├── users/
│   ├── GET    /                   # 管理员列表（分页）
│   ├── GET    /{id}               # 获取管理员详情
│   ├── POST   /                   # 创建管理员
│   ├── PUT    /{id}               # 修改管理员
│   ├── DELETE /{id}               # 删除管理员
│   ├── PUT    /{id}/status        # 修改管理员状态
│   └── PUT    /{id}/roles         # 分配角色
│
├── roles/
│   ├── GET    /                   # 角色列表
│   ├── GET    /{id}               # 获取角色详情
│   ├── POST   /                   # 创建角色
│   ├── PUT    /{id}               # 修改角色
│   ├── DELETE /{id}               # 删除角色
│   ├── PUT    /{id}/menus         # 分配菜单
│   └── PUT    /{id}/permissions   # 分配权限
│
├── menus/
│   ├── GET    /                   # 菜单列表（树形）
│   ├── GET    /{id}               # 获取菜单详情
│   ├── POST   /                   # 创建菜单
│   ├── PUT    /{id}               # 修改菜单
│   └── DELETE /{id}               # 删除菜单
│
└── permissions/
    ├── GET    /                   # 权限列表
    ├── GET    /{id}               # 获取权限详情
    ├── POST   /                   # 创建权限
    ├── PUT    /{id}               # 修改权限
    └── DELETE /{id}               # 删除权限
```

### 4.2 登录接口

```java
POST /api/v1/admin/auth/login

Request:
{
    "username": "admin",
    "password": "C@rt1s@n",
    "rememberMe": true
}

Response:
{
    "code": 200,
    "message": "Success",
    "data": {
        "token": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
        "expireTime": "2026-03-28T12:00:00Z",
        "admin": {
            "id": 1,
            "username": "admin",
            "nickname": "超级管理员",
            "avatar": null
        },
        "roles": [
            {"code": "SUPER_ADMIN", "name": "超级管理员"}
        ],
        "menus": [/* 树形菜单 */],
        "permissions": ["admin:user:read", "admin:user:write", ...]
    }
}
```

### 4.3 权限注解使用

```java
@RestController
@RequestMapping("/api/v1/admin/users")
@RequireAuth
public class AdminUserController {

    @GetMapping
    @RequirePermission("admin:user:read")
    public ApiResponse<PageResponse<AdminDto>> listUsers() { ... }

    @PostMapping
    @RequirePermission("admin:user:write")
    public ApiResponse<AdminDto> createUser() { ... }

    @DeleteMapping("/{id}")
    @RequirePermission("admin:user:delete")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) { ... }
}
```

---

## 五、权限校验实现

### 5.1 Sa-Token 多账号体系

管理员使用独立的 Sa-Token 账号体系：

```java
// 管理员登录
StpUtil.ADMIN.login(adminId);

// 普通用户登录
StpUtil.login(userId);
```

### 5.2 StpInterface 实现

在 `config` 层实现委托式 StpInterface：

```java
@Configuration
public class SaTokenConfig {

    @Bean
    public StpInterface cartisanStpInterface(
            AdminPermissionService adminPermissionService,
            TenantPermissionService tenantPermissionService) {

        return new StpInterface() {
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                return switch (loginType) {
                    case "admin" -> adminPermissionService.getPermissions((Long) loginId);
                    case "user"  -> tenantPermissionService.getPermissions((Long) loginId);
                    default     -> List.of();
                };
            }

            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                return switch (loginType) {
                    case "admin" -> adminPermissionService.getRoles((Long) loginId);
                    case "user"  -> tenantPermissionService.getRoles((Long) loginId);
                    default     -> List.of();
                };
            }
        };
    }
}
```

### 5.3 超管权限判断

```java
// AdminPermissionService
public List<String> getPermissions(Long adminId) {
    if (hasSuperAdminRole(adminId)) {
        return List.of();  // 超管返回空，拦截器中直接放行
    }
    return adminUserRepository.findPermissionCodesByAdminId(adminId);
}

// 拦截器增强
public boolean hasPermission(String permissionCode) {
    if (SecurityContext.hasRole("SUPER_ADMIN")) {
        return true;  // 超管直接通过
    }
    return StpUtil.hasPermission(permissionCode);
}
```

---

## 六、包结构

```
com.aieducenter.admin/
├── domain/
│   ├── aggregate/
│   │   └── Admin.java
│   ├── entity/
│   │   ├── AdminRole.java
│   │   ├── AdminPermission.java
│   │   └── AdminMenu.java
│   ├── repository/
│   │   ├── AdminUserRepository.java
│   │   ├── AdminRoleRepository.java
│   │   ├── AdminPermissionRepository.java
│   │   └── AdminMenuRepository.java
│   ├── error/
│   │   └── AdminError.java
│   └── service/
│       └── AdminPermissionService.java
│
├── application/
│   ├── AdminAuthAppService.java
│   ├── AdminManagementAppService.java
│   ├── RoleManagementAppService.java
│   ├── MenuManagementAppService.java
│   └── dto/
│       ├── command/
│       └── query/
│
├── infrastructure/
│   └── persistence/
│       ├── JpaAdminUserRepository.java
│       ├── JpaAdminRoleRepository.java
│       ├── JpaAdminPermissionRepository.java
│       └── JpaAdminMenuRepository.java
│
└── web/
    └── controller/
        ├── AdminAuthController.java
        ├── AdminUserController.java
        ├── AdminRoleController.java
        ├── AdminMenuController.java
        └── AdminPermissionController.java
```

---

## 七、初始化数据

### 7.1 系统默认数据

- **角色**：超级管理员（SUPER_ADMIN）
- **菜单**：用户管理、角色管理、菜单管理、权限管理
- **权限**：admin:{resource}:{action} 格式
- **管理员**：
  - 用户名：admin
  - 密码：C@rt1s@n
  - 标记为系统用户，不可删除

### 7.2 ID 规则

- 预留 ID 段 1-10000 为系统数据
- 业务数据使用 TSID 生成器

---

## 八、Token 配置

### 8.1 过期时间

| 类型 | 过期时间 |
|------|----------|
| 默认 | 8 小时 |
| 记住我 | 7 天 |

### 8.2 配置方式

```yaml
sa-token:
  timeout: 28800              # 8 小时（默认）
  is-remember: true           # 启用记住我
  remember-time: 604800       # 7 天
```

---

## 九、权限标识规范

### 9.1 格式

```
{domain}:{resource}:{action}

示例：
  admin:user:read     # 后台-用户-查看
  admin:user:write    # 后台-用户-编辑
  admin:user:delete   # 后台-用户-删除
```

### 9.2 标准操作

| action | 说明 |
|--------|------|
| read | 查询/查看 |
| write | 新增/编辑 |
| delete | 删除 |
| execute | 执行操作 |
| audit | 审计/审核 |

---

## 十、与 Tenant Context 权限的区分

| 维度 | Platform Admin | Tenant |
|------|----------------|--------|
| **管理范围** | 整个平台 | 单个租户内 |
| **接口前缀** | `/api/v1/admin/*` | `/api/v1/tenant/*` |
| **loginType** | `admin` | `user` |
| **权限示例** | `admin:tenant:read` | `tenant:member:read` |
| **菜单** | 动态配置 | 固定设计，功能级控制 |
| **表名** | `admin_users`, `admin_roles`... | 待设计 |

---

## 十一、迁移文件

### 11.1 新增文件

```
db/migration/
├── V5__create_admin_tables.sql      # 管理员相关表
└── V6__init_admin_data.sql          # 初始化数据
```

### 11.2 依赖

无额外依赖，使用现有 cartisan-boot 模块。

---

## 十二、后续扩展

### 12.1 业务模块接入

后续业务模块（租户管理、模型管理等）接入时：
1. 在 `admin_menus` 表中添加菜单记录
2. 在 `admin_permissions` 表中添加权限记录
3. 在 Controller 上添加 `@RequirePermission` 注解

### 12.2 操作日志

可扩展增加管理员操作审计日志，记录：
- 操作人
- 操作时间
- 操作类型
- 操作资源
- 操作结果

---

**文档结束**
