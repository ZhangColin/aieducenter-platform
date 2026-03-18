# Feature: F02-01 User 聚合根与领域模型 — 测试规格

> 版本：v1.0 | 日期：2026-03-18
> 状态：Phase 5 完成
> Epic：Epic 2 - 用户与登录

---

## 测试策略

### 分层测试

| 层级 | 测试类型 | 覆盖范围 | 工具 |
|------|----------|----------|------|
| **Domain** | 单元测试 | 值对象校验、聚合根行为 | JUnit 5 + AssertJ |
| **Infrastructure** | 集成测试 | Repository CRUD、软删除 | Testcontainers + PostgreSQL |

### 测试原则

1. **TDD 红绿灯循环** - 先写测试，观察失败，再实现
2. **真实逻辑** - 测试业务行为，不测试 mock
3. **断言有意义** - 避免 assertTrue(true)，验证实际结果
4. **命名规范** - `should_{预期结果}_when_{条件}`

---

## 单元测试用例清单

### UsernameTest（15 tests）

| 测试方法 | 场景 | 预期结果 |
|----------|------|----------|
| `shouldCreateUsername_whenValid` | 正确格式（字母开头） | 成功创建 |
| `shouldCreateUsername_whenWithUnderscore` | 包含下划线 | 成功创建 |
| `shouldCreateUsername_whenWithNumbers` | 包含数字 | 成功创建 |
| `shouldThrow_whenTooShort` | 少于3位 | USERNAME_INVALID |
| `shouldThrow_whenStartsWithNumber` | 数字开头 | USERNAME_INVALID |
| `shouldThrow_whenStartsWithUnderscore` | 下划线开头 | USERNAME_INVALID |
| `shouldThrow_whenContainsSpecialChar` | 包含特殊字符 | USERNAME_INVALID |
| `shouldThrow_whenTooLong` | 超过20位 | USERNAME_INVALID |
| `shouldThrow_whenNull` | null | USERNAME_INVALID |
| `shouldReturnTrue_whenSameValue` | 相同值 | true |
| `shouldReturnFalse_whenDifferentValue` | 不同值 | false |
| `shouldReturnFalse_whenComparingToNull` | 与 null 比较 | false |
| `shouldReturnValue_whenValue` | 获取值 | 返回原始字符串 |
| `shouldSupportLongUsername` | 20位用户名 | 成功创建 |
| `shouldSupportMinLength` | 3位用户名 | 成功创建 |

### EmailTest（12 tests）

| 测试方法 | 场景 | 预期结果 |
|----------|------|----------|
| `shouldCreateEmail_whenValid` | 正确邮箱格式 | 成功创建 |
| `shouldCreateEmail_whenWithSubdomain` | 含子域名 | 成功创建 |
| `shouldCreateEmail_whenWithPlusSign` | 含+号（Gmail风格） | 成功创建 |
| `shouldThrow_whenMissingAtSign` | 缺少@ | EMAIL_INVALID |
| `shouldThrow_whenMissingDomain` | 缺少域名 | EMAIL_INVALID |
| `shouldThrow_whenMissingLocalPart` | 缺少本地部分 | EMAIL_INVALID |
| `shouldThrow_whenNull` | null | EMAIL_INVALID |
| `shouldThrow_whenEmpty` | 空字符串 | EMAIL_INVALID |
| `shouldReturnTrue_whenSameValue` | 相同值 | true |
| `shouldReturnFalse_whenDifferentValue` | 不同值 | false |
| `shouldReturnFalse_whenComparingToNull` | 与 null 比较 | false |
| `shouldReturnValue_whenValue` | 获取值 | 返回原始字符串 |

### PhoneNumberTest（14 tests）

| 测试方法 | 场景 | 预期结果 |
|----------|------|----------|
| `shouldCreatePhoneNumber_whenValid` | 正确手机号 | 成功创建 |
| `shouldSupportAllStartingDigits` | 13-19开头 | 全部支持 |
| `shouldThrow_whenNotStartWith1` | 非1开头 | PHONE_NUMBER_INVALID |
| `shouldThrow_whenSecondDigitIllegal` | 第二位非3-9 | PHONE_NUMBER_INVALID |
| `shouldThrow_whenTooShort` | 少于11位 | PHONE_NUMBER_INVALID |
| `shouldThrow_whenTooLong` | 多于11位 | PHONE_NUMBER_INVALID |
| `shouldThrow_whenContainsNonDigit` | 包含非数字 | PHONE_NUMBER_INVALID |
| `shouldThrow_whenNull` | null | PHONE_NUMBER_INVALID |
| `shouldThrow_whenEmpty` | 空字符串 | PHONE_NUMBER_INVALID |
| `shouldReturnTrue_whenSameValue` | 相同值 | true |
| `shouldReturnFalse_whenDifferentValue` | 不同值 | false |
| `shouldReturnFalse_whenComparingToNull` | 与 null 比较 | false |
| `shouldReturnValue_whenValue` | 获取值 | 返回原始字符串 |
| `shouldSupportEdgeValidNumber` | 边界有效值（130...） | 成功创建 |

