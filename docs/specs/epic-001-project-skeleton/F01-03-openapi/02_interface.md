# Feature: OpenAPI 文档配置 — 接口契约

> 版本：v1.0 | 日期：2026-03-16
> Epic：Epic 1 - 项目骨架
> Feature：F01-03

---

## 接口定义

本 Feature 为纯配置型，不定义新的业务 API 端点。配置完成后，SpringDoc 自动提供以下端点：

### GET /swagger-ui.html
**描述：** Swagger UI 文档界面（重定向到 index.html）
**鉴权：** 无
**Response：** HTML 页面

### GET /swagger-ui/index.html
**描述：** Swagger UI 文档界面主页面
**鉴权：** 无
**Response：** HTML 页面

### GET /v3/api-docs
**描述：** OpenAPI 3.1 规范 JSON
**鉴权：** 无
**Response (成功)：**
```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "海创元智研云平台 API",
    "version": "1.0.0",
    "description": "AI 聚合 SaaS 平台接口"
  },
  "security": [
    {
      "BearerAuth": []
    }
  ],
  "components": {
    "securitySchemes": {
      "BearerAuth": {
        "type": "http",
        "scheme": "bearer",
        "bearerFormat": "JWT",
        "description": "JWT 认证，格式：Bearer {token}"
      }
    }
  }
}
```

### GET /v3/api-docs/swagger-config
**描述：** Swagger UI 配置
**鉴权：** 无
**Response：** JSON 配置对象

---

## 领域接口描述（伪代码）

### 配置类：OpenApiConfig

**职责：** 提供 OpenAPI Bean，SpringDoc 自动读取并生成文档

**方法签名：**
```java
@Bean
public OpenAPI customOpenAPI() {
    // 返回配置好的 OpenAPI 对象
}
```

**配置内容：**
1. Info 元信息
   - title: "海创元智研云平台 API"
   - version: "1.0.0"
   - description: "AI 聚合 SaaS 平台接口"

2. 安全要求（Security Requirement）
   - 全局应用 BearerAuth

3. 安全方案（Security Scheme）
   - name: "BearerAuth"
   - type: HTTP
   - scheme: bearer
   - bearerFormat: JWT
   - description: "JWT 认证，格式：Bearer {token}"

---

## 数据结构

### OpenAPI JSON 结构（伪代码）

```
OpenAPI {
    openapi: "3.1.0"
    info: Info {
        title: String
        version: String
        description: String
    }
    security: List<SecurityRequirement> {
        [ { "BearerAuth": [] } ]
    }
    components: Components {
        securitySchemes: Map<String, SecurityScheme> {
            "BearerAuth": SecurityScheme {
                type: "http"
                scheme: "bearer"
                bearerFormat: "JWT"
                description: String
            }
        }
    }
}
```

---

## 核心流程（伪代码）

### 启动流程

```
1. Spring Boot 启动
   ↓
2. SpringDocAutoConfiguration 自动配置
   ↓
3. 扫描 @Configuration 类，找到 OpenApiConfig
   ↓
4. 调用 customOpenAPI() 获取 OpenAPI Bean
   ↓
5. SpringDoc 使用 OpenAPI Bean 生成：
   - /v3/api-docs 端点（OpenAPI JSON）
   - /swagger-ui/index.html 端点（Swagger UI）
   ↓
6. 应用就绪
```

### 访问流程

```
客户端访问 /swagger-ui.html
   ↓
SpringDoc 返回 Swagger UI HTML 页面
   ↓
页面加载后发起 AJAX 请求 /v3/api-docs
   ↓
SpringDoc 返回 OpenAPI JSON（由 OpenApiConfig 配置）
   ↓
Swagger UI 解析 JSON 并渲染文档界面
```

---

## 依赖配置

### Gradle 依赖

```kotlin
// server/build.gradle.kts
dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
}
```

**版本说明：**
- SpringDoc 2.7.0 兼容 Spring Boot 3.4
- 如果 cartisan-dependencies BOM 已管理版本，可省略版本号

---

## 技术方案说明

### 方案选择：直接在业务项目配置

**理由：**
1. cartisan-boot 目前没有 OpenAPI 模块
2. F01-03 目标是快速完成骨架搭建，不需要抽象通用模块
3. 配置简单，重复配置成本低

**未来演进：**
- 如果多个项目都需要 OpenAPI 配置，可以考虑创建 cartisan-openapi 模块

### 配置方式选择：Java Config 类

**理由：**
1. 类型安全，IDE 支持好
2. SecurityScheme 配置复杂，Java 代码比 YAML 更清晰
3. 便于后续扩展（如动态配置、条件配置）

### 为什么不使用 application.yml

**SpringDoc YAML 配置示例：**
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

**不足：**
- YAML 只能配置路径等简单属性
- Info 和 SecurityScheme 仍需要 Java Config
- 混用两种方式增加维护成本

---

## 文件结构

```
server/
├── build.gradle.kts                    # [修改] 添加依赖
└── src/
    └── main/
        └── java/
            └── com/aieducenter/
                └── config/
                    └── OpenApiConfig.java   # [新增] 配置类
```

---

## 注意事项

### SpringDoc 与 Spring Boot Actuator 的兼容性

- Actuator 端点在 `/actuator` 路径下
- Swagger UI 在 `/swagger-ui` 路径下
- 两者无冲突，可共存

### 未来 API 文档注解

本 Feature 只配置基础框架。具体业务 API 的文档注解将在后续 Epic 中添加：

```java
// 未来使用示例（不在本 Feature 范围）
@Operation(summary = "创建用户", description = "...")
@PostMapping("/users")
public ApiResponse<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
    // ...
}
```

### 安全考虑

- Swagger UI 默认无需认证即可访问
- 生产环境应考虑通过配置禁用或添加访问控制
- 本 Feature 不处理生产环境安全配置
