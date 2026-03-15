# 团队规则库 (SKILL)

> 定位：团队的"经验手册"——记录所有值得沉淀的踩坑经验和铁律。AI 在开发前会读取它，避免重复犯错。
>
> cartisan-boot 框架层的规则见：[cartisan-boot/docs/skills/SKILL.md](../../cartisan-boot/docs/skills/SKILL.md)

---

## DDD / 领域建模

### 规则 DDD-001：Entity.sameIdentityAs() 需要类型检查

**问题**：由于 Java 泛型类型擦除，接口方法签名中的 `T` 在运行时被擦除为 `Object`，无法直接调用 `other.getId()`。

**正确做法**：
```java
@SuppressWarnings("unchecked")
default boolean sameIdentityAs(T other) {
    if (other == null) return false;
    if (this.getClass() != other.getClass()) return false;
    ID otherId = ((Entity<T, ID>) other).getId();
    return Objects.equals(this.getId(), otherId);
}
```

**记忆口诀**：接口泛型方法里调泛型参数的方法？必须先检查类型再强转。

---

### 规则 DDD-002：ValueObject 的 sameValueAs 可直接委托 equals

**原因**：`equals()` 方法接受 `Object` 类型，不需要处理类型擦除问题。

```java
public interface ValueObject<T> {
    default boolean sameValueAs(T other) {
        if (other == null) return false;
        return this.equals(other);  // ✅ 简洁
    }
}
```

---

### 规则 DDD-003：领域事件应自动生成元数据

**推荐做法**：
- `eventId`：自动生成 UUID
- `occurredAt`：自动设置为 `Instant.now()`
- `aggregateId`：由子类提供并验证非空

```java
public abstract class DomainEvent {
    private final String eventId;
    private final Instant occurredAt;
    private final String aggregateId;

    protected DomainEvent(String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
        this.aggregateId = Objects.requireNonNull(aggregateId, "aggregateId");
    }
}
```

---

## 代码风格

### 规则 STYLE-001：领域接口应包含完整 JavaDoc 和使用示例

**要求**：
- 每个公共接口/类必须有类级别 JavaDoc
- 包含功能描述、使用场景、示例代码
- 示例代码应完整可运行

**模板**：
```java
/**
 * [一句话描述]。
 *
 * <p>[详细描述]。</p>
 *
 * <h3>使用示例</h3>
 *
 * <pre>{@code
 * // 完整可运行的示例
 * }</pre>
 *
 * @since 0.1.0
 */
```

---

### 规则 STYLE-002：JavaDoc 中必须转义 HTML 特殊字符

**问题**：JavaDoc 解析器会将 `<` 和 `>` 解析为 HTML 标签，导致编译失败。

**正确做法**：
```java
/**
 * 分页查询参数。
 * <p>参数自动校验：
 *   <li>page &lt; 1 时修正为 1</li>
 *   <li>size &gt; 100 时修正为 100</li>
 * </p>
 */
```

**常用 HTML 实体**：
| 字符 | 实体 |
|------|------|
| `<` | `&lt;` |
| `>` | `&gt;` |
| `&` | `&amp;` |

---

### 规则 STYLE-003：使用 Record 实现 ValueObject 和 Identity

```java
// ✅ ValueObject
public record Money(BigDecimal amount, String currency) implements ValueObject<Money> {
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}

// ✅ Identity
public record TenantId(Long value) implements Identity<Long> {
    public TenantId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Tenant ID must be positive");
        }
    }
}
```

---

## 测试

### 规则 TEST-001：使用 AssertJ 而非 JUnit 断言

```java
import static org.assertj.core.api.Assertions.assertThat;

// ✅ AssertJ：链式调用，语义清晰
assertThat(actual).isEqualTo(expected);
assertThat(list).hasSize(3).containsExactly("a", "b", "c");

// ❌ JUnit：不支持链式调用
assertEquals(expected, actual);
```

---

