# Admin 上下文代码规范修复与测试补充 - 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 AdminUser 中的断言方式，提交现有测试文件，补充 PermissionControllerTest

**Architecture:** 单一限界上下文（Admin）的代码质量改进，涉及领域层断言修复和测试覆盖补充

**Tech Stack:** Java 21, cartisan-boot 框架, JUnit 5, Mockito, AssertJ

---

## Task 1: 修复 AdminUser 断言方式

**Files:**
- Modify: `server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java:273-283`

**背景：** 根据 cartisan-boot 使用手册 3.3 节，领域层应使用 `Assertions.require()` 抛出 `DomainException`，而不是直接 throw。

- [ ] **Step 1: 定位需要修复的方法**

查看 `AdminUser.java` 的 `validateUsername()` 和 `validatePasswordStrength()` 方法（约第 273-283 行）。

- [ ] **Step 2: 修复 validateUsername() 方法**

将：
```java
private void validateUsername(String username) {
    if (username == null || !username.matches(USERNAME_PATTERN)) {
        throw new DomainException(AdminMessage.USERNAME_INVALID);
    }
}
```

改为：
```java
private void validateUsername(String username) {
    Assertions.require(
        username != null && username.matches(USERNAME_PATTERN),
        AdminMessage.USERNAME_INVALID
    );
}
```

- [ ] **Step 3: 修复 validatePasswordStrength() 方法**

将：
```java
private void validatePasswordStrength(String plainPassword) {
    if (plainPassword == null || !plainPassword.matches(PASSWORD_PATTERN)) {
        throw new DomainException(AdminMessage.PASSWORD_WEAK);
    }
}
```

改为：
```java
private void validatePasswordStrength(String plainPassword) {
    Assertions.require(
        plainPassword != null && plainPassword.matches(PASSWORD_PATTERN),
        AdminMessage.PASSWORD_WEAK
    );
}
```

- [ ] **Step 4: 验证编译通过**

```bash
cd server && ./gradlew compileJava
```

预期：BUILD SUCCESSFUL

- [ ] **Step 5: 运行 AdminUserTest 确保测试通过**

```bash
cd server && ./gradlew test --tests "*AdminUserTest"
```

预期：所有测试通过

- [ ] **Step 6: 提交修复**

```bash
git add server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java
git commit -m "$(cat <<'EOF'
fix: use Assertions.require() in AdminUser validation methods

Replace direct DomainException throws with Assertions.require()
in validateUsername() and validatePasswordStrength() methods
to comply with cartisan-boot framework conventions.

Ref: cartisan-boot usage manual section 3.3

Co-Authored-By: Claude Opus 4.6 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 2: 提交现有测试文件

**Files:**
- Add to git: 7 个已存在的测试文件

**背景：** 这些测试文件已存在但未提交到 git，需要添加到版本控制。

- [ ] **Step 1: 查看未跟踪的测试文件**

```bash
git status server/src/test/java/com/aieducenter/admin/
```

预期：看到以下未跟踪文件：
- `AdminUserManagementAppServiceTest.java`
- `MenuManagementAppServiceTest.java`
- `AdminUserPermissionAppServiceTest.java`
- `PermissionScanAppServiceTest.java`
- `AdminUserControllerTest.java`
- `AdminMenuControllerTest.java`
- `AdminAuthControllerTest.java`

- [ ] **Step 2: 运行所有测试确保通过**

```bash
cd server && ./gradlew test
```

预期：所有测试通过

- [ ] **Step 3: 添加测试文件到 git**

```bash
git add server/src/test/java/com/aieducenter/admin/application/AdminUserManagementAppServiceTest.java
git add server/src/test/java/com/aieducenter/admin/application/MenuManagementAppServiceTest.java
git add server/src/test/java/com/aieducenter/admin/application/AdminUserPermissionAppServiceTest.java
git add server/src/test/java/com/aieducenter/admin/application/PermissionScanAppServiceTest.java
git add server/src/test/java/com/aieducenter/admin/web/controller/AdminUserControllerTest.java
git add server/src/test/java/com/aieducenter/admin/web/controller/AdminMenuControllerTest.java
git add server/src/test/java/com/aieducenter/admin/web/controller/AdminAuthControllerTest.java
```

- [ ] **Step 4: 提交测试文件**

```bash
git commit -m "$(cat <<'EOF'
test: add application service and controller tests