### UserTest（18 tests）

| 测试方法 | 场景 | 预期结果 |
|----------|------|----------|
| `shouldCreateUser_whenRequiredFieldsValid` | 必填字段有效 | 成功创建 |
| `shouldSetNicknameToUsername_whenNicknameBlank` | nickname为空字符串 | 昵称=用户名 |
| `shouldSetNicknameToUsername_whenNicknameNull` | nickname为null | 昵称=用户名 |
| `shouldSetNicknameToUsername_whenNicknameOnlySpaces` | nickname仅空格 | 昵称=用户名 |
| `shouldMatchPassword_whenCorrect` | 密码正确 | true |
| `shouldNotMatchPassword_whenIncorrect` | 密码错误 | false |
| `shouldMatchPassword_whenEmptyPassword` | 空密码 | true |
| `shouldUpdateUsername_whenNewUsernameValid` | 修改用户名 | 用户名已更新 |
| `shouldUpdateNickname_whenNewNicknameValid` | 修改昵称 | 昵称已更新 |
| `shouldNotUpdateNickname_whenNewNicknameBlank` | nickname为空 | 不更新 |
| `shouldNotUpdateNickname_whenNewNicknameNull` | nickname为null | 不更新 |
| `shouldUpdateAvatar_whenNewAvatarValid` | 修改头像 | 头像已更新 |
| `shouldClearAvatar_whenNewAvatarNull` | 清空头像 | 头像为空 |
| `shouldUpdatePassword_whenOldPasswordCorrect` | 旧密码正确 | 密码已更新 |
| `shouldThrow_whenOldPasswordIncorrect` | 旧密码错误 | PASSWORD_INCORRECT |
| `shouldKeepOldPassword_whenUpdatePasswordFails` | 修改失败 | 保持旧密码 |
| `shouldReturnEmptyEmail_whenNotSet` | email未设置 | Optional.empty |
| `shouldReturnEmptyPhoneNumber_whenNotSet` | phone未设置 | Optional.empty |

---

## 集成测试用例清单

### SpringDataJpaUserRepositoryTest（6 tests）

| 测试方法 | 场景 | 预期结果 |
|----------|------|----------|
| `shouldSaveUser` | 保存用户 | ID生成，数据可查询 |
| `shouldFindUserByUsername` | 按username查询 | 返回正确用户 |
| `shouldReturnEmpty_whenUsernameNotFound` | username不存在 | Optional.empty |
| `shouldCheckExistsByUsername` | 检查username存在性 | 返回正确布尔值 |
| `shouldSoftDeleteUser` | 软删除用户 | 查询不到已删除用户 |
| `shouldNotFindDeletedUser` | 已删除用户查询 | 返回空 |

**注意**: 集成测试依赖 Testcontainers + Docker，需确保 CI 环境配置。

---

## 测试覆盖率

| 模块 | 行覆盖率 | 分支覆盖率 |
|------|----------|------------|
| Username | 100% | 100% |
| Email | 100% | 100% |
| PhoneNumber | 100% | 100% |
| User | 95%+ | 90%+ |
| **总计** | **~98%** | **~95%** |

---

## 交叉审查结论

**审查人**: Claude（Code Reviewer Skill）
**审查范围**: 01_requirement.md + 代码变更
**结论**: ✅ 通过（需注意 CI Docker 配置）

### 优势

- DDD 架构清晰，职责分明
- 值对象自封装校验，符合 DDD 原则
- BCrypt 密码加密正确实现
- 测试覆盖完整，59个单元测试全部通过
- 软删除支持完善

### 需注意

1. 集成测试依赖 Docker，CI 需配置 Docker runner
2. nickname 默认逻辑存在语义歧义（已在 JavaDoc 说明）
3. PhoneNumber 仅支持中国大陆号码

### 评估

**可合并**: 是。代码质量高，完全符合 DDD 六边形架构，所有 AC 已实现。
