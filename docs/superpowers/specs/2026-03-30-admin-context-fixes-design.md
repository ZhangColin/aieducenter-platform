# Admin 上下文代码规范修复与测试补充 - 设计文档

**日期：** 2026-03-30
**作者：** Claude
**状态：** 草案

## 1. 概述

### 1.1 目标

修复 Admin 上下文中不符合 cartisan-boot 框架规范的代码，并补充缺失的测试文件，确保代码质量和测试覆盖率。

### 1.2 背景

通过代码审查发现：
- 1 处代码规范问题：领域层未使用 `Assertions.require()`
- 8 个测试文件缺失（`AdminUserAuthAppServiceTest` 已存在）

## 2. 代码修复

### 2.1 问题定位

**文件：** `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java`

**问题：**
- `validateUsername()` 方法（行 273-276）直接抛 `DomainException`
- `validatePasswordStrength()` 方法（行 279-282）直接抛 `DomainException`

### 2.2 修复方案

根据 cartisan-boot 使用手册 3.3 节，领域层应使用 `Assertions.require()` 抛出异常。

**修复前：**
```java
private void validateUsername(String username) {
    if (username == null || !username.matches(USERNAME_PATTERN)) {
        throw new DomainException(AdminMessage.USERNAME_INVALID);
    }
}

private void validatePasswordStrength(String plainPassword) {
    if (plainPassword == null || !plainPassword.matches(PASSWORD_PATTERN)) {
        throw new DomainException(AdminMessage.PASSWORD_WEAK);
    }
}
```

**修复后：**
```java
private void validateUsername(String username) {
    Assertions.require(
        username != null && username.matches(USERNAME_PATTERN),
        AdminMessage.USERNAME_INVALID
    );
}

private void validatePasswordStrength(String plainPassword) {
    Assertions.require(
        plainPassword != null && plainPassword.matches(PASSWORD_PATTERN),
        AdminMessage.PASSWORD_WEAK
    );
}
```

## 3. 测试补充

### 3.1 测试策略

遵循现有测试模式：
- **应用服务测试**：使用 `@ExtendWith(MockitoExtension.class)`，纯 Mock
- **Controller API 测试**：使用 `MockMvcBuilders.standaloneSetup()`
- **测试命名**：`given_{条件}_when_{操作}_then_{预期结果}`
- **断言库**：AssertJ

### 3.2 需要补充的测试文件

| # | 测试文件 | 测试类型 | 覆盖方法 | 预计测试数 |
|---|----------|----------|----------|------------|
| 1 | `AdminUserManagementAppServiceTest` | 应用服务 | findAll, create, update, delete, updateStatus, assignRoles, resetPassword, findById | ~12 |
| 2 | `MenuManagementAppServiceTest` | 应用服务 | findAll, create, update, delete, assignToRole, getTree, findById | ~10 |
| 3 | `AdminUserPermissionAppServiceTest` | 应用服务 | getRoleCodes, getPermissions, getMenus, hasRole, hasPermission | ~6 |
| 4 | `PermissionScanAppServiceTest` | 应用服务 | scanAll, scanByScope, syncPermissions | ~5 |
| 5 | `AdminUserControllerTest` | API | GET /api/v1/admin/users, POST, PUT, DELETE, PUT /status, PUT /roles, PUT /password | ~8 |
| 6 | `AdminMenuControllerTest` | API | GET /api/v1/admin/menus, GET /tree, POST, PUT, DELETE | ~7 |
| 7 | `AdminAuthControllerTest` | API | POST /login, POST /logout, GET /current, PUT /password | ~5 |
| 8 | `PermissionControllerTest` | API | GET /permissions, GET /scan, POST /sync | ~4 |

### 3.3 测试模板

#### 应用服务测试模板

```java
package com.aieducenter.admin.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class XxxAppServiceTest {

    @Mock
    private XxxRepository xxxRepository;

    private XxxAppService service;

    @BeforeEach
    void setUp() {
        service = new XxxAppService(xxxRepository);
    }

    @Test
    void given_valid_input_when_methodName_then_success() {
        // Given
        // When
        // Then
    }

    @Test
    void given_invalid_input_when_methodName_then_throw_exception() {
        // Given
        // When & Then
    }
}
```

#### Controller API 测试模板

```java
package com.aieducenter.admin.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class XxxControllerTest {

    @Mock
    private XxxAppService xxxAppService;

    private XxxController controller;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        controller = new XxxController(xxxAppService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void given_request_when_endpoint_then_returnExpectedResponse() throws Exception {
        // Given
        // When & Then
        mvc.perform(get("/api/v1/xxx"))
                .andExpect(status().isOk());
    }
}
```

## 4. 实施计划

### 4.1 任务分解

**阶段 1：代码修复**
- Task 1: 修复 AdminUser.java 的断言方式

**阶段 2：应用服务测试**
- Task 2: AdminUserManagementAppServiceTest
- Task 3: MenuManagementAppServiceTest
- Task 4: AdminUserPermissionAppServiceTest
- Task 5: PermissionScanAppServiceTest

**阶段 3：Controller API 测试**
- Task 6: AdminUserControllerTest
- Task 7: AdminMenuControllerTest
- Task 8: AdminAuthControllerTest
- Task 9: PermissionControllerTest

### 4.2 验收标准

- 所有代码修复通过 `./gradlew compileJava`
- 所有新测试通过 `./gradlew test`
- 测试命名遵循 `given_when_then` 格式
- 测试使用 AssertJ 断言
- 测试覆盖主要业务场景（正常流程 + 异常流程）

## 5. 参考资料

- cartisan-boot 使用手册：`docs/guide/cartisan-boot-使用手册.md`
- 现有测试参考：`RoleManagementAppServiceTest.java`, `AdminRoleControllerTest.java`
- AdminUser 代码：`server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java`
