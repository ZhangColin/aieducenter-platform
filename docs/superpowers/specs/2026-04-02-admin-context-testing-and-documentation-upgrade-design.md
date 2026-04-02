# Admin 上下文生产级测试补充与规范文档升级设计文档

**版本**: v1.0
**日期**: 2026-04-02
**作者**: Claude & User
**状态**: Design Approved

---

## 一、项目背景

### 1.1 当前状态

Admin 上下文已经实现了高质量的 DDD 六边形架构，是项目的**黄金模板**：

- ✅ 领域层零外部依赖
- ✅ 正确使用 cartisan-boot 框架组件
- ✅ 编码规范：Java Record DTO、构造函数注入
- ✅ 基础测试覆盖完整

### 1.2 发现的缺口

通过全面代码审查，发现以下需要改进的地方：

**测试缺口**：
- 缺少并发测试（同时创建管理员、角色分配冲突）
- 缺少菜单树形结构测试（层级关系、排序）
- 缺少边界值测试（密码强度、用户名长度）
- 缺少性能测试（查询响应时间、权限检查）
- 缺少压力测试（高并发场景）

**文档缺口**：
- 规范文档第 84 行存在反模式示例（直接在聚合根使用 BCryptPasswordEncoder）
- 缺少"领域层解耦规范"关键章节
- 测试规范过于简单（仅 30 行），缺少详细指导

### 1.3 项目目标

1. **建立生产级测试套件**：补充完整的测试场景，确保代码质量
2. **修正规范文档**：删除反模式示例，添加关键章节
3. **持续改进机制**：建立可复用的测试模板和最佳实践

---

## 二、设计方案

### 2.1 测试策略设计

#### 2.1.1 并发测试套件

**测试场景**：

1. **并发创建管理员（用户名唯一性冲突）**
   ```java
   @Test
   @Execution(CONCURRENT)
   void given_concurrent_creation_when_duplicate_username_then_only_one_success() {
       int threadCount = 10;
       String username = "admin001";

       // 10 个线程同时创建同名用户
       // 预期：只有 1 个成功，9 个失败
   }
   ```

2. **并发分配角色**
   ```java
   @Test
   void given_concurrent_role_assignment_when_same_admin_then_consistent_state() {
       // 多个线程同时给同一管理员分配角色
       // 验证最终状态一致性
   }
   ```

3. **并发删除（软删除）**
   ```java
   @Test
   void given_concurrent_deletion_when_same_admin_then_one_success() {
       // 多个线程同时删除同一管理员
   }
   ```

**技术实现**：
- JUnit 5 `@Execution(CONCURRENT)` 注解
- `CountDownLatch` 控制并发时序
- `@Transactional` 确保测试数据回滚

#### 2.1.2 菜单树形结构测试

**测试场景**：

1. **构建 3 层菜单树**
   ```java
   @Test
   void given_menu_hierarchy_when_query_tree_then_return_correct_structure() {
       // 系统管理 (1)
       //   ├── 用户管理 (2)
       //   │   └── 用户列表 (3)
       //   └── 角色管理 (4)
       // 验证层级关系
   }
   ```

2. **菜单排序**
   ```java
   @Test
   void given_menus_with_sort_order_when_query_tree_then_sorted() {
       // 验证同级菜单按 sortOrder 排序
   }
   ```

3. **删除父菜单**
   ```java
   @Test
   void given_parent_menu_with_children_when_delete_then_fail_or_cascade() {
       // 验证业务规则：有子菜单时不能删除
   }
   ```

#### 2.1.3 边界值测试套件

**测试矩阵**：

| 场景 | 最小值 | 最小值-1 | 最大值 | 最大值+1 | 中间值 | 特殊字符 |
|-----|-------|----------|-------|----------|--------|---------|
| 用户名 | 3字符（abc）✅ | 2字符（ab）❌ | 20字符✅ | 21字符❌ | 10字符✅ | a_b, a-b✅ |
| 密码长度 | 8字符✅ | 7字符❌ | 20字符✅ | 21字符❌ | 12字符✅ | - |
| 密码强度 | a1aaaaaa✅ | aa❌ | Aa1aaaaa✅ | - | - | 特殊字符✅ |

**实现方式**：
```java
@ParameterizedTest
@CsvSource({
    "abc, true",      // 最小有效
    "ab, false",      // 最小无效
    "12345678901234567890, true",  // 最大有效
    "123456789012345678901, false" // 最大无效
})
void given_username_when_validate_then_expected(String username, boolean valid) {
    // 测试实现
}
```

