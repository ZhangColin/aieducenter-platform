# Feature: F02-01 User 聚合根与领域模型 — 实施计划

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 3 完成

---

## 目标复述

建立 User 聚合根，使用 String 类型存储 username/email/phoneNumber，使用 hutool 工具类进行验证，实现密码加密策略、UserRepository 接口及实现。

---

## 变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新增 | `server/build.gradle.kts` | 添加 cartisan-data-jpa 依赖、hutool 依赖 |
| 新增 | `.../account/domain/error/UserError.java` | 错误码枚举 |
| 新增 | `.../account/domain/repository/UserRepository.java` | 仓储接口 |
| 新增 | `.../account/domain/aggregate/User.java` | 用户聚合根（含验证逻辑） |
| 新增 | `.../account/infrastructure/persistence/JpaUserRepository.java` | 仓储实现 |
| 新增 | `.../account/domain/aggregate/UserTest.java` | 测试 |
| 新增 | `.../account/infrastructure/persistence/UserRepositoryTest.java` | 集成测试 |
| 新增 | `.../resources/db/migration/V1__create_users_table.sql` | Flyway 迁移脚本 |

> **设计决策**：不使用单值值对象（如 Email、PhoneNumber、Username），直接使用 String 类型，验证逻辑使用 hutool 工具类，在 User 聚合根的构造函数和更新方法中集中处理。详见 [cartisan-boot/docs/decisions/值对象的JPA映射策略.md](../../../../cartisan-boot/docs/decisions/值对象的JPA映射策略.md)。

---

## 核心流程

```
1. 配置依赖和基础设置
   └─> 添加 cartisan-data-jpa 依赖
   └─> 添加 hutool 依赖（用于验证）
   └─> 启用 JPA 审计功能

2. 创建错误码
   └─> UserError 枚举

3. 创建仓储接口
   └─> UserRepository 接口定义

4. 创建聚合根（带测试）
   └─> User 聚合根（使用 String 字段，构造函数中用 hutool 验证）

5. 创建基础设施层
   └─> JpaUserRepository 实现

6. 创建集成测试
   └─> UserRepositoryTest

7. 创建数据库迁移脚本
   └─> Flyway SQL
```

---

## 原子任务清单

### Step 1: 配置依赖和 JPA 审计

- **文件**: `server/build.gradle.kts`
- **内容**:
  - 添加 `implementation("com.cartisan:cartisan-data-jpa:0.1.0-SNAPSHOT")`
  - 添加 `implementation("org.springframework.security:spring-security-crypto")`
  - 添加 `implementation("cn.hutool:hutool-core:5.8.+")`（用于验证）
  - 添加 Spring JPA 审计配置类
- **验证**: `./gradlew compileJava` 通过

### Step 2: 创建 UserError 错误码枚举

- **文件**: `server/src/main/java/.../account/domain/error/UserError.java`
- **内容**:
  - 实现 `CodeMessage` 接口
  - 定义错误码（USERNAME_INVALID、EMAIL_INVALID、PHONE_INVALID 等）
- **验证**: 编译通过

### Step 3: 创建 UserRepository 接口

- **文件**: `server/src/main/java/.../account/domain/repository/UserRepository.java`
- **内容**:
  - 继承 `BaseRepository<User, Long>`
  - 定义 exists/findBy 方法
- **验证**: 编译通过

### Step 4: 编写 User 测试（红灯）

- **文件**: `server/src/test/java/.../account/domain/aggregate/UserTest.java`
- **内容**:
  - 测试用例：
    - 创建用户（username、email、phoneNumber 验证）
    - nickname 默认值
    - 密码匹配/不匹配
    - 修改各字段（含验证）
- **验证**: 编译通过 + 测试红灯

### Step 5: 编写 User 实现（绿灯）

- **文件**: `server/src/main/java/.../account/domain/aggregate/User.java`
- **内容**:
  - 继承 `SoftDeletable`
  - 字段：`String id`, `String username`, `String email`, `String phoneNumber`, `String password`, `String nickname`, `String avatar`
  - JPA 注解：`@Entity`, `@Table`, `@Column` 等
  - 构造函数中使用 hutool 验证：
    - `Validator.isEmail()` 验证邮箱
    - `Validator.isMobile()` 验证手机号
    - 自定义正则验证用户名
  - 方法：matchesPassword、updateEmail、updatePhoneNumber、updatePassword 等
  - 使用 `BCryptPasswordEncoder`
  - `@PrePersist` 生成 TSID
- **验证**: 测试全绿 + ArchUnit 通过

### Step 6: 创建 JpaUserRepository 实现

- **文件**: `server/src/main/java/.../account/infrastructure/persistence/JpaUserRepository.java`
- **内容**:
  - `@Adapter(PortType.REPOSITORY)`
  - 继承 `JpaRepository<User, Long>` 并实现 `UserRepository` 接口
- **验证**: 编译通过

### Step 7: 编写 UserRepository 集成测试

- **文件**: `server/src/test/java/.../account/infrastructure/persistence/UserRepositoryTest.java`
- **内容**:
  - 继承 `IntegrationTestBase`
  - 测试：保存、查询、exists检查、软删除
- **验证**: 测试全绿 + Docker 可用

### Step 8: 创建数据库迁移脚本

- **文件**: `server/src/main/resources/db/migration/V1__create_users_table.sql`
- **内容**:
  - CREATE TABLE users
  - CREATE INDEX
  - COMMENT
- **验证**: Flyway 迁移成功

---

## 执行顺序

```
Step 1 → Step 2 → Step 3
                    ↓
                Step 4 → Step 5
                    ↓
                   Step 6
                    ↓
                Step 7 → Step 8
```

**注意**：Step 4-5 是测试-实现对，必须按顺序执行（先红灯后绿灯）。

---

## 验证门禁

### 编译检查
```bash
./gradlew compileJava
./gradlew compileTestJava
```

### 测试检查
```bash
./gradlew test
```

### ArchUnit 检查
```bash
./gradlew test --tests "*ArchitectureTest"
```

### 飞行路径检查
```bash
./gradlew flywayMigrateInfo
```

---

## 预估工作量

| Step | 描述 | 预估行数 |
|------|------|----------|
| 1 | 依赖配置 | ~25 行 |
| 2 | UserError 枚举 | ~40 行 |
| 3 | UserRepository 接口 | ~30 行 |
| 4-5 | User + 测试（含验证逻辑） | ~250 行 |
| 6 | JpaUserRepository 实现 | ~30 行 |
| 7 | 集成测试 | ~100 行 |
| 8 | 迁移脚本 | ~30 行 |
| **总计** | | **~505 行** |

---

## 完成标准

- [ ] 所有测试绿灯
- [ ] ArchUnit 通过
- [ ] Flyway 迁移成功
- [ ] 代码符合 SKILL.md 规范
- [ ] 测试命名使用 `should_when` 格式
