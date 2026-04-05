# verification & tenant 上下文规范重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 修复 verification 和 tenant 两个上下文中的规范违规问题，包括添加 package-info.java、修正包结构、枚举存储方式、删除未使用代码和测试命名。

**架构:** 按上下文独立重构，每个上下文的任务互不依赖。verification 重命名包结构，tenant 修改枚举存储并创建数据库迁移脚本。

**技术栈:** Java 21, Spring Boot 3.4.x, cartisan-boot 框架, JPA, PostgreSQL, Flyway, JUnit 5, ArchUnit

---

## 前置检查

### Task 0: 验证环境

**目的:** 确保开发环境正常，避免因环境问题导致重构失败

- [ ] **Step 1: 检查 Git 状态**

```bash
cd server && git status
```
Expected: clean working tree (无未提交的更改)

- [ ] **Step 2: 运行 ArchUnit 测试**

```bash
cd server && ./gradlew test --tests "*ArchitectureTest"
```
Expected: 测试通过（记录当前状态，用于对比）

- [ ] **Step 3: 运行相关上下文测试**

```bash
cd server && ./gradlew test --tests "*verification*" --tests "*tenant*"
```
Expected: 所有测试通过

---

## Part 1: verification 上下文重构

### Task 1: 创建 verification/package-info.java

**文件:**
- 创建: `server/src/main/java/com/aieducenter/verification/package-info.java`

- [ ] **Step 1: 创建 package-info.java 文件**

```java
/**
 * 验证码限界上下文。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>邮箱验证码生成与校验</li>
 *   <li>短信验证码生成与校验</li>
 *   <li>图形验证码生成与校验</li>
 *   <li>发送限流（邮箱/手机/IP）</li>
 * </ul>
 *
 * @since 0.1.0
 */
@BoundedContext(name = "Verification", subDomain = SubDomain.SUPPORTING)
package com.aieducenter.verification;

import com.cartisan.core.stereotype.BoundedContext;
import com.cartisan.core.stereotype.SubDomain;
```

- [ ] **Step 2: 验证编译**

```bash
cd server && ./gradlew compileJava
```
Expected: 编译成功

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/verification/package-info.java
git commit -m "refactor(verification): add package-info.java with @BoundedContext"
```

---

### Task 2: 重命名 verification.web → verification.endpoints

**文件:**
- 移动: `server/src/main/java/com/aieducenter/verification/web/VerificationCodeController.java`
  → `server/src/main/java/com/aieducenter/verification/endpoints/VerificationCodeController.java`
- 移动: `server/src/main/java/com/aieducenter/verification/web/CaptchaController.java`
  → `server/src/main/java/com/aieducenter/verification/endpoints/CaptchaController.java`
- 修改测试文件中的 package 声明和 import 语句

- [ ] **Step 1: 创建新目录**

```bash
mkdir -p server/src/main/java/com/aieducenter/verification/endpoints
```

- [ ] **Step 2: 移动 VerificationCodeController.java**

```bash
cd server/src/main/java/com/aieducenter/verification
git mv web/VerificationCodeController.java endpoints/VerificationCodeController.java
```

- [ ] **Step 3: 修改 VerificationCodeController.java 包声明**

打开 `endpoints/VerificationCodeController.java`，修改第 1 行：

```java
// 修改前
package com.aieducenter.verification.web;

// 修改后
package com.aieducenter.verification.endpoints;
```

- [ ] **Step 4: 移动 CaptchaController.java**

```bash
cd server/src/main/java/com/aieducenter/verification
git mv web/CaptchaController.java endpoints/CaptchaController.java
```

- [ ] **Step 5: 修改 CaptchaController.java 包声明**

打开 `endpoints/CaptchaController.java`，修改第 1 行：

```java
// 修改前
package com.aieducenter.verification.web;

// 修改后
package com.aieducenter.verification.endpoints;
```

- [ ] **Step 6: 更新所有 import 语句（包括跨上下文引用）**

搜索所有引用旧包的文件：

```bash
cd server
grep -r "com.aieducenter.verification.web" --include="*.java" . | grep -v "package com.aieducenter.verification.web"
```

检查是否有其他上下文引用了 verification.web（跨上下文依赖）：

```bash
cd server/src/main/java
grep -r "com.aieducenter.verification.web" --include="*.java" . | grep -v "^./verification/"
```

如果发现跨上下文引用（如 admin、user 等），需要更新这些文件中的 import 语句：

```java
// 修改前
import com.aieducenter.verification.web.VerificationCodeController;
import com.aieducenter.verification.web.CaptchaController;

