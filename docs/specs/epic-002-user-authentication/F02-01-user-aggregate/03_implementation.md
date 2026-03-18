# Feature: F02-01 User 聚合根与领域模型 — 实施计划

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 3 完成

---

## 目标复述

建立 User 聚合根及相关领域模型，包括 Username/Email/PhoneNumber 值对象、密码加密策略、UserRepository 接口及实现。

---

## 变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新增 | `server/build.gradle.kts` | 添加 cartisan-data-jpa 依赖 |
| 新增 | `.../account/domain/error/UserError.java` | 错误码枚举 |
| 新增 | `.../account/domain/valueobject/Username.java` | 用户名值对象 |
| 新增 | `.../account/domain/valueobject/Email.java` | 邮箱值对象 |
| 新增 | `.../account/domain/valueobject/PhoneNumber.java` | 手机号值对象 |
| 新增 | `.../account/domain/repository/UserRepository.java` | 仓储接口 |
| 新增 | `.../account/domain/aggregate/User.java` | 用户聚合根 |
| 新增 | `.../account/infrastructure/persistence/JpaUser.java` | JPA 实体 |
| 新增 | `.../account/infrastructure/persistence/JpaUserRepository.java` | 仓储实现 |
| 新增 | `.../account/domain/valueobject/UsernameTest.java` | 测试 |
| 新增 | `.../account/domain/valueobject/EmailTest.java` | 测试 |
| 新增 | `.../account/domain/valueobject/PhoneNumberTest.java` | 测试 |
| 新增 | `.../account/domain/aggregate/UserTest.java` | 测试 |
| 新增 | `.../account/infrastructure/persistence/UserRepositoryTest.java` | 集成测试 |
| 新增 | `.../resources/db/migration/V1__create_users_table.sql` | Flyway 迁移脚本 |

---

## 核心流程

```
1. 配置依赖和基础设置
   └─> 添加 cartisan-data-jpa 依赖
   └─> 启用 JPA 审计功能

2. 创建值对象（带测试）
   └─> Username → Email → PhoneNumber

3. 创建错误码
   └─> UserError 枚举

4. 创建仓储接口
   └─> UserRepository 接口定义

5. 创建聚合根（带测试）
   └─> User 聚合根

6. 创建基础设施层
   └─> JpaUser 实体
   └─> JpaUserRepository 实现

7. 创建集成测试
   └─> UserRepositoryTest

8. 创建数据库迁移脚本
   └─> Flyway SQL
```

---

## 原子任务清单

### Step 1: 配置依赖和 JPA 审计

- **文件**: `server/build.gradle.kts`
- **内容**:
  - 添加 `implementation("com.cartisan:cartisan-data-jpa:0.1.0-SNAPSHOT")`
  - 添加 `implementation("org.springframework.security:spring-security-crypto")`
  - 添加 Spring JPA 审计配置类
- **验证**: `./gradlew compileJava` 通过

### Step 2: 创建 UserError 错误码枚举

- **文件**: `server/src/main/java/.../account/domain/error/UserError.java`
- **内容**:
  - 实现 `CodeMessage` 接口
  - 定义 10 个错误码（USERNAME_INVALID 等）
- **验证**: 编译通过

### Step 3: 编写 Username 测试（红灯）

- **文件**: `server/src/test/java/.../account/domain/valueobject/UsernameTest.java`
- **内容**:
  - 测试用例：格式正确、太短、数字开头、特殊字符、值相等
  - 使用 AssertJ 断言
- **验证**: 编译通过 + 测试红灯（实现不存在）

### Step 4: 编写 Username 实现（绿灯）

- **文件**: `server/src/main/java/.../account/domain/valueobject/Username.java`
- **内容**:
  - Record 实现 `ValueObject<String>`
  - compact constructor 中校验格式
  - 正则：`^[a-zA-Z][a-zA-Z0-9_]{2,19}$`
- **验证**: 测试全绿 + ArchUnit 通过

### Step 5: 编写 Email 测试（红灯）

- **文件**: `server/src/test/java/.../account/domain/valueobject/EmailTest.java`
- **内容**:
  - 测试用例：格式正确、格式错误、值相等
- **验证**: 编译通过 + 测试红灯

### Step 6: 编写 Email 实现（绿灯）

- **文件**: `server/src/main/java/.../account/domain/valueobject/Email.java`
- **内容**:
  - Record 实现 `ValueObject<String>`
  - compact constructor 中校验邮箱格式