#### 2.1.4 权限继承测试

**测试场景**：

1. **角色权限继承到用户**
   ```java
   @Test
   void given_role_with_permissions_when_assign_to_user_then_user_inherits() {
       // 角色有权限：admin:user:read, admin:user:write
       // 分配给用户后，用户应拥有相同权限
   }
   ```

2. **多角色权限合并**
   ```java
   @Test
   void given_user_with_multiple_roles_when_get_permissions_then_merged() {
       // 角色1：user:read
       // 角色2：user:write
       // 用户应有 user:read + user:write
   }
   ```

3. **权限冲突处理**
   ```java
   @Test
   void given_duplicate_permissions_in_roles_when_merge_then_deduplicated() {
       // 验证权限去重
   }
   ```

#### 2.1.5 性能测试

**测试目标和场景**：

| 场景 | 性能目标 | 测试方法 |
|-----|---------|---------|
| 分页查询 | 10000 条记录，< 2s | `@Timeout(2, TimeUnit.SECONDS)` |
| 权限检查 | 10 个角色，< 1s | `@Timeout(1, TimeUnit.SECONDS)` |
| 菜单树构建 | 100 个菜单，< 500ms | `@Timeout(500, TimeUnit.MILLISECONDS)` |

**实现示例**：
```java
@Test
@Timeout(value = 2, unit = TimeUnit.SECONDS)
void given_10000_users_when_query_by_page_then_fast() {
    // 分页查询 10000 条记录，应在 2 秒内完成
}

@RepeatedTest(10)
void performance_test_should_be_stable() {
    // 重复测试 10 次，验证性能稳定性
}
```

#### 2.1.6 压力测试

**测试场景**：

1. **并发登录压力测试**
   ```java
   @Test
   void given_100_concurrent_logins_when_authenticate_then_all_success() {
       // 100 个线程同时登录，验证成功率 > 95%
   }
   ```

2. **数据库连接池压力**
   ```java
   @Test
   void given_high_concurrent_requests_when_crud_then_no_connection_leak() {
       // 高并发 CRUD 操作，验证无连接泄漏
   }
   ```

### 2.2 规范文档重写设计（v2.0）

#### 2.2.1 文档结构调整

**新增章节**：
- 2.7 领域层解耦规范（新增）
- 6.3 并发测试规范（新增）
- 6.4 边界值测试方法（新增）
- 6.5 性能测试指南（新增）
- 6.6 测试覆盖率目标（新增）

**修正章节**：
- 2.1 聚合根（修正第 84 行反模式示例）
- 第六章测试规范（完全重写）

#### 2.2.2 领域层解耦规范（2.7 节）

**核心原则**：
> 领域层必须保持零外部依赖。所有外部框架、库、基础设施都通过端口接口解耦。

**禁止事项**：
```java
// ❌ 禁止：领域层直接依赖 Spring Framework
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

// ❌ 禁止：领域层直接依赖 Redis 客户端
import redis.clients.jedis.Jedis;
private Jedis jedis = new Jedis("localhost");

// ❌ 禁止：领域层直接依赖 HTTP 客户端
import org.springframework.web.client.RestTemplate;
private RestTemplate restTemplate = new RestTemplate();
```

**正确做法**：
```java
// ✅ 正确：通过端口接口解耦
@DomainService
public class PasswordEncoderService {
    private final PasswordEncoderPort encoder;
    // 领域服务封装南向端口
}

// ✅ 正确：端口接口定义在 domain/port
public interface PasswordEncoderPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}

// ✅ 正确：适配器实现在 infrastructure
@Adapter(PortType.CLIENT)
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {
    // 基础设施层实现
}
```

**识别需要解耦的依赖检查清单**：
- [ ] Spring Security（密码编码、认证）
- [ ] Spring Data（数据库操作）
- [ ] Redis（缓存、消息队列）
- [ ] HTTP 客户端（外部服务调用）
- [ ] 消息队列（Kafka、RabbitMQ）
- [ ] 文件存储（OSS、S3）
- [ ] 邮件/短信发送
- [ ] 第三方支付

#### 2.2.3 测试规范扩展（第六章）

**并发测试规范（6.3）**：
- 测试场景覆盖（唯一性约束、状态一致性、软删除）
- 技术要点（`@Execution(CONCURRENT)`、`CountDownLatch`、`@Transactional`）