// 修改后
import com.aieducenter.verification.endpoints.VerificationCodeController;
import com.aieducenter.verification.endpoints.CaptchaController;
```

```java
// 修改前
import com.aieducenter.verification.web.VerificationCodeController;
import com.aieducenter.verification.web.CaptchaController;

// 修改后
import com.aieducenter.verification.endpoints.VerificationCodeController;
import com.aieducenter.verification.endpoints.CaptchaController;
```

- [ ] **Step 7: 删除旧的 web 目录**

```bash
cd server/src/main/java/com/aieducenter/verification
git rm -r web
```

- [ ] **Step 8: 更新测试文件的 package 声明**

检查测试文件：

```bash
cd server
find src/test -path "*verification/web*" -name "*.java"
```

对于每个测试文件，修改：
1. 文件夹路径：`verification/web/` → `verification/endpoints/`
2. package 声明：`package com.aieducenter.verification.web;` → `package com.aieducenter.verification.endpoints;`

例如：
```bash
# 移动测试文件
cd server/src/test/java/com/aieducenter/verification
git mv web/VerificationCodeControllerTest.java endpoints/VerificationCodeControllerTest.java
git mv web/CaptchaControllerTest.java endpoints/CaptchaControllerTest.java
```

然后修改每个测试文件顶部的 package 声明：
```java
// 修改前
package com.aieducenter.verification.web;

// 修改后
package com.aieducenter.verification.endpoints;
```

- [ ] **Step 9: 验证编译和测试**

```bash
cd server && ./gradlew compileJava test --tests "*verification*"
```
Expected: 编译成功，所有测试通过

- [ ] **Step 10: 提交**

```bash
git add -A
git commit -m "refactor(verification): rename web.controller to endpoints.controller

- Move VerificationCodeController to endpoints.controller
- Move CaptchaController to endpoints.controller
- Update all import statements
- Update test package declarations"
```

---

### Task 3: 删除未使用的 CaptchaCode 类

**文件:**
- 删除: `server/src/main/java/com/aieducenter/verification/domain/model/CaptchaCode.java`

- [ ] **Step 1: 确认 CaptchaCode 未被使用**

```bash
cd server
grep -r "CaptchaCode" --include="*.java" src/
```
Expected: 仅找到定义文件，无其他引用

- [ ] **Step 2: 删除 CaptchaCode.java**

```bash
cd server/src/main/java/com/aieducenter/verification/domain/model
git rm CaptchaCode.java
```

- [ ] **Step 3: 验证编译和测试**

```bash
cd server && ./gradlew compileJava test --tests "*verification*"
```
Expected: 编译成功，所有测试通过

- [ ] **Step 4: 提交**

```bash
git commit -m "refactor(verification): remove unused CaptchaCode class

Replaced by CaptchaGenerationService.CaptchaResult record"
```

---

## Part 2: tenant 上下文重构

### Task 4: 创建 tenant/package-info.java

**文件:**
- 创建: `server/src/main/java/com/aieducenter/tenant/package-info.java`

- [ ] **Step 1: 创建 package-info.java 文件**

```java
/**
 * 租户限界上下文。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>租户创建与管理</li>
 *   <li>租户类型（个人/企业）</li>
 *   <li>租户归属（ownerId）</li>
 * </ul>
 *
 * @since 0.1.0
 */
@BoundedContext(name = "Tenant", subDomain = SubDomain.CORE)
package com.aieducenter.tenant;

import com.cartisan.core.stereotype.BoundedContext;
import com.cartisan.core.stereotype.SubDomain;
```

- [ ] **Step 2: 验证编译**

```bash
cd server && ./gradlew compileJava
```
Expected: 编译成功

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/aieducenter/tenant/package-info.java
git commit -m "refactor(tenant): add package-info.java with @BoundedContext"
```

---

### Task 5: 修改 Tenant.type 枚举存储方式

**文件:**
- 修改: `server/src/main/java/com/aieducenter/tenant/domain/aggregate/Tenant.java:38-41`

- [ ] **Step 1: 修改 Tenant.java 枚举字段注解**

打开 `Tenant.java`，找到 `type` 字段（约第 38-41 行），修改为：

```java
// 修改前
@Getter
@Enumerated(EnumType.STRING)
@Column(name = "type", nullable = false, length = 20)
private TenantType type;

// 修改后
@Getter
@EnumConvert(TenantType.class)
@Column(name = "type", nullable = false)
private TenantType type;
```

- [ ] **Step 2: 添加必要的 import**

在文件顶部的 import 区域添加：

```java
import com.cartisan.data.jpa.annotation.EnumConvert;
```

删除不再需要的 import（如果存在）：

```java
import jakarta.persistence.Enumerated;  // 删除此行
```

- [ ] **Step 3: 验证编译**

