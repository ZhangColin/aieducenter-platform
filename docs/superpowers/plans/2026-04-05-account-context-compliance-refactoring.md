# Account 上下文规范符合性重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 Account 上下文全面调整为符合《限界上下文代码编写规范》，修复 11 个规范问题

**架构:** 六边形架构重构：领域层 → 应用层（DTO 分层 + Mapper） → 接口层（包重命名 + 注解完善），同时进行数据库迁移和测试更新

**Tech Stack:** Java 21, Spring Boot 3.4.x, MapStruct, JPA, Flyway, cartisan-boot 框架

---

## 文件结构映射

### 新建文件（5个）
- `server/src/main/java/com/aieducenter/account/application/dto/response/UserResponse.java`
- `server/src/main/java/com/aieducenter/account/application/mapper/UserMapper.java`
- `server/src/main/java/com/aieducenter/account/endpoints/controller/AccountController.java`（从 web 移动）
- `server/src/main/resources/db/migration/V12__rename_account_users_table.sql`
- `server/src/test/java/com/aieducenter/account/application/mapper/UserMapperTest.java`

### 移动文件（8个）
- DTO 文件重组到 command/response 子包（6个）
- Controller 重命名到 endpoints/controller（1个）
- 测试工具类移动（1个）

### 修改文件（15个）
- package-info.java（SubDomain、包结构说明）
- User.java（表名、导入顺序）
- AppService 类（添加 @Transactional）
- 测试文件（更新导入语句）

---

## Task 1: 准备工作

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/package-info.java`

- [ ] **Step 1: 创建功能分支**

```bash
git checkout develop
git pull origin develop
git checkout -b refactor/account-context-compliance
```

- [ ] **Step 2: 更新 package-info.java 的 SubDomain 类型和包结构说明**

修改 `server/src/main/java/com/aieducenter/account/package-info.java` 的完整内容：

```java
/**
 * Account Context。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>用户注册（用户名/邮箱/手机号）</li>
 *   <li>用户登录（密码登录/手机短信验证码登录）</li>
 *   <li>密码管理（修改密码/重置密码）</li>
 *   <li>用户信息管理（昵称/头像/邮箱/手机号）</li>
 * </ul>
 *
 * <h3>限界上下文</h3>
 * <p>平台用户账号中心，独立于 Admin Context 和 Tenant Context</p>
 *
 * <h3>包结构</h3>
 * <ul>
 *   <li>domain - 领域层：聚合根（User）、仓储接口、领域服务、枚举、端口接口</li>
 *   <li>application - 应用层：应用服务（AccountRegistrationAppService、AccountLoginAppService 等）、DTO</li>
 *   <li>infrastructure - 基础设施层：持久化、缓存、消息队列等南向接口适配器</li>
 *   <li>endpoints - 北向接口适配器层：REST API（Controller）</li>
 * </ul>
 *
 * @since 0.1.0
 */
@BoundedContext(
    name = "Account",
    subDomain = SubDomain.SUPPORTING  // CORE → SUPPORTING
)
package com.aieducenter.account;

import com.cartisan.core.stereotype.BoundedContext;
import com.cartisan.core.stereotype.SubDomain;
```

- [ ] **Step 3: 验证编译**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

- [ ] **Step 4: 提交更改**

```bash
git add server/src/main/java/com/aieducenter/account/package-info.java
git commit -m "refactor(account): update SubDomain from CORE to SUPPORTING"
```

---

## Task 2: 领域层调整 - 表名重命名

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/domain/aggregate/User.java`
- Create: `server/src/main/resources/db/migration/V12__rename_account_users_table.sql`

- [ ] **Step 1: 创建数据库迁移脚本**

创建 `server/src/main/resources/db/migration/V12__rename_account_users_table.sql`:

```sql
-- ========================================================================
-- Account Context: 重命名用户表（acc_users → act_users）
-- ========================================================================

-- PostgreSQL / MySQL
ALTER TABLE acc_users RENAME TO act_users;
```

