# Feature: OpenAPI 文档配置 — 测试规格

> 版本：v1.0 | 日期：2026-03-16
> Epic：Epic 1 - 项目骨架
> Feature：F01-03

---

## 测试策略

### 测试类型

| 测试类型 | 工具/方式 | 覆盖内容 |
|----------|-----------|----------|
| **单元测试** | JUnit 5 + AssertJ | OpenAPI Bean 配置正确性 |
| **手动验证** | curl + 浏览器 | 端点可访问性和响应内容 |
| **集成测试** | SpringBootTest | 完整应用上下文加载 |

### 测试范围

**包含：**
- OpenAPI Bean 配置验证
- Info 元信息正确性
- SecurityScheme 配置正确性
- SecurityRequirement 应用

**不包含：**
- Swagger UI 界面自动化测试（手动验证）
- 多 API 分组场景（不在本 Feature 范围）

---

## 测试用例清单

### 单元测试用例

| 用例ID | 测试方法 | 验证内容 | 对应AC |
|--------|----------|----------|--------|
| TC1 | `shouldConfigureOpenAPI` | OpenAPI Bean 不为 null | AC2 |
| TC2 | `shouldHaveCorrectInfo` | title、version、description 正确 | AC2 |
| TC3 | `shouldHaveBearerAuthSecurityScheme` | BearerAuth type、scheme、bearerFormat 正确 | AC4 |
| TC4 | `shouldHaveSecurityRequirement` | security 包含 BearerAuth | AC5 |

### 手动验证用例

| 用例ID | 验证命令 | 预期结果 | 对应AC |
|--------|----------|----------|--------|
| MV1 | `curl -I http://localhost:8080/swagger-ui.html` | HTTP 302 重定向到 index.html | AC3 |
| MV2 | `curl http://localhost:8080/v3/api-docs \| jq '.info'` | 返回正确的 info 对象 | AC2, AC3 |
| MV3 | `curl http://localhost:8080/v3/api-docs \| jq '.components.securitySchemes'` | 返回正确的 BearerAuth 配置 | AC4 |
| MV4 | 浏览器访问 http://localhost:8080/swagger-ui.html | 页面正常显示，有 Authorize 按钮 | AC3 |

---

## 测试实现

### OpenApiConfigTest.java

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class OpenApiConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void shouldConfigureOpenAPI() {
        assertThat(openAPI).isNotNull();
    }

    @Test
    void shouldHaveCorrectInfo() {
        assertThat(openAPI.getInfo().getTitle())
                .isEqualTo("海创元智研云平台 API");
        assertThat(openAPI.getInfo().getVersion())
                .isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription())
                .isEqualTo("AI 聚合 SaaS 平台接口");
    }

    @Test
    void shouldHaveBearerAuthSecurityScheme() {
        SecurityScheme bearerAuth = openAPI.getComponents()
                .getSecuritySchemes()
                .get("BearerAuth");

        assertThat(bearerAuth).isNotNull();
        assertThat(bearerAuth.getType())
                .isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(bearerAuth.getScheme())
                .isEqualTo("bearer");
        assertThat(bearerAuth.getBearerFormat())
                .isEqualTo("JWT");
    }

    @Test
    void shouldHaveSecurityRequirement() {
        assertThat(openAPI.getSecurity()).isNotEmpty();
        assertThat(openAPI.getSecurity().get(0))
                .containsKey("BearerAuth");
    }
}
```

---

## 测试配置

### application-test.yml

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

**说明：** 配置类测试不需要数据源，排除相关自动配置。

---

## 验证命令

### 编译和测试

```bash
# 编译检查
./gradlew compileJava compileTestJava

# 运行测试
./gradlew test

# 全量检查
./gradlew check
```

### 手动端点验证

```bash
# 启动应用
./gradlew bootRun

# 验证端点
curl -I http://localhost:8080/swagger-ui.html
curl -s http://localhost:8080/v3/api-docs | jq '.info'
curl -s http://localhost:8080/v3/api-docs | jq '.components.securitySchemes'
```

---

## 测试执行记录

| 执行时间 | 执行人 | 测试结果 | 备注 |
|----------|--------|----------|------|
| 2026-03-16 | AI (Claude) | ✅ 全部通过 | 4/4 单元测试通过，手动验证通过 |

---

## 代码审查记录

### 审查信息

| 字段 | 值 |
|------|-----|
| 审查时间 | 2026-03-16 |
| 审查模型 | Claude (Self-Review) |
| 审查范围 | Spec (01 + 02) + 代码变更 |
| 审查结论 | 通过 |

### 审查发现

| 优先级 | 问题 | 状态 |
|--------|------|------|
| - | 无问题 | - |

### 审查意见

- ✅ 所有 AC 符合 Spec
- ✅ 代码质量符合 cartisan-boot 规范
- ✅ 测试覆盖充分
- ✅ 无额外或遗漏实现

---

## 完成检查清单

### 代码实现

- [x] `build.gradle.kts` 添加 SpringDoc 依赖
- [x] `OpenApiConfig.java` 配置类创建
- [x] JavaDoc 文档完整
- [x] 包结构符合 Spec

### 测试

- [x] 单元测试编写完成
- [x] 单元测试全部通过
- [x] 手动验证端点可访问
- [x] OpenAPI JSON 内容正确

### 构建验证

- [x] compileJava 通过
- [x] compileTestJava 通过
- [x] test 通过
- [x] check 通过

### 文档

- [x] 01_requirement.md 完成
- [x] 02_interface.md 完成
- [x] 03_implementation.md 完成
- [x] 04_test_spec.md 完成

---

## Feature 状态

**状态：已完成 ✅**

所有验收标准已满足，代码审查通过，测试验证完成。
