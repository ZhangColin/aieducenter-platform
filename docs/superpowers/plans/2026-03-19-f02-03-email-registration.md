# F02-03 邮箱注册流程 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 POST /api/account/register/email 端点，完成验证码校验、用户创建、自动建租户、返回 Token 的完整注册流程。

**Architecture:** Account 上下文负责注册逻辑，通过 `UserRegisteredEvent`（ApplicationEventPublisher 同步发布）触发 Tenant 上下文创建 Personal 租户，两者共享同一事务。

**Tech Stack:** Java 21 / Spring Boot 3.4.x / Sa-Token（cartisan-security AuthenticationService）/ JPA + Flyway / Mockito + AssertJ

**Spec:** `docs/superpowers/specs/2026-03-19-f02-03-email-registration-design.md`

---

## 文件清单

| 操作 | 文件 |
|------|------|
| 修改 | `server/src/main/java/com/aieducenter/account/domain/aggregate/User.java` |
| 修改 | `server/src/main/java/com/aieducenter/account/domain/repository/UserRepository.java` |
| 新建 | `server/src/main/java/com/aieducenter/account/domain/event/UserRegisteredEvent.java` |
| 新建 | `server/src/main/java/com/aieducenter/account/application/dto/RegisterByEmailCommand.java` |
| 新建 | `server/src/main/java/com/aieducenter/account/application/dto/RegisterResult.java` |
| 新建 | `server/src/main/java/com/aieducenter/account/application/AccountRegistrationAppService.java` |
| 新建 | `server/src/main/java/com/aieducenter/account/web/AccountController.java` |
| 新建 | `server/src/main/java/com/aieducenter/tenant/domain/aggregate/Tenant.java` |
| 新建 | `server/src/main/java/com/aieducenter/tenant/domain/model/TenantType.java` |
| 新建 | `server/src/main/java/com/aieducenter/tenant/domain/repository/TenantRepository.java` |
| 新建 | `server/src/main/java/com/aieducenter/tenant/domain/error/TenantError.java` |
| 新建 | `server/src/main/java/com/aieducenter/tenant/application/TenantAppService.java` |
| 新建 | `server/src/main/java/com/aieducenter/tenant/application/event/UserRegisteredEventListener.java` |
| 新建 | `server/src/main/java/com/aieducenter/tenant/infrastructure/persistence/SpringDataJpaTenantRepository.java` |
| 新建 | `server/src/main/resources/db/migration/V3__create_tenants_table.sql` |
| 修改 | `server/src/test/java/com/aieducenter/account/domain/aggregate/UserTest.java` |
| 新建 | `server/src/test/java/com/aieducenter/account/application/AccountRegistrationAppServiceTest.java` |
| 新建 | `server/src/test/java/com/aieducenter/tenant/domain/aggregate/TenantTest.java` |
| 新建 | `server/src/test/java/com/aieducenter/tenant/application/TenantAppServiceTest.java` |
| 新建 | `server/src/test/java/com/aieducenter/account/web/AccountRegistrationIntegrationTest.java` |

---

## Task 1: User 聚合根 - 密码强度校验

**修改：** `User.java`

- [ ] 在 `UserTest.java` 中补充密码强度测试（PASSWORD_WEAK 场景：纯数字、纯字母、7位、21位；通过场景：8位字母+数字）
- [ ] 运行 `./gradlew :server:test --tests "*.UserTest"` 确认新测试红灯（编译错误也算）
- [ ] 在 `User.java` 中新增私有方法 `validatePasswordStrength()`，正则 `^(?=.*[a-zA-Z])(?=.*\d).{8,20}$`，不符合抛 `UserError.PASSWORD_WEAK`
- [ ] 在构造函数和 `updatePassword()` 中调用该方法
- [ ] 注意：现有测试 `shouldMatchPassword_whenEmptyPassword` 和部分用空密码的测试会红灯，更新这些测试使用合法密码（如 `password123`）
- [ ] 运行 `./gradlew :server:test --tests "*.UserTest"` 确认全绿
- [ ] commit: `feat(user): add password strength validation`

---

## Task 2: User 聚合根 - registerByEmail 工厂方法 + UserRepository.save

**修改：** `User.java`、`UserRepository.java`

- [ ] 在 `UserTest.java` 补充 `registerByEmail` 测试：调用后 email 已绑定、nickname 正确
- [ ] 在 `User.java` 添加静态工厂方法 `registerByEmail(username, email, plainPassword, nickname)`：调用现有构造函数再调用 `updateEmail(email)` 返回 user
- [ ] 在 `UserRepository` 接口添加 `User save(User user)` 方法声明（JPA 已实现，只需接口声明）
- [ ] 运行 `./gradlew :server:test --tests "*.UserTest"` 确认全绿
- [ ] commit: `feat(user): add registerByEmail factory method and save to UserRepository`

---

## Task 3: Tenant 上下文骨架（领域层 + 数据库）

**新建：** `TenantType.java`、`Tenant.java`（JPA entity + @PrePersist TSID）、`TenantRepository.java`、`TenantError.java`、`V3__create_tenants_table.sql`