- [ ] **Step 2: 修改 User.java 表名注解**

修改 `server/src/main/java/com/aieducenter/account/domain/aggregate/User.java` 第 38 行:

```java
@Table(name = "act_users")  // acc_users → act_users
```

- [ ] **Step 3: 优化导入顺序（User.java）**

确保导入顺序符合规范：Java 标准库 → hutool → Jakarta/Spring → cartisan → 项目内部

- [ ] **Step 4: 运行测试验证**

```bash
cd server && mvn test -Dtest UserTest
```

Expected: TESTS PASS

- [ ] **Step 5: 提交更改**

```bash
git add server/src/main/resources/db/migration/V12__rename_account_users_table.sql
git add server/src/main/java/com/aieducenter/account/domain/aggregate/User.java
git commit -m "refactor(account): rename table from acc_users to act_users"
```

---

## Task 3: 应用层 - DTO 子包重组

**Files:**
- Move: DTO 文件到子包（6个）
- Modify: 所有导入这些 DTO 的类

- [ ] **Step 1: 创建 command 子包**

```bash
mkdir -p server/src/main/java/com/aieducenter/account/application/dto/command
```

- [ ] **Step 2: 移动 Command DTO 文件**

使用 IDE 重构或 git mv 命令移动文件：

```bash
cd server/src/main/java/com/aieducenter/account/application/dto

# 移动 Command DTO
git mv RegisterCommand.java command/
git mv LoginByPasswordCommand.java command/
git mv LoginBySmsCommand.java command/
git mv ResetPasswordCommand.java command/
```

- [ ] **Step 3: 创建 response 子包**

```bash
mkdir -p server/src/main/java/com/aieducenter/account/application/dto/response
```

- [ ] **Step 4: 移动 Response DTO 文件**

```bash
cd server/src/main/java/com/aieducenter/account/application/dto

# 移动 Response DTO
git mv LoginResult.java response/
git mv RegisterResult.java response/
```

- [ ] **Step 5: 更新所有导入语句**

修改以下文件的导入语句（共 7 个文件）：
- `AccountRegistrationAppService.java`
- `AccountLoginAppService.java`
- `AccountPasswordResetAppService.java`
- `AccountQueryAppService.java`
- `AccountController.java`
- `AuthenticationConfigTest.java`（如存在引用）
- `AuthenticationIntegrationTest.java`（如存在引用）

将：
```java
import com.aieducenter.account.application.dto.RegisterCommand;
import com.aieducenter.account.application.dto.LoginResult;
```

改为：
```java
import com.aieducenter.account.application.dto.command.RegisterCommand;
import com.aieducenter.account.application.dto.response.LoginResult;
```

- [ ] **Step 6: 验证编译**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

- [ ] **Step 7: 提交更改**

```bash
git add server/src/main/java/com/aieducenter/account/application/dto/
git add server/src/main/java/com/aieducenter/account/application/
git add server/src/main/java/com/aieducenter/account/web/
git commit -m "refactor(account): reorganize DTO into command/response subpackages"
```

---

## Task 4: 应用层 - 创建 UserResponse 和 UserMapper

**Files:**
- Create: `server/src/main/java/com/aieducenter/account/application/dto/response/UserResponse.java`
- Create: `server/src/main/java/com/aieducenter/account/application/mapper/UserMapper.java`
- Create: `server/src/test/java/com/aieducenter/account/application/mapper/UserMapperTest.java`

- [ ] **Step 1: 创建 UserResponse DTO**

创建 `server/src/main/java/com/aieducenter/account/application/dto/response/UserResponse.java`:

```java
package com.aieducenter.account.application.dto.response;

import java.time.LocalDateTime;

/**
 * 用户响应 DTO。
 *
 * <p>用于返回用户详细信息，未来可用于用户信息查询 API。</p>
 */
public record UserResponse(
    Long id,
    String username,
    String nickname,
    String email,
    String phoneNumber,
    String avatar,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

- [ ] **Step 2: 创建 mapper 子包**

```bash
mkdir -p server/src/main/java/com/aieducenter/account/application/mapper
```

- [ ] **Step 3: 创建 UserMapper 接口**

创建 `server/src/main/java/com/aieducenter/account/application/mapper/UserMapper.java`:

```java
package com.aieducenter.account.application.mapper;

import com.aieducenter.account.application.dto.response.UserResponse;
import com.aieducenter.account.domain.aggregate.User;
import com.cartisan.data.jpa.mapping.DomainMapper;
import org.mapstruct.Mapper;

/**
 * User 聚合根与 UserResponse DTO 的转换器。
 */
@Mapper(componentModel = "spring")
public interface UserMapper extends DomainMapper<User, UserResponse> {
    @Override
    UserResponse convert(User user);

    // convertList() 和 convertSet() 由 DomainMapper 基类提供
}
```

- [ ] **Step 4: 编写测试 - UserMapper 基本转换测试**

创建 `server/src/test/java/com/aieducenter/account/application/mapper/UserMapperTest.java`:

```java
package com.aieducenter.account.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aieducenter.account.application.dto.response.UserResponse;
import com.aieducenter.account.domain.aggregate.User;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
    }

    @Test
    void given_user_with_all_fields_when_convert_then_responseCorrect() {
        // Given
        User user = User.register("john_doe", "encoded", "John", "john@example.com", "13812345678");

        // When
        UserResponse response = userMapper.convert(user);

        // Then
        assertThat(response.username()).isEqualTo("john_doe");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.phoneNumber()).isEqualTo("13812345678");
    }

    @Test
    void given_user_with_null_fields_when_convert_then_responseCorrect() {
        // Given
        User user = User.register("john_doe", "encoded", "John", null, null);

        // When
        UserResponse response = userMapper.convert(user);

        // Then
        assertThat(response.username()).isEqualTo("john_doe");
        assertThat(response.email()).isNull();
        assertThat(response.phoneNumber()).isNull();
    }

    @Test
    void given_user_list_when_convertList_then_allConverted() {
        // Given
        var users = java.util.List.of(
            User.register("user1", "encoded", "User 1", null, null),
            User.register("user2", "encoded", "User 2", "user2@example.com", "13812345678")
        );

        // When
        var responses = userMapper.convertList(users);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).username()).isEqualTo("user1");
        assertThat(responses.get(1).email()).isEqualTo("user2@example.com");
    }
}
```

- [ ] **Step 5: 运行测试验证**

```bash
cd server && mvn test -Dtest UserMapperTest
```

Expected: TESTS PASS

验证 MapStruct 生成文件：
```bash
ls -la server/target/generated-sources/annotations/com/aieducenter/account/application/mapper/UserMapperImpl.java
```

Expected: 文件存在（MapStruct 自动生成）

- [ ] **Step 6: 提交更改**

```bash
git add server/src/main/java/com/aieducenter/account/application/dto/response/UserResponse.java
git add server/src/main/java/com/aieducenter/account/application/mapper/
git add server/src/test/java/com/aieducenter/account/application/mapper/
git commit -m "feat(account): add UserResponse DTO and UserMapper"
```

---

## Task 5: 应用层 - 添加 @Transactional 注解

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/application/AccountLoginAppService.java`
- Modify: `server/src/main/java/com/aieducenter/account/application/AccountQueryAppService.java`
- Modify: `server/src/main/java/com/aieducenter/account/application/AccountRegistrationAppService.java`
- Modify: `server/src/main/java/com/aieducenter/account/application/AccountPasswordResetAppService.java`

- [ ] **Step 1: 为 AccountLoginAppService 添加事务注解**

修改 `AccountLoginAppService.java`:

确认以下方法有 `@Transactional` 注解（应该已存在）：
```java
@Transactional  // 会产生会话状态，非只读
public LoginResult loginByPassword(LoginByPasswordCommand command) {
    // ... 现有逻辑
}

@Transactional  // 会产生会话状态，非只读
public LoginResult loginBySms(LoginBySmsCommand command) {
    // ... 现有逻辑
}
```

为 logout 方法添加 `@Transactional`：
```java
@Transactional  // 会清除会话状态
public void logout() {
    authenticationService.logout();
}
```

- [ ] **Step 2: 为 AccountQueryAppService 添加只读事务**

修改 `AccountQueryAppService.java`:

```java
@Transactional(readOnly = true)
public boolean isUsernameAvailable(String username) {
    return !userRepository.existsByUsername(username);
}

@Transactional(readOnly = true)
public boolean isPhoneNumberAvailable(String phoneNumber) {
    return !userRepository.existsByPhoneNumber(phoneNumber);
}
```

- [ ] **Step 3: 验证 AccountRegistrationAppService 和 AccountPasswordResetAppService**

检查以下文件，确认写操作方法有 `@Transactional` 注解：

```bash
# 检查 register 方法
grep -n "@Transactional" server/src/main/java/com/aieducenter/account/application/AccountRegistrationAppService.java

# 检查 resetPassword 方法
grep -n "@Transactional" server/src/main/java/com/aieducenter/account/application/AccountPasswordResetAppService.java
```

Expected: 每个文件至少有 1 个 `@Transactional` 注解

- [ ] **Step 4: 运行测试验证**

```bash
cd server && mvn test -Dtest *AppServiceTest
```

Expected: TESTS PASS

- [ ] **Step 5: 提交更改**

```bash
git add server/src/main/java/com/aieducenter/account/application/
git commit -m "refactor(account): add @Transactional annotations to AppServices"
```

---

## Task 6: 接口层 - 包重命名（web → endpoints/controller）

**Files:**
- Move: `server/src/main/java/com/aieducenter/account/web/AccountController.java`

- [ ] **Step 1: 创建 endpoints/controller 目录**

```bash
mkdir -p server/src/main/java/com/aieducenter/account/endpoints/controller
```

- [ ] **Step 2: 移动 AccountController 并更新包声明**

使用 IDE 重构功能移动文件，或手动移动：

```bash
cd server/src/main/java/com/aieducenter/account
git mv web/AccountController.java endpoints/controller/
```

更新 `AccountController.java` 的包声明：
```java
package com.aieducenter.account.endpoints.controller;
```

- [ ] **Step 3: 删除空的 web 包**

```bash
git rm -r web/
```

- [ ] **Step 4: 验证编译**

```bash
cd server && mvn compile
```

Expected: BUILD SUCCESS

- [ ] **Step 5: 提交更改**

```bash
git add server/src/main/java/com/aieducenter/account/
git commit -m "refactor(account): rename web package to endpoints/controller"
```

---

## Task 7: 接口层 - 添加完整注解

**Files:**
- Modify: `server/src/main/java/com/aieducenter/account/endpoints/controller/AccountController.java`

- [ ] **Step 1: 添加类级别注解**

在 `AccountController.java` 添加：

```java
@Validated                          // 新增
@Tag(name = "Account", description = "账号管理")  // 新增
public class AccountController {
```

确保导入：
```java
import jakarta.validation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
```

- [ ] **Step 2: 为公共 API 添加 @Operation 注解**