```bash
cd server && ./gradlew compileJava
```
Expected: 编译成功

- [ ] **Step 4: 运行单元测试（预期会失败）**

```bash
cd server && ./gradlew test --tests "*TenantTest"
```
Expected: 测试可能失败（因为数据库字段尚未迁移）

- [ ] **Step 5: 暂存修改，等待数据库迁移完成后一起提交**

```bash
git add server/src/main/java/com/aieducenter/tenant/domain/aggregate/Tenant.java
```

---

### Task 6: 创建 Flyway 数据库迁移脚本

**文件:**
- 创建: `server/src/main/resources/db/migration/V11__alter_tenant_type_column.sql`

- [ ] **Step 1: 检查现有迁移脚本版本**

```bash
ls -la server/src/main/resources/db/migration/
```
确认最新版本号（可能是 V1__xxx.sql），使用下一个版本号

- [ ] **Step 2: 创建迁移脚本文件**

创建 `server/src/main/resources/db/migration/V11__alter_tenant_type_column.sql`：

```sql
-- ============================================================
-- 修改 tenants.type 字段：VARCHAR → INT
-- 数据迁移：PERSONAL → 1
-- ============================================================

-- 0. 迁移前数据验证
SELECT COUNT(*) as total_count,
       COUNT(CASE WHEN type = 'PERSONAL' THEN 1 END) as personal_count
FROM tenants;

-- 1. 备份现有数据
CREATE TABLE tenants_backup_20260405 AS SELECT * FROM tenants;

-- 2. 添加临时整数字段
ALTER TABLE tenants ADD COLUMN type_int INT;

-- 3. 迁移数据
UPDATE tenants
SET type_int = CASE type
    WHEN 'PERSONAL' THEN 1
    ELSE 1  -- 默认值
END;

-- 4. 设置 NOT NULL 约束
ALTER TABLE tenants ALTER COLUMN type_int SET NOT NULL;

-- 5. 验证数据迁移（确保没有NULL值）
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM tenants WHERE type_int IS NULL) THEN
        RAISE EXCEPTION 'Data migration failed: NULL values found in type_int';
    END IF;
END $$;

-- 6. 删除旧字段，重命名新字段
ALTER TABLE tenants DROP COLUMN type;
ALTER TABLE tenants RENAME COLUMN type_int TO type;

-- 7. 添加注释
COMMENT ON COLUMN tenants.type IS '租户类型：1-个人';
```

- [ ] **Step 3: 运行 Flyway 迁移（需要数据库连接）**

⚠️ **注意**: 如果没有运行中的数据库，此步骤将在集成测试时执行

```bash
cd server && ./gradlew flywayMigrate
```

- [ ] **Step 4: 提交代码和迁移脚本**

```bash
git add server/src/main/java/com/aieducenter/tenant/domain/aggregate/Tenant.java
git add server/src/main/resources/db/migration/V11__alter_tenant_type_column.sql
git commit -m "refactor(tenant): change Tenant.type to use @EnumConvert (Integer storage)

- Replace @Enumerated(EnumType.STRING) with @EnumConvert(TenantType.class)
- Add Flyway migration script V2__alter_tenant_type_column.sql
- Migrate data: VARCHAR 'PERSONAL' → INT 1
- Add backup table before migration"
```

---

### Task 7: 修正 TenantTest 测试命名

**文件:**
- 修改: `server/src/test/java/com/aieducenter/tenant/domain/aggregate/TenantTest.java:12`

- [ ] **Step 1: 修改测试方法名**

打开 `TenantTest.java`，找到测试方法（约第 12 行），修改为：

```java
// 修改前
@Test
void shouldCreateTenant_whenFieldsValid() {
    // When
    Tenant tenant = new Tenant("John's Space", TenantType.PERSONAL, 123L);

    // Then
    assertThat(tenant.getName()).isEqualTo("John's Space");
    assertThat(tenant.getType()).isEqualTo(TenantType.PERSONAL);
    assertThat(tenant.getOwnerId()).isEqualTo(123L);
}

// 修改后
@Test
void given_valid_fields_when_create_tenant_then_success() {
    // Given
    String name = "John's Space";
    TenantType type = TenantType.PERSONAL;
    Long ownerId = 123L;

    // When
    Tenant tenant = new Tenant(name, type, ownerId);

    // Then
    assertThat(tenant.getName()).isEqualTo(name);
    assertThat(tenant.getType()).isEqualTo(type);
    assertThat(tenant.getOwnerId()).isEqualTo(ownerId);
}
```

- [ ] **Step 2: 运行测试**

```bash
cd server && ./gradlew test --tests "*TenantTest"
```
Expected: 测试通过