### 规则 TEST-002：测试方法命名应遵循 given-when-then 模式

**模板**：
```java
@Test
void given_{条件}_when_{操作}_then_{预期结果}() {
    // Given
    ...

    // When
    ...

    // Then
    ...
}
```

**示例**：
```java
@Test
void givenInsufficientCoins_whenChat_thenThrowsInsufficientBalanceException() {
    // Given
    Tenant tenant = createTenantWithCoins(10);
    ChatRequest request = new ChatRequest("GPT-4", List.of("message"));

    // When & Then
    assertThatThrownBy(() -> chatService.chat(tenant.getId(), request))
        .isInstanceOf(InsufficientBalanceException.class);
}
```

---

### 规则 TEST-003：MockMvc 集成测试需要测试专用 Controller

**问题**：直接在测试中调用 `StpUtil.login()` 后使用 MockMvc，Sa-Token 上下文未初始化。

**正确做法**：创建测试专用 Controller，通过 HTTP 请求触发登录
```java
// ✅ 通过 MockMvc 请求登录，Sa-Token 上下文正确初始化
@RestController
@RequestMapping("/test/auth")
public class TestAuthController {
    @GetMapping("/login/{userId}")
    public ApiResponse<Map<String, String>> login(@PathVariable Long userId) {
        StpUtil.login(userId);
        String token = StpUtil.getTokenValue();
        return ApiResponse.ok(Map.of("token", token));
    }
}

// 测试中先登录获取 token
String token = extractToken(mvc.perform(get("/test/auth/login/100"))
    .andReturn()
    .getResponse()
    .getContentAsString());

// 使用 token 访问受保护端点
mvc.perform(get("/api/conversations").header("satoken", token))
    .andExpect(status().isOk());
```

---

## Spring Data JPA / 数据访问

### 规则 DATA-001：JPA save() 后必须用原始 entity 发布事件

**问题**：`SimpleJpaRepository.save()` 返回的可能是一个新实例，不是原始传入的实体。

```java
@Override
public <S extends T> S save(S entity) {
    S savedEntity = super.save(entity);
    publishDomainEvents(entity);  // ✅ 使用原始 entity
    return savedEntity;
}
```

**记忆口诀**：JPA save 返回值 ≠ 原始参数，后处理必须用原参数。

---

### 规则 DATA-002：@MappedSuperclass 需要添加 @EntityListeners

**问题**：审计字段没有被自动填充。