```java
@PostMapping("/register")
@Operation(summary = "用户注册", description = "通过用户名+密码或手机号+验证码注册新用户")
public ApiResponse<RegisterResult> register(@Valid @RequestBody RegisterCommand command) {
    // ...
}

@PostMapping("/login")
@Operation(summary = "密码登录", description = "通过用户名/邮箱/手机号+密码登录")
public ApiResponse<LoginResult> loginByPassword(@Valid @RequestBody LoginByPasswordCommand command) {
    // ...
}

@PostMapping("/login/sms")
@Operation(summary = "短信验证码登录", description = "通过手机号+短信验证码登录")
public ApiResponse<LoginResult> loginBySms(@Valid @RequestBody LoginBySmsCommand command) {
    // ...
}

@GetMapping("/check-username")
@Operation(summary = "检查用户名是否可用", description = "用于注册表单的实时用户名可用性验证")
public ApiResponse<Boolean> checkUsername(
        @RequestParam @NotBlank @Size(min = 3, max = 20) String username) {
    // ...
}

@GetMapping("/check-phone")
@Operation(summary = "检查手机号是否可用", description = "用于注册表单的实时手机号可用性验证")
public ApiResponse<Boolean> checkPhone(
        @RequestParam @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$") String phone) {
    // ...
}
```

确保导入：
```java
import io.swagger.v3.oas.annotations.Operation;
```

- [ ] **Step 3: 为需要认证的 API 添加权限注解**

```java
@PostMapping("/logout")
@RequireAuth  // 新增
@Operation(summary = "退出登录", description = "清除服务端会话状态")
public ApiResponse<Void> logout() {
    // ...
}

@PostMapping("/reset-password")
@RequireAuth  // 新增
@Operation(summary = "重置密码", description = "通过手机号验证码重置密码")
public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordCommand command) {
    // ...
}
```

确保导入：
```java
import com.cartisan.security.authentication.RequireAuth;
```

- [ ] **Step 4: 运行测试验证**

```bash
cd server && mvn test -Dtest *IntegrationTest
```

Expected: TESTS PASS

- [ ] **Step 5: 验证 Swagger 文档可访问**

启动应用并访问 Swagger UI：`http://localhost:8080/swagger-ui.html`
验证：
- Account 标签存在
- 所有 API 有完整描述

- [ ] **Step 6: 提交更改**

```bash
git add server/src/main/java/com/aieducenter/account/endpoints/controller/
git commit -m "refactor(account): add complete annotations to AccountController"
```

---

## Task 8: 测试更新 - 更新导入语句

**Files:**
- Modify: 所有测试文件（11个）：
  - 6 个应用服务测试
  - 3 个集成测试
  - 1 个测试工具类（TestAuthController）
  - 1 个配置测试（可能需要更新）

- [ ] **Step 1: 更新应用服务测试的导入**

修改以下文件，更新 DTO 导入路径：
- `AccountLoginAppServiceTest.java`
- `AccountRegistrationAppServiceTest.java`
- `AccountPasswordResetAppServiceTest.java`

将：
```java
import com.aieducenter.account.application.dto.RegisterCommand;
```

改为：
```java
import com.aieducenter.account.application.dto.command.RegisterCommand;
```

类似地更新其他 DTO 导入。

- [ ] **Step 2: 更新集成测试的导入**

修改以下文件，更新 Controller 导入路径：
- `AccountLoginIntegrationTest.java`
- `AccountRegistrationIntegrationTest.java`
- `AccountPasswordResetIntegrationTest.java`
- `TestAuthController.java`

将：
```java
import com.aieducenter.account.web.AccountController;
```

改为：
```java
import com.aieducenter.account.endpoints.controller.AccountController;
```

- [ ] **Step 3: 移动 TestAuthController 到 endpoints/controller**

```bash
cd server/src/test/java/com/aieducenter/account
git mv web/TestAuthController.java endpoints/controller/
```

更新包声明：
```java
package com.aieducenter.account.endpoints.controller;
```

- [ ] **Step 4: 运行所有测试验证**

```bash
cd server && mvn test
```

Expected: ALL TESTS PASS

- [ ] **Step 5: 提交更改**

```bash
git add server/src/test/java/com/aieducenter/account/
git commit -m "refactor(account): update test imports after package reorganization"
```