- [ ] 新建 `TenantType` 枚举（PERSONAL）
- [ ] 新建 `Tenant` 聚合根：继承 `SoftDeletable`，实现 `AggregateRoot<Tenant>`，字段 id/name/type/ownerId，构造函数赋值，@PrePersist 生成 TSID
- [ ] 新建 `TenantRepository` 接口（`@Port(PortType.REPOSITORY)`，继承 `BaseRepository`，暂无额外方法）
- [ ] 新建 `TenantError`（空枚举预留）
- [ ] 新建 Flyway 脚本 `V3__create_tenants_table.sql`（建表 + 索引，参考 spec）
- [ ] 在 `TenantTest.java` 写领域测试：构造 Tenant 后 name/type/ownerId 正确
- [ ] 运行 `./gradlew :server:test --tests "*.TenantTest"` 确认通过
- [ ] commit: `feat(tenant): add Tenant aggregate, repository, and DB migration`

---

## Task 4: Tenant 应用层 + 基础设施

**新建：** `TenantAppService.java`、`SpringDataJpaTenantRepository.java`

- [ ] 新建 `SpringDataJpaTenantRepository`（`@Adapter(PortType.REPOSITORY)`，extends `BaseRepository<Tenant, Long>, TenantRepository`）
- [ ] 新建 `TenantAppService`（`@Service`，构造注入 `TenantRepository`，实现 `createPersonalTenant(Long ownerId, String name)`）
- [ ] 在 `TenantAppServiceTest.java` 写单元测试：mock TenantRepository，调用 `createPersonalTenant` 后验证 `save` 被调用、Tenant 字段正确
- [ ] 运行 `./gradlew :server:test --tests "*.TenantAppServiceTest"` 确认通过
- [ ] commit: `feat(tenant): add TenantAppService and JPA adapter`

---

## Task 5: UserRegisteredEvent + EventListener

**新建：** `UserRegisteredEvent.java`、`UserRegisteredEventListener.java`

- [ ] 新建 `UserRegisteredEvent` record（userId, username, email, nickname, occurredAt）
- [ ] 新建 `UserRegisteredEventListener`（`@Component`，`@EventListener`，nickname 为空时 fallback 到 username，调用 `tenantAppService.createPersonalTenant`）
- [ ] 在 `TenantAppServiceTest.java` 补充 listener fallback 测试：nickname 非空时用 nickname，nickname 为空时用 username
- [ ] 运行 `./gradlew :server:test --tests "*.TenantAppServiceTest"` 确认通过
- [ ] 运行 `./gradlew compileJava` 确认编译通过
- [ ] commit: `feat(tenant): add UserRegisteredEvent and listener`

---

## Task 6: 注册应用服务 + DTO + Controller

**新建：** `RegisterByEmailCommand.java`、`RegisterResult.java`、`AccountRegistrationAppService.java`、`AccountController.java`

- [ ] 新建 `RegisterByEmailCommand` record（username, email, password, nickname, verificationCode；username/email/password/verificationCode 加 `@NotBlank`，nickname 加 `@Size(max=50)`）
- [ ] 新建 `RegisterResult` record（token）
- [ ] 新建 `AccountRegistrationAppService`（构造注入 UserRepository、VerificationCodeAppService、AuthenticationService、ApplicationEventPublisher；`registerByEmail` 方法加 `@Transactional`，按 spec 8 步骤实现）
- [ ] 新建 `AccountController`（`@RestController`，`@RequestMapping("/api/account")`，`POST /register/email`，返回 `ApiResponse<RegisterResult>`，`@Valid` 校验入参）
- [ ] 运行 `./gradlew compileJava` 确认编译通过
- [ ] commit: `feat(account): add email registration app service and controller`

---

## Task 7: 单元测试 - AccountRegistrationAppService

**新建：** `AccountRegistrationAppServiceTest.java`

使用 `@ExtendWith(MockitoExtension.class)`，mock UserRepository、VerificationCodeAppService、AuthenticationService、ApplicationEventPublisher。

- [ ] 测试场景：成功注册（验证 token 返回、publishEvent 被调用）
- [ ] 测试场景：验证码错误（verifyCode 抛异常，直接透传）
- [ ] 测试场景：用户名重复（existsByUsername 返回 true，抛 USERNAME_ALREADY_EXISTS）
- [ ] 测试场景：邮箱重复（existsByEmail 返回 true，抛 EMAIL_ALREADY_EXISTS）
- [ ] 测试场景：密码强度不足（抛 PASSWORD_WEAK）
- [ ] 运行 `./gradlew :server:test --tests "*.AccountRegistrationAppServiceTest"` 确认全绿
- [ ] commit: `test(account): add AccountRegistrationAppService unit tests`

---

## Task 8: 集成测试

**新建：** `AccountRegistrationIntegrationTest.java`

使用 `@SpringBootTest(webEnvironment = MOCK)`，`@AutoConfigureMockMvc`，真实 Redis + PostgreSQL（参考 `AuthenticationIntegrationTest`）。

- [ ] 测试：完整注册成功 → 响应含 token、DB 有 user 记录、DB 有 tenant 记录
- [ ] 测试：重复邮箱注册 → 409
- [ ] 测试：密码强度不足 → 400（密码 `"weakpass"`）
- [ ] 运行 `./gradlew :server:test --tests "*.AccountRegistrationIntegrationTest"` 确认全绿
- [ ] commit: `test(account): add email registration integration tests`

---

## Task 9: 全量验证

- [ ] 运行 `./gradlew :server:test` 确认所有测试绿灯
- [ ] 运行 `./gradlew check`（含 ArchUnit）确认架构规则通过
- [ ] commit（如有修复）