**边界值测试方法（6.4）**：
- 等价类划分方法
- 边界值组合测试
- 边界值检查清单

**性能测试指南（6.5）**：
- 性能目标定义
- 实现方式（`@Timeout`、`@RepeatedTest`）
- 基准测试方法

**测试覆盖率目标（6.6）**：

| 层级 | 覆盖率目标 | 说明 |
|-----|-----------|------|
| 领域层 | 95%+ | 核心业务逻辑，必须完整覆盖 |
| 应用层 | 90%+ | 业务编排逻辑 |
| 基础设施层 | 80%+ | 适配器、仓储实现 |
| 控制器层 | 70%+ | REST API 端点 |

**覆盖率检查命令**：
```bash
# 生成覆盖率报告
./gradlew jacocoTestReport

# 检查覆盖率阈值
./gradlew jacocoTestCoverageVerification
```

### 2.3 测试文件结构

```
server/src/test/java/com/aieducenter/admin/
├── domain/
│   ├── aggregate/
│   │   ├── AdminUserTest.java (已存在)
│   │   ├── AdminRoleTest.java (已存在)
│   │   └── AdminMenuTest.java (已存在)
│   └── service/
│       └── PasswordEncoderServiceTest.java (已存在)
├── application/
│   ├── AdminUserManagementAppServiceTest.java (已存在)
│   ├── AdminUserAuthAppServiceTest.java (已存在)
│   └── RoleManagementAppServiceTest.java (已存在)
├── concurrency/ ⭐ 新增包
│   ├── AdminUserConcurrencyTest.java
│   └── RoleAssignmentConcurrencyTest.java
├── performance/ ⭐ 新增包
│   ├── AdminPerformanceTest.java
│   └── AdminStressTest.java
├── boundary/ ⭐ 新增包
│   ├── AdminUserBoundaryTest.java
│   └── MenuBoundaryTest.java
└── integration/
    ├── AdminMenuTreeTest.java ⭐ 新增
    └── AdminControllerTest.java (已存在)
```

---

## 三、实施计划

### 3.1 任务分解

**阶段 1：测试补充（17 小时）**

| 任务 | 测试文件 | 预估时间 |
|-----|---------|---------|
| 并发测试 | `AdminUserConcurrencyTest.java` | 4 小时 |
| 菜单树测试 | `AdminMenuTreeTest.java` | 3 小时 |
| 边界值测试 | `AdminUserBoundaryTest.java` | 3 小时 |
| 权限继承测试 | `AdminRolePermissionTest.java`（扩展） | 2 小时 |
| 性能测试 | `AdminPerformanceTest.java` | 3 小时 |
| 压力测试 | `AdminStressTest.java` | 2 小时 |

**阶段 2：规范文档重写（9 小时）**

| 任务 | 文档章节 | 预估时间 |
|-----|---------|---------|
| 添加 2.7 领域层解耦规范 | 新增章节 | 2 小时 |
| 修正 2.1 聚合根反模式示例 | 修正示例 | 1 小时 |
| 重写第六章测试规范 | 完全重写 | 4 小时 |
| 扩展第十一章质量检查清单 | 新增测试项 | 1 小时 |
| 版本更新和文档审查 | v2.0 发布 | 1 小时 |

**阶段 3：验证和集成（3.5 小时）**

| 任务 | 说明 | 预估时间 |
|-----|-----|---------|
| 运行所有测试 | 确保通过 | 1 小时 |
| 生成覆盖率报告 | 验证达到目标 | 1 小时 |
| ArchUnit 规则验证 | 架构合规性 | 0.5 小时 |
| 文档交叉验证 | 确保代码与文档一致 | 1 小时 |

**总计预估时间：29.5 小时（约 4 天）**

### 3.2 实施顺序

**Day 1：核心测试补充**
1. 并发测试（最复杂，优先处理）
2. 边界值测试（中等复杂度）

**Day 2：业务测试补充**
1. 菜单树形结构测试
2. 权限继承测试

**Day 3：性能和压力测试**
1. 性能测试
2. 压力测试

**Day 4：规范文档重写**
1. 添加领域层解耦规范
2. 重写测试规范章节
3. 修正反模式示例
4. 验证和集成

### 3.3 技术栈

**测试框架**（已具备）：
- JUnit 5（并发测试支持）
- AssertJ（断言）
- Mockito（Mock）
- Spring Boot Test（`@Transactional` 回滚）
- Jacoco（覆盖率统计）