**正确做法**：
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)  // ✅ 必须添加
public abstract class Auditable {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

---

### 规则 DATA-003：JPQL @Query 查询不受 @SQLRestriction 影响

**问题**：使用 `@Query` 编写 JPQL 时，软删除过滤不生效。

```java
// ✅ JPQL 查询手动添加软删除条件
@Query("SELECT c FROM Conversation c WHERE c.deleted = false AND c.tenantId = :tenantId")
List<Conversation> findActiveByTenantId(@Param("tenantId") Long tenantId);
```

**记忆口诀**：JPQL 查询手动加条件，@SQLRestriction 只管自动生成的 SQL。

---

## Spring Boot / Web

### 规则 WEB-001：Actuator 与全局异常处理器的交互

**场景**：访问未暴露的 Actuator 端点时，期望返回 404，但实际返回 500。

**原因**：cartisan-web 的 `@ControllerAdvice` 全局异常处理器捕获了 Spring 抛出的 `HandlerNotFoundException`，转换为 ApiResponse 格式的 500 响应。

**影响**：不影响核心功能（端点确实未暴露，通过 `/actuator` 可验证），但响应格式与纯 Actuator 环境不同。

**预期行为**：
```bash
# 访问未暴露的端点
curl http://localhost:8080/actuator/info
# 返回: {"code":500,"message":"Internal server error",...}
# 而非标准的 404
```

**后续优化（可选）**：在 cartisan-web 的 GlobalExceptionHandler 中排除 `/actuator/**` 路径：
```java
@ExceptionHandler(HandlerNotFoundException.class)
public ResponseEntity<ApiResponse<Void>> handleNotFound(HandlerNotFoundException e, HttpServletRequest request) {
    // 排除 Actuator 路径，让 Spring 返回标准 404
    if (request.getRequestURI().startsWith("/actuator/")) {
        throw e;  // 重新抛出，让 Spring 处理
    }
    return ApiResponse.error(404, "Not Found");
}
```

---

### 规则 WEB-002：@Validated 必须放在类上触发 @RequestParam 校验

```java
// ✅ 添加 @Validated
@Validated  // 必须有
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    @GetMapping("/search")
    public void search(@RequestParam @Min(1) Integer page) {
        // Spring 会先校验，校验失败抛出 ConstraintViolationException
    }
}
```

---

### 规则 WEB-003：@Component 默认 bean 名称可能与自动配置冲突

**问题**：`@Component` 默认 bean 名称可能与 Spring Boot 自动配置冲突。

```java
// ✅ 显式指定 bean 名称，避免与 Spring Boot 自动配置冲突
@Component("aieducenterRequestContextFilter")
public class RequestContextFilter extends OncePerRequestFilter {
    // ...
}
```

**命名建议**：使用项目前缀，如 `aieducenter{ClassName}` 或 `{context}{ClassName}`。

---

## 断言工具 (Assertions)

### 规则 ASRT-001：异常类型语义决定 HTTP 状态码

| 断言方法 | 异常类型 | 语义 | HTTP |
|---------|---------|------|------|
| `require()` | `DomainException` | 调用者责任 = 业务规则违反 | 4xx |
| `ensure()` | `IllegalStateException` | 实现者责任 = 代码 bug | 500 |
| `requirePresent()` | `DomainException` | 资源不存在 | 404 |

```java
// ✅ 业务规则违反
public void deductCoins(int amount) {
    require(this.balance >= amount,
        new InsufficientBalanceException("余额不足"));
}

// ✅ 代码 bug
public void addItem(OrderItem item) {
    this.items.add(item);
    ensure(this.items.contains(item), "item should be present after add");
}
```

---

## 计费相关

### 规则 BILL-001：金额计算用 BigDecimal，禁止 double

```java
// ✅ 正确
public record Money(BigDecimal amount, String currency) {
    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount), "CNY");
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}

// ❌ 错误：精度丢失
public double add(double a, double b) {
    return a + b;  // 0.1 + 0.2 可能 ≠ 0.3
}
```

---

### 规则 BILL-002：虚拟币预扣需校验余额和配额

**计费流程**：
```java
// 1. 预扣
CoinDeduction deduction = coinService.preDeduct(tenantId, userId, estimatedCoins);

// 2. 调用 AI
try {
    ChatResponse response = aiGateway.chat(request);

    // 3. 实扣（多退少补）
    coinService.actualDeduct(deduction.getId(), response.actualTokens());
} catch (Exception e) {
    // 4. 失败回滚
    coinService.rollbackDeduction(deduction.getId());
    throw e;
}
```

---

## 多租户相关

### 规则 TENANT-001：租户上下文用 ScopedValue 传递

```java
// 使用 cartisan-security 的 TenantContext
Long tenantId = TenantContext.currentTenantId();

// 在 Filter 中设置
TenantContext.set(tenantId);
try {
    chain.doFilter(request, response);
} finally {
    TenantContext.clear();
}
```

---

### 规则 TENANT-002：所有数据查询必须带租户过滤

```java
// ✅ Repository 层自动过滤
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c WHERE c.tenantId = :tenantId AND c.deleted = false")
    List<Conversation> findByTenantId(@Param("tenantId") Long tenantId);
}

// ✅ 也可以用 @SQLRestriction
@Entity(name = "conversation")
@SQLRestriction("tenant_id = CURRENT_TENANT_ID() AND deleted = false")
public class Conversation { ... }
```

---

## AI 调用相关

### 规则 AI-001：流式输出需正确处理 SSE 断开

```java
// ✅ 正确处理客户端断开
public Flux<String> chatStream(ChatRequest request) {
    return aiProvider.chatStream(request)
        .doOnCancel(() -> {
            // 记录取消，可能需要部分计费
            log.info("Client disconnected during chat");
        })
        .doOnError(e -> {
            // 记录错误，回滚预扣
            log.error("Chat error", e);
        })
        .doOnComplete(() -> {
            // 流结束，触发实扣
            coinService.actualDeduct(deductionId, actualTokens);
        });
}
```

---

## 前端 / Monorepo

### 规则 FRONT-001：Next.js 配置 transpilePackages 转译 workspace 包

**问题**：从 workspace 包导入源码（.ts）时，Next.js 默认不会转译，导致运行时错误。

**正确做法**：
```ts
// next.config.ts
const nextConfig: NextConfig = {
  transpilePackages: ['@aieducenter/ui', '@aieducenter/api-client', '@aieducenter/shared'],
}
```

**记忆口诀**：workspace 包导入源码？transpilePackages 必配。

---

### 规则 FRONT-002：TypeScript jsx 配置用 preserve

**问题**：Next.js 15 + React 19 应使用 `jsx: "preserve"`，而非 `react`。

**正确做法**：
```json
{
  "compilerOptions": {
    "jsx": "preserve"  // 让 Next.js 处理 JSX
  }
}
```

---

### 规则 FRONT-003：isolatedModules 必须启用

**问题**：Next.js 要求 `isolatedModules: true`，否则可能构建失败。

**正确做法**：
```json
{
  "compilerOptions": {
    "isolatedModules": true  // tsconfig.base.json
  }
}
```

---

### 规则 FRONT-004：workspace:* 协议用于开发期本地引用

**正确做法**：
```json
{
  "dependencies": {
    "@aieducenter/ui": "workspace:*"  // 开发期自动链接
  }
}
```

**注意**：发布时 pnpm 自动替换为实际版本号。

---

### 规则 FRONT-005：共享包空壳占位，延迟引入依赖

**原则**：F01-04 仅创建包结构，shadcn/ui 等 React peerDependencies 留到 F01-06 添加。

```json
// F01-04 阶段
{
  "name": "@aieducenter/ui",
  "exports": { ".": "./src/index.ts" }
  // ❌ 不添加 React peerDependencies

// F01-06 阶段
{
  "peerDependencies": {
    "react": "^19.0.0",
    "react-dom": "^19.0.0"
  }
}
```

---

## 踩坑记录

### PIT-001 (2026-03-15)：MessageFormat 参数不足时不会抛异常

**场景**：`MessageFormat.format("Error {0} at {1}", "onlyOne")` 不会抛异常，而是保留未替换的占位符。

**实际行为**：返回 `"Error onlyOne at {1}"`。

---

### PIT-002 (2026-03-15)：@Retention(RUNTIME) 是注解可被反射读取的前提

**正确做法**：
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)  // 必须有
public @interface Aggregate {
}
```

---

### PIT-003 (2026-03-15)：枚举完整性测试应使用精确匹配

```java
// ✅ 精确匹配，防止新增值
@Test
void tenantRole_shouldHaveExactlyFourValues() {
    assertThat(TenantRole.values())
        .hasSize(4)
        .containsExactlyInAnyOrder(
            TenantRole.OWNER,
            TenantRole.ADMIN,
            TenantRole.MEMBER,
            TenantRole.GUEST
        );
}
```

---

### PIT-004 (2026-03-15)：BigDecimal 构造不要用 double

```java
// ❌ 错误：0.1 无法精确表示
new BigDecimal(0.1)  // 实际是 0.1000000000000000055511151231257827021181583404541015625

// ✅ 正确：先转字符串
new BigDecimal("0.1")  // 或 BigDecimal.valueOf(0.1)
```
