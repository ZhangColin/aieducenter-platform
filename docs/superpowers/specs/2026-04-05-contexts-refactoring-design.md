# verification & tenant 上下文规范重构设计

**日期**: 2026-04-05
**状态**: Draft
**作者**: AI Assistant

## 概述

修复 verification 和 tenant 两个上下文中违反《限界上下文代码编写规范》的问题，确保代码符合 cartisan-boot 框架使用规范。

## 目标

1. 为两个上下文添加 `package-info.java` 标注
2. 修正北向接口层包结构
3. 修正枚举存储方式（符合框架规范）
4. 删除未使用的代码
5. 修正测试命名格式

## 涉及上下文

- `com.aieducenter.verification` - 验证码限界上下文
- `com.aieducenter.tenant` - 租户限界上下文

---

## 一、verification 上下文变更

### 1.1 添加 package-info.java

**文件**: `server/src/main/java/com/aieducenter/verification/package-info.java`

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

### 1.2 重命名包结构

**变更**:
```
verification.web
  → verification.endpoints
```

**影响文件**:
- `server/src/main/java/com/aieducenter/verification/web/VerificationCodeController.java`
  → `server/src/main/java/com/aieducenter/verification/endpoints/VerificationCodeController.java`
- `server/src/main/java/com/aieducenter/verification/web/CaptchaController.java`
  → `server/src/main/java/com/aieducenter/verification/endpoints/CaptchaController.java`
- 对应的测试文件也需要移动并修改 package 声明

### 1.3 删除未使用的类

**删除文件**:
- `server/src/main/java/com/aieducenter/verification/domain/model/CaptchaCode.java`

**原因**: 该类未被任何代码引用，已使用 `CaptchaGenerationService.CaptchaResult` record 替代。

---

## 二、tenant 上下文变更

### 2.1 添加 package-info.java

**文件**: `server/src/main/java/com/aieducenter/tenant/package-info.java`

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

### 2.2 修改 TenantType 枚举存储

**文件**: `server/src/main/java/com/aieducenter/tenant/domain/aggregate/Tenant.java`

**当前代码**:
```java
@Getter
@Enumerated(EnumType.STRING)
@Column(name = "type", nullable = false, length = 20)
private TenantType type;
```

**修改为**:
```java
@Getter
@EnumConvert(TenantType.class)
@Column(name = "type", nullable = false)
private TenantType type;
```

### 2.3 数据库迁移脚本

**文件**: `server/src/main/resources/db/migration/V11__alter_tenant_type_column.sql`

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

### 2.4 修正测试命名

**文件**: `server/src/test/java/com/aieducenter/tenant/domain/aggregate/TenantTest.java`

**当前代码**:
```java
@Test
void shouldCreateTenant_whenFieldsValid() { }
```

**修改为**:
```java
@Test
void given_valid_fields_when_create_tenant_then_success() { }
```

---

## 三、不变量保证

1. **业务逻辑不变**: 仅修正代码组织和注解使用，不改变任何业务行为
2. **API 兼容性**: Controller 路径和响应格式保持不变
3. **数据完整性**: 数据库迁移前备份，迁移后验证

---

## 四、测试验证

### 4.1 单元测试

- `TenantTest` - 测试聚合根创建
- `VerificationCodeAppServiceTest` - 测试应用服务
- `CaptchaAppServiceTest` - 测试图形验证码服务

```bash
cd server && ./gradlew test --tests "*TenantTest"
cd server && ./gradlew test --tests "*VerificationCodeAppServiceTest"
cd server && ./gradlew test --tests "*CaptchaAppServiceTest"
```

### 4.2 集成测试

- 验证 Controller 请求路径正常工作
- 验证枚举序列化/反序列化（JSON ↔ Integer）
- 验证数据库 CRUD 操作

```bash
cd server && ./gradlew test --tests "*VerificationCodeControllerTest"
cd server && ./gradlew test --tests "*CaptchaControllerTest"
```

### 4.3 ArchUnit 测试

- 运行 `ArchitectureTest` 确保架构规则通过

```bash
cd server && ./gradlew test --tests "*ArchitectureTest"
```

---

## 五、执行顺序

1. 创建两个 `package-info.java` 文件
2. 重命名 `verification.web` → `verification.endpoints.controller`
3. 检查并更新所有 import 语句
4. 删除 `CaptchaCode`
5. 修改 `Tenant.type` 字段注解
6. 创建 Flyway 迁移脚本
7. 修正 `TenantTest` 测试命名
8. 运行所有测试验证

---

## 六、回滚方案

### 6.1 数据库回滚

**方案1：完整表恢复（推荐）**
```sql
-- 从备份表恢复
TRUNCATE tenants;
INSERT INTO tenants SELECT * FROM tenants_backup_20260405;
```

**方案2：字段级回滚（如果表迁移失败）**
```sql
-- 添加临时字符串字段
ALTER TABLE tenants ADD COLUMN type_varchar VARCHAR(20);

-- 迁移数据回字符串
UPDATE tenants SET type_varchar = 'PERSONAL' WHERE type = 1;

-- 设置约束并替换字段
ALTER TABLE tenants ALTER COLUMN type_varchar SET NOT NULL;
ALTER TABLE tenants DROP COLUMN type;
ALTER TABLE tenants RENAME COLUMN type_varchar TO type;
```

### 6.2 代码回滚

```bash
# 回滚代码变更
git revert <commit-hash>
# 或直接重置到指定提交
git reset --hard <commit-before-refactoring>
```