---

## Task 9: 最终验证

- [ ] **Step 1: 运行完整测试套件**

```bash
cd server && mvn verify
```

Expected:
- 所有单元测试通过
- 所有集成测试通过
- ArchUnit 架构测试通过

- [ ] **Step 2: 手动 API 测试**

启动应用并测试：

```bash
cd server && mvn spring-boot:run
```

测试公共 API（无需认证）：
```bash
# 检查用户名
curl http://localhost:8080/api/account/check-username?username=testuser

# 检查手机号
curl http://localhost:8080/api/account/check-phone?phone=13812345678
```

测试认证 API（需要 token）：
```bash
# 登录获取 token
TOKEN=$(curl -X POST http://localhost:8080/api/account/login \
  -H "Content-Type: application/json" \
  -d '{"account":"testuser","password":"Test1234","captchaId":"1","captchaCode":"1234"}' \
  | jq -r '.data.token')

# 退出登录
curl -X POST http://localhost:8080/api/account/logout \
  -H "Authorization: Bearer $TOKEN"
```

- [ ] **Step 3: 验证 Swagger UI**

访问 `http://localhost:8080/swagger-ui.html`：
- ✅ Account 标签显示
- ✅ 所有 API 有完整描述
- ✅ 公开 API 和认证 API 区分清晰

- [ ] **Step 4: 检查 ArchUnit 规则**

确认以下规则现在通过：
- ✅ 包命名：endpoints/controller 存在
- ✅ DTO 分层：command/response 子包存在
- ✅ 权限注解：@RequireAuth 存在

- [ ] **Step 5: 代码审查准备**

```bash
# 查看所有提交
git log --oneline develop..refactor/account-context-compliance

# 查看变更统计
git diff develop...refactor/account-context-compliance --stat
```

预期提交数量：8-10 个

---

## Task 10: 合并到 develop

- [ ] **Step 1: 拉取最新 develop**

```bash
git checkout develop
git pull origin develop
```

- [ ] **Step 2: 合并功能分支**

```bash
git merge refactor/account-context-compliance --no-ff
```

- [ ] **Step 3: 解决冲突（如有）**

如有冲突，按照以下原则解决：
- 优先保留功能分支的改动
- 确保导入语句符合规范
- 运行测试验证

- [ ] **Step 4: 运行完整验证**

```bash
cd server && mvn verify
```

Expected: ALL TESTS PASS

- [ ] **Step 5: 推送到远程**

```bash
git push origin develop
```

- [ ] **Step 6: 创建 Pull Request（如使用 GitHub）**

```bash
# 如果使用 GitHub Flow，创建 PR
gh pr create --title "refactor(account): Account 上下文规范符合性重构" \
  --body "修复 11 个规范问题，详见设计文档" \
  --base develop --head refactor/account-context-compliance
```

---

## 附录：验证清单

### 功能验收
- [ ] 所有 API 端点正常工作
- [ ] 测试套件 100% 通过
- [ ] ArchUnit 架构测试通过

### 规范验收
- [ ] 包结构符合文档要求
- [ ] 所有注解完整且正确
- [ ] 导入顺序符合规范
- [ ] 错误码符合 USER_XXX 格式

### 文档验收
- [ ] Swagger API 文档完整
- [ ] 代码注释清晰
- [ ] 设计文档已归档

---

## 故障排查

### 问题 1: MapStruct 生成的 UserMapperImpl 找不到

**原因**: MapStruct 注解处理器未配置

**解决**: 确认 `pom.xml` 包含：
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>
```

### 问题 2: ArchUnit 测试失败

**原因**: 包结构调整未完全同步

**解决**: 检查是否有遗漏的导入语句或包路径

### 问题 3: 数据库迁移失败

**原因**: 迁移版本号冲突

**解决**: 确认使用 V12（现有最高版本是 V11）

---

**实施计划完成！** 准备开始执行。
