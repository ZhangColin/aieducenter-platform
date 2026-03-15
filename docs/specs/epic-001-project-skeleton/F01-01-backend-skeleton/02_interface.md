# Feature: 后端项目骨架搭建 — 接口契约

> Epic: Epic 1 - 项目骨架
> Feature: F01-01
> 版本：v1.0 | 日期：2026-03-15

---

## 一、接口定义

### GET /api/health

**描述：** 应用健康检查接口，用于验证服务是否正常运行

**鉴权：** 无（公开接口）

**Request：**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|---------|------|
| - | - | - | - | 无请求参数 |

**Response (成功)：**

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 响应码，0 表示成功 |
| message | String | 响应消息，"OK" |
| data.status | String | 健康状态，固定值 "ok" |
| data.timestamp | String | ISO-8601 格式时间戳 |
| requestId | String (可选) | 请求追踪 ID |

**错误码：**

| 错误码 | HTTP Status | 触发条件 |
|--------|------------|---------|
| - | - | 本接口无业务错误，正常情况只返回 200 |

**示例（成功）：**

```bash
$ curl http://localhost:8080/api/health

{
  "code": 0,
  "message": "OK",
  "data": {
    "status": "ok",
    "timestamp": "2026-03-15T12:34:56.789Z"
  },
  "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

## 二、领域接口描述（伪代码）

### 健康检查服务

**接口：** `HealthController`

**职责：** 提供应用级健康检查，不涉及具体业务领域

**方法：**

```
health() -> HealthResponse
```

**返回值结构：**

```
HealthResponse {
  status: String      // 固定值 "ok"
  timestamp: Instant  // 当前时间
}
```

**异常处理：** 无（本接口不抛出业务异常，系统异常由全局异常处理器统一处理）

---

## 三、数据结构描述

### HealthResponse（Record）

| 字段 | 类型 | 说明 |
|------|------|------|
| status | String | 固定值 "ok" |
| timestamp | Instant | 响应生成时间 |

---

## 四、配置接口描述

### application.yml 配置项

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| server.port | int | 8080 | HTTP 服务端口 |
| spring.application.name | String | aieducenter-platform | 应用名称 |

### logback-spring.xml 配置

- 控制台输出，格式：`时间 [线程] 级别 Logger - 消息`
- `com.aieducenter` 日志级别：INFO
- `com.cartisan` 日志级别：DEBUG（开发期观察框架行为）

---

## 五、Gradle 配置描述

### settings.gradle.kts

```kotlin
rootProject.name = "aieducenter-server"

// Composite Build：引用本地 cartisan-boot
// 路径：server/ → ../aieducenter-platform/ → ../workspace/ → cartisan-boot/
includeBuild("../../cartisan-boot")
```

### build.gradle.kts 关键配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| group | com.aieducenter | 项目组 ID |
| version | 1.0.0-SNAPSHOT | 项目版本 |
| java.toolchain | 21 | Java 版本 |
| 依赖 BOM | com.cartisan:cartisan-dependencies:1.0.0-SNAPSHOT | 由 Composite Build 提供 |
| 核心依赖 | cartisan-core, cartisan-web | 基础能力 |
| 测试依赖 | cartisan-test | 测试支持 |

---

## 六、包结构描述

```
com.aieducenter
├── AieduCenterApplication              # 主启动类
├── controller                           # 应用级接口
│   └── HealthController                 # 健康检查
├── account                              # Account Context（空目录）
│   ├── domain/
│   ├── application/
│   ├── controller/
│   └── infrastructure/
├── tenant                               # Tenant Context（空目录）
│   └── ...（四层）
├── gateway                              # Gateway Context（空目录）
│   └── ...（四层）
├── conversation                         # Conversation Context（空目录）
│   └── ...（四层）
├── billing                              # Billing Context（空目录）
│   └── ...（四层）
├── agent                                # Agent Context（空目录）
│   └── ...（四层）
├── creative                             # Creative Context（空目录）
│   └── ...（四层）
└── admin                                # Admin Context（空目录）
    └── ...（四层）
```

**包职责说明：**

| 包 | 职责 |
|----|------|
| `com.aieducenter.controller` | 应用级接口（健康检查、版本信息等），不属于任何业务 Context |
| `com.aieducenter.{context}.controller` | 各业务 Context 的 HTTP 适配器 |
| `com.aieducenter.{context}.application` | 各业务 Context 的应用服务 |
| `com.aieducenter.{context}.domain` | 各业务 Context 的领域模型 |
| `com.aieducenter.{context}.infrastructure` | 各业务 Context 的基础设施实现 |

---

## 七、非功能需求

### 性能

- 启动时间 < 30 秒（本地开发环境，开发机器）
- `/api/health` 响应时间 < 100ms

### 可观测性

- 启动日志包含：Spring Boot 版本、应用名、端口
- 请求日志包含：HTTP 方法、路径、状态码
- cartisan 框架日志级别 DEBUG，便于观察框架行为

---

## 八、技术方案说明

### Composite Build 方案选择

**选择理由：**

1. **开发期便利**：修改 cartisan-boot 后无需重新发布到 Maven Local，aieducenter-platform 自动获取最新代码
2. **IDE 支持**：IntelliJ IDEA 可同时导航到两个项目，调试方便
3. **独立构建**：两个项目保持独立的版本控制

**备选方案未选择：**

| 方案 | 未选择原因 |
|------|-----------|
| Maven Local | 每次修改框架后需手动 publish，容易遗忘 |
| 直接复制源码 | 代码重复，无法追踪框架版本变化 |

### 健康检查接口位置设计

**选择：** `com.aieducenter.controller.HealthController`

**理由：**

- 健康检查不是任何业务 Context 的职责
- 应用级接口统一放在根 controller 包，与各 Context 的 controller 区分
- 为未来可能的其他应用级接口（版本信息、配置检查等）预留空间

---

## 九、与后续 Feature 的接口约定

### F01-02 Actuator 健康检查

- 本 Feature 的 `/api/health` 是临时实现
- F01-02 将引入 Spring Boot Actuator，提供 `/actuator/health`
- 两个接口可共存，`/api/health` 可保留作为自定义健康检查

### F01-03 OpenAPI 文档

- 本 Feature 的接口将被自动纳入 OpenAPI 文档
- HealthController 需要适当的 JavaDoc 注解，便于生成文档