- **验证**: 测试全绿

### Step 7: 编写 PhoneNumber 测试（红灯）

- **文件**: `server/src/test/java/.../account/domain/valueobject/PhoneNumberTest.java`
- **内容**:
  - 测试用例：格式正确、非1开头、第二位非法、值相等
- **验证**: 编译通过 + 测试红灯

### Step 8: 编写 PhoneNumber 实现（绿灯）

- **文件**: `server/src/main/java/.../account/domain/valueobject/PhoneNumber.java`
- **内容**:
  - Record 实现 `ValueObject<String>`
  - compact constructor 中校验格式
  - 正则：`^1[3-9]\d{9}$`
- **验证**: 测试全绿

### Step 9: 创建 UserRepository 接口

- **文件**: `server/src/main/java/.../account/domain/repository/UserRepository.java`
- **内容**:
  - 继承 `BaseRepository<User, Long>`
  - 定义 exists/findBy 方法
- **验证**: 编译通过

### Step 10: 编写 User 测试（红灯）

- **文件**: `server/src/test/java/.../account/domain/aggregate/UserTest.java`
- **内容**:
  - 测试用例：创建、nickname默认、密码匹配/不匹配、修改各字段
- **验证**: 编译通过 + 测试红灯

### Step 11: 编写 User 实现（绿灯）

- **文件**: `server/src/main/java/.../account/domain/aggregate/User.java`
- **内容**:
  - 继承 `SoftDeletable`
  - 字段：id, username, email, phoneNumber, password, nickname, avatar
  - 方法：构造函数、matchesPassword、updateUsername、updateNickname、updateAvatar、updatePassword
  - 使用 `BCryptPasswordEncoder`
- **验证**: 测试全绿 + ArchUnit 通过

### Step 12: 创建 JpaUser 实体

- **文件**: `server/src/main/java/.../account/infrastructure/persistence/JpaUser.java`
- **内容**:
  - `@Entity`、`@Table(name = "users")`
  - 继承 `SoftDeletable`
  - JPA 注解映射
  - `@PrePersist` 生成 TSID
- **验证**: 编译通过

### Step 13: 创建 JpaUserRepository 实现

- **文件**: `server/src/main/java/.../account/infrastructure/persistence/JpaUserRepository.java`
- **内容**:
  - `@Adapter(PortType.REPOSITORY)`
  - 实现 `UserRepository` 接口
  - 委托给 `JpaRepository<User, Long>`
- **验证**: 编译通过

### Step 14: 编写 UserRepository 集成测试

- **文件**: `server/src/test/java/.../account/infrastructure/persistence/UserRepositoryTest.java`
- **内容**:
  - 继承 `IntegrationTestBase`
  - 测试：保存、查询、exists检查、软删除
- **验证**: 测试全绿 + Docker 可用

### Step 15: 创建数据库迁移脚本

- **文件**: `server/src/main/resources/db/migration/V1__create_users_table.sql`
- **内容**:
  - CREATE TABLE users
  - CREATE INDEX
  - COMMENT
- **验证**: Flyway 迁移成功

---

## 执行顺序

```
Step 1 → Step 2 → Step 3 → Step 4 → Step 5 → Step 6
                              ↓
                         Step 7 → Step 8
                              ↓
                         Step 9
                              ↓
                        Step 10 → Step 11
                              ↓
                         Step 12 → Step 13
                              ↓
                         Step 14 → Step 15
```

**注意**：Step 3-4、5-6、7-8、10-11 是测试-实现对，必须按顺序执行（先红灯后绿灯）。

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
| 1 | 依赖配置 | ~20 行 |
| 2 | UserError 枚举 | ~50 行 |
| 3-4 | Username + 测试 | ~80 行 |
| 5-6 | Email + 测试 | ~60 行 |
| 7-8 | PhoneNumber + 测试 | ~60 行 |
| 9 | UserRepository 接口 | ~30 行 |
| 10-11 | User + 测试 | ~200 行 |
| 12 | JpaUser 实体 | ~100 行 |
| 13 | JpaUserRepository 实现 | ~50 行 |
| 14 | 集成测试 | ~100 行 |
| 15 | 迁移脚本 | ~30 行 |
| **总计** | | **~780 行** |

---

## 完成标准

- [ ] 所有测试绿灯
- [ ] ArchUnit 通过
- [ ] Flyway 迁移成功
- [ ] 代码符合 SKILL.md 规范
- [ ] 测试命名使用 `should_when` 格式