**无需新增依赖**。

### 3.4 风险和缓解措施

| 风险 | 影响 | 缓解措施 |
|-----|------|---------|
| 并发测试不稳定 | CI 失败 | 使用 `@Transactional` 确保回滚，固定随机种子 |
| 性能测试环境差异 | 测试结果不一致 | 设置合理的超时阈值，使用相对性能指标 |
| 文档重写遗漏内容 | 规范不完整 | 使用 check-list 逐项核对 |
| 时间预估不足 | 延期完成 | 优先完成核心测试（并发、边界值），性能测试可后续补充 |

---

## 四、质量保证

### 4.1 验收标准

**测试验收**：
- [ ] 所有新增测试通过（100%）
- [ ] 测试覆盖率达到目标（领域层 95%+，应用层 90%+）
- [ ] 并发测试稳定性 > 95%（100 次运行）
- [ ] 性能测试超时次数为 0

**文档验收**：
- [ ] 规范文档更新到 v2.0
- [ ] 所有反模式示例已修正
- [ ] 新增章节完整且清晰
- [ ] 代码示例与实际代码一致

**架构验收**：
- [ ] ArchUnit 规则全部通过
- [ ] 无领域层违规依赖
- [ ] 代码审查通过

### 4.2 质量门禁

**Pre-commit 检查**：
```bash
# 1. 代码编译
./gradlew compileJava

# 2. 所有测试通过
./gradlew test

# 3. ArchUnit 架构规则验证
./gradlew test --tests ArchitectureTest

# 4. 覆盖率检查
./gradlew jacocoTestCoverageVerification
```

**CI/CD 集成**：
- 所有测试必须通过才能合并
- 覆盖率低于目标时构建失败
- ArchUnit 规则违规时构建失败

### 4.3 测试维护策略

**定期审查**：
- 每月审查测试覆盖率报告
- 每季度审查并发测试稳定性
- 每半年更新性能基准值

**测试去重**：
- 使用 `@Tag("slow")` 标记慢速测试
- CI 快速通道：跳过性能测试
- 本地开发：运行所有测试

---

## 五、持续改进

### 5.1 后续行动计划

**短期（1-3 个月）**：
- [x] 完成 Admin 上下文测试补充
- [x] 更新规范文档到 v2.0
- [ ] 将 Admin 模式应用到 Account 上下文
- [ ] 建立 ArchUnit 规则模板库

**中期（3-6 个月）**：
- [ ] 所有上下文达到测试覆盖率目标
- [ ] 建立性能基准测试套件
- [ ] 创建自动化文档生成工具
- [ ] 建立代码审查检查清单

**长期（6-12 个月）**：
- [ ] 建立上下文迁移指南
- [ ] 创建自动化架构合规检查工具
- [ ] 建立技术债务追踪机制
- [ ] 形成团队技术文化

### 5.2 成功指标

**质量指标**：
- 测试覆盖率：领域层 ≥ 95%，应用层 ≥ 90%
- ArchUnit 违规：0
- 生产 Bug 率：下降 50%

**效率指标**：
- 新功能开发时间：减少 20%（因为有完善的测试）
- 代码审查时间：减少 30%（因为规范明确）
- 回归问题：减少 60%（因为有完善的测试）

**知识指标**：
- 规范文档引用率：100%（所有 PR 都参考规范）
- 上下文一致性：95%（所有上下文遵循相同模式）
- 新人上手时间：减少 40%（因为有完整的规范和示例）

---

## 六、关键交付物

1. **代码交付物**：
   - 6 个新测试文件（并发、性能、边界值等）
   - 扩展现有测试（权限继承）
   - 测试覆盖率提升到目标值

2. **文档交付物**：
   - `限界上下文代码编写规范.md v2.0`
   - 新增 3 个章节（2.7, 6.3-6.6）
   - 修正反模式示例

3. **流程交付物**：
   - 质量门禁标准
   - 测试维护策略
   - 持续改进计划

---

## 七、核心价值

### 7.1 技术价值
- 建立生产级测试套件，确保代码质量
- 修正规范文档中的反模式，防止错误传播
- 建立可复用的测试模板和最佳实践

### 7.2 流程价值
- 让 Admin 上下文成为真正的"黄金模板"
- 为其他上下文提供清晰的迁移路径
- 建立持续改进的机制

### 7.3 团队价值
- 统一团队的编码和测试标准
- 降低代码审查成本
- 加速新人上手

---

**文档结束**