Add comprehensive test coverage for:
- AdminUserManagementAppService (CRUD, status, roles, password)
- MenuManagementAppService (CRUD, tree, role assignment)
- AdminUserPermissionAppService (roles, permissions, menus)
- PermissionScanAppService (scan by scope)
- AdminUserController (API endpoints)
- AdminMenuController (API endpoints)
- AdminAuthController (login, logout, current user)

All tests follow given-when-then naming convention and use AssertJ.

Co-Authored-By: Claude Opus 4.6 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 3: 创建 PermissionControllerTest

**Files:**
- Create: `server/src/test/java/com/aieducenter/admin/web/controller/PermissionControllerTest.java`

**背景：** `PermissionController` 只有一个端点 `GET /api/v1/admin/permissions?scope=admin`，需要补充 API 测试。参考 `AdminRoleControllerTest.java` 的测试模式。

- [ ] **Step 1: 创建测试文件骨架**

```bash
cat > server/src/test/java/com/aieducenter/admin/web/controller/PermissionControllerTest.java << 'EOF'
package com.aieducenter.admin.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aieducenter.admin.application.PermissionScanAppService;
import com.aieducenter.admin.application.dto.response.PermissionResponse;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PermissionController API 测试。
 */
@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    @Mock
    private PermissionScanAppService permissionScanAppService;

    private PermissionController controller;
    private org.springframework.test.web.servlet.MockMvc mvc;

    @BeforeEach
    void setUp() {
        controller = new PermissionController(permissionScanAppService);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void given_default_scope_when_scanPermissions_then_returnPermissions() throws Exception {
        // Given
        when(permissionScanAppService.scanByScope("admin"))
                .thenReturn(List.of(
                        new PermissionResponse("admin:user:read", "用户管理-查看"),
                        new PermissionResponse("admin:user:write", "用户管理-编辑")
                ));

        // When & Then
        mvc.perform(get("/api/v1/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("admin:user:read"))
                .andExpect(jsonPath("$[0].name").value("用户管理-查看"))
                .andExpect(jsonPath("$[1].code").value("admin:user:write"));

        verify(permissionScanAppService).scanByScope("admin");
    }

    @Test
    void given_custom_scope_when_scanPermissions_then_returnPermissionsForScope() throws Exception {
        // Given
        when(permissionScanAppService.scanByScope("user"))
                .thenReturn(List.of(
                        new PermissionResponse("user:profile:read", "个人资料-查看")
                ));

        // When & Then
        mvc.perform(get("/api/v1/admin/permissions").param("scope", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("user:profile:read"))
                .andExpect(jsonPath("$[0].name").value("个人资料-查看"));

        verify(permissionScanAppService).scanByScope("user");
    }

    @Test
    void given_empty_permissions_when_scanPermissions_then_returnEmptyList() throws Exception {
        // Given
        when(permissionScanAppService.scanByScope("admin"))
                .thenReturn(List.of());

        // When & Then
        mvc.perform(get("/api/v1/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
EOF
```

- [ ] **Step 2: 编译测试**

```bash
cd server && ./gradlew compileTestJava
```

预期：BUILD SUCCESSFUL

- [ ] **Step 3: 运行测试**

```bash
cd server && ./gradlew test --tests "*PermissionControllerTest"
```

预期：所有测试通过

- [ ] **Step 4: 运行所有测试确保无回归**

```bash
cd server && ./gradlew test
```

预期：所有测试通过

- [ ] **Step 5: 提交测试**

```bash
git add server/src/test/java/com/aieducenter/admin/web/controller/PermissionControllerTest.java
git commit -m "$(cat <<'EOF'
test: add PermissionControllerTest

Add API tests for PermissionController:
- scanPermissions with default scope
- scanPermissions with custom scope
- empty permissions list

Follows existing test pattern from AdminRoleControllerTest.

Co-Authored-By: Claude Opus 4.6 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## 验收检查

- [ ] **Verify 1**: AdminUser.java 使用 `Assertions.require()` 而非直接 throw
- [ ] **Verify 2**: 所有测试通过 `./gradlew test`
- [ ] **Verify 3**: 测试命名遵循 `given_when_then` 格式
- [ ] **Verify 4**: 代码编译通过 `./gradlew compileJava`
- [ ] **Verify 5**: 所有更改已提交到 git

## 参考资料

- cartisan-boot 使用手册：`docs/guide/cartisan-boot-使用手册.md`
- AdminUser 代码：`server/src/main/java/com/aieducenter/admin/domain/aggregate/AdminUser.java`
- 参考测试：`server/src/test/java/com/aieducenter/admin/web/controller/AdminRoleControllerTest.java`
- 设计规范：`docs/superpowers/specs/2026-03-30-admin-context-fixes-design.md`