- [ ] **Step 3: 提交**

```bash
git add server/src/test/java/com/aieducenter/tenant/domain/aggregate/TenantTest.java
git commit -m "refactor(tenant): rename test method to follow given-when-then pattern

- shouldCreateTenant_whenFieldsValid → given_valid_fields_when_create_tenant_then_success
- Improve test structure with explicit Given section"
```

---

## Part 3: 验证与文档

### Task 8: 运行完整测试套件

**目的:** 确保所有重构后测试通过

- [ ] **Step 1: 运行所有单元测试**

```bash
cd server && ./gradlew test
```
Expected: 所有测试通过

- [ ] **Step 2: 运行 ArchUnit 架构测试**

```bash
cd server && ./gradlew test --tests "*ArchitectureTest"
```
Expected: 架构测试通过

- [ ] **Step 3: 检查测试覆盖率**

```bash
cd server && ./gradlew test jacocoTestReport
```
Expected: 覆盖率符合目标（tenant 上下文应保持高覆盖率）

---

### Task 9: 验证枚举序列化/反序列化

**目的:** 确保 @EnumConvert 注解正常工作

**前置条件**: 应用可启动，或使用单元测试验证

- [ ] **Step 1: 方法 A - 单元测试验证（推荐，无需启动应用）**

```bash
cd server && ./gradlew test --tests "*Tenant*"
```

如果测试通过，说明枚举序列化/反序列化正常。

- [ ] **Step 2: 方法 B - 集成测试验证（需要应用启动）**

如果需要手动验证，启动应用后：

```bash
# 1. 获取 Tenant 数据，验证 type 序列化为整数
curl http://localhost:8080/api/tenants/1 2>/dev/null | jq '.type'

# 预期输出：1（整数），而非 "PERSONAL"（字符串）

# 2. 创建 Tenant，验证 type 反序列化
curl -X POST http://localhost:8080/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","type":1,"ownerId":999}' \
  2>/dev/null | jq

# 预期返回成功响应
```

**注意**: 如果 tenant 上下文尚未有对外 API，跳过此步骤，使用方法 A 即可。

- [ ] **Step 3: 验证数据库存储**

```bash
# 连接数据库检查
psql -U postgres -d aieducenter -c "SELECT id, name, type FROM tenants LIMIT 5;"
```
Expected: type 列显示为整数 (1)

---

### Task 10: 清理备份表（可选）

**目的:** 如果一切正常，删除数据库备份表

- [ ] **Step 1: 确认系统运行正常**

等待至少 24 小时，确认：
- tenant 相关功能正常
- 数据读写正常
- 无错误日志

- [ ] **Step 2: 删除备份表**

⚠️ **谨慎操作**: 确认不再需要备份后再执行

```sql
DROP TABLE tenants_backup_20260405;
```

- [ ] **Step 3: 提交清理脚本（如果创建了手动清理脚本）**

```bash
git add db/cleanup-scripts/
git commit -m "chore(tenant): remove backup table after successful migration"
```

---

## 完成标准

✅ 所有任务完成，当：

1. **verification 上下文:**
   - [x] `package-info.java` 存在且包含 `@BoundedContext`
   - [x] 包结构从 `web.controller` 重命名为 `endpoints.controller`
   - [x] `CaptchaCode` 类已删除
   - [x] 所有测试通过

2. **tenant 上下文:**
   - [x] `package-info.java` 存在且包含 `@BoundedContext`
   - [x] `Tenant.type` 使用 `@EnumConvert(TenantType.class)`
   - [x] Flyway 迁移脚本已执行
   - [x] 测试命名符合 given-when-then 规范
   - [x] 所有测试通过

3. **架构验证:**
   - [x] ArchUnit 测试通过
   - [x] 枚举序列化/反序列化正常
   - [x] 数据库存储正确（Integer）

---

## 回滚指南

如果遇到问题需要回滚：

### 代码回滚
```bash
# 回滚到重构前的提交
git revert <commit-range>
# 或
git reset --hard <commit-before-refactoring>
```

### 数据库回滚
```bash
# 方案1: 从备份表恢复
psql -U postgres -d aieducenter -c "TRUNCATE tenants; INSERT INTO tenants SELECT * FROM tenants_backup_20260405;"

# 方案2: 运行回滚脚本（如果创建了）
psql -U postgres -d aieducenter -f db/rollback/V2__rollback_tenant_type.sql
```

---

## 参考文档

- [cartisan-boot 使用手册](../../guide/cartisan-boot-使用手册.md)
- [限界上下文代码编写规范](../../guide/限界上下文代码编写规范.md)
- [设计文档](../specs/2026-04-05-contexts-refactoring-design.md)
