# Feature: OpenAPI 文档配置 — 实施计划

> 版本：v1.0 | 日期：2026-03-16
> Epic：Epic 1 - 项目骨架
> Feature：F01-03

---

## 目标复述

配置 SpringDoc 生成 OpenAPI 3.1 规范文档，为前端自动生成 TypeScript API 客户端做准备。需要：

1. 添加 SpringDoc 依赖到 `build.gradle.kts`
2. 创建 `OpenApiConfig` 配置类，设置 API 元信息和 Bearer Token 认证
3. 验证 Swagger UI 和 OpenAPI JSON 端点可正常访问

---

## 变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `server/build.gradle.kts` | 添加 `springdoc-openapi-starter-webmvc-ui` 依赖 |
| 新增 | `server/src/main/java/com/aieducenter/config/OpenApiConfig.java` | OpenAPI 配置类 |

---

## 核心流程（伪代码）

```
1. 添加依赖
   在 build.gradle.kts 中添加 SpringDoc 依赖

2. 创建配置类
   @Configuration
   class OpenApiConfig {
       @Bean
       OpenAPI customOpenAPI() {
           return OpenAPI()
               .setInfo(Info()
                   .setTitle("海创元智研云平台 API")
                   .setVersion("1.0.0")
                   .setDescription("AI 聚合 SaaS 平台接口"))
               .addSecurityItem(SecurityRequirement().addList("BearerAuth"))
               .setComponents(Components()
                   .addSecuritySchemes("BearerAuth", SecurityScheme()
                       .setType(HTTP)
                       .setScheme("bearer")
                       .setBearerFormat("JWT")))
       }
   }

3. 启动验证
   - 运行 ./gradlew :server:bootRun
   - 访问 http://localhost:8080/swagger-ui.html
   - 访问 http://localhost:8080/v3/api-docs
```

---

## 原子任务清单

### Step 1: 修改 build.gradle.kts，添加依赖

**文件：** `server/build.gradle.kts`

**内容：** 在 `dependencies` 块中添加 SpringDoc 依赖

```kotlin
dependencies {
    // ... 现有依赖

    // SpringDoc OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
}
```

**验证：** 编译通过
```bash
./gradlew :server:compileJava
```

---

### Step 2: 创建 OpenApiConfig 配置类

**文件：** `server/src/main/java/com/aieducenter/config/OpenApiConfig.java`

**内容：**

```java
package com.aieducenter.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("海创元智研云平台 API")
                        .version("1.0.0")
                        .description("AI 聚合 SaaS 平台接口"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .name("BearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 认证，格式：Bearer {token}")));
    }
}
```

**验证：** 编译通过
```bash
./gradlew :server:compileJava
```

---

### Step 3: 启动应用验证端点

**操作：**

1. 启动应用
```bash
./gradlew :server:bootRun
```

2. 验证端点可访问
```bash
# 验证 Swagger UI
curl -I http://localhost:8080/swagger-ui.html
# 预期：HTTP/1.1 200 OK

# 验证 OpenAPI JSON
curl http://localhost:8080/v3/api-docs | jq .
# 预期：返回完整的 OpenAPI JSON
```

3. 验证元信息
```bash
curl -s http://localhost:8080/v3/api-docs | jq '.info'
# 预期输出：
# {
#   "title": "海创元智研云平台 API",
#   "version": "1.0.0",
#   "description": "AI 聚合 SaaS 平台接口"
# }
```

4. 验证安全配置
```bash
curl -s http://localhost:8080/v3/api-docs | jq '.components.securitySchemes'
# 预期输出：
# {
#   "BearerAuth": {
#     "type": "http",
#     "scheme": "bearer",
#     "bearerFormat": "JWT",
#     "description": "JWT 认证，格式：Bearer {token}"
#   }
# }
```

5. 浏览器验证
   - 访问 http://localhost:8080/swagger-ui.html
   - 确认页面正常显示
   - 确认右上角有 "Authorize" 按钮

**验证标准：** 所有端点可访问，返回内容符合预期

---

### Step 4: 编写单元测试（可选）

**文件：** `server/src/test/java/com/aieducenter/config/OpenApiConfigTest.java`

**内容：**

```java
package com.aieducenter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
}
```

**验证：** 测试通过
```bash
./gradlew :server:test --tests OpenApiConfigTest
```

---

## 实施顺序

```
Step 1 → Step 2 → Step 3 → Step 4（可选）
  ↓        ↓        ↓
添加依赖  创建配置  启动验证
```

**核心路径：** Step 1 → Step 2 → Step 3

**可选：** Step 4（单元测试，配置类简单时可省略）

---

## 验收检查清单

完成后逐项检查：

- [ ] `build.gradle.kts` 包含 SpringDoc 依赖
- [ ] `OpenApiConfig.java` 文件存在且编译通过
- [ ] 应用可正常启动
- [ ] `/swagger-ui.html` 可访问，页面正常
- [ ] `/v3/api-docs` 返回正确的 OpenAPI JSON
- [ ] `info.title` = "海创元智研云平台 API"
- [ ] `info.version` = "1.0.0"
- [ ] `components.securitySchemes.BearerAuth` 存在且配置正确
- [ ] `security` 包含 `{"BearerAuth": []}`
- [ ] （可选）单元测试通过
