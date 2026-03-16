# Feature: 前后端联调验证 — 接口契约

> 版本：v1.0 | 日期：2026-03-16

---

## 一、HTTP 接口定义

### GET /api/health

**描述**：健康检查端点，用于验证前后端通信链路

**鉴权**：无需鉴权（公开端点）

**Request**：无请求体

**Response (成功)**：

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | HTTP 状态码，200 表示成功 |
| message | string | 响应消息，成功时为 "success" |
| data | object | 响应数据 |
| data.status | string | 固定值 "ok" |
| data.timestamp | string | ISO 8601 格式时间戳 |

**示例（成功）**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "ok",
    "timestamp": "2026-03-16T10:30:00Z"
  }
}
```

**错误码**：无（健康检查端点不返回错误）

---

## 二、前端组件接口描述（伪代码）

```
组件：HealthCheckCard

属性：
  无

状态：
  - healthStatus: 'loading' | 'healthy' | 'error'
  - lastChecked: string | null
  - errorMessage: string | null

行为：
  - onMount(): 调用后端 /api/health
  - onSuccess(data): 更新状态为 healthy，显示 timestamp
  - onError(error): 更新状态为 error，显示错误信息

UI 结构：
  ┌─────────────────────────┐
  │ 后端连接              │
  │ ✅ 正常                │
  │ 最后检查: 10:30:00      │
  └─────────────────────────┘
```

---

## 三、脚本接口描述

### scripts/dev.sh

**功能**：同时启动后端和前端开发服务器

**输入**：无参数

**行为**：
1. 启动 Spring Boot 后端（后台运行）
2. 启动 Next.js 前端（前台运行）
3. 捕获 Ctrl+C 信号，清理后端进程

**输出**：
- 后端日志输出到 `logs/backend.log`
- 前端日志输出到 stdout

---

### scripts/verify-integration.sh

**功能**：自动化验证前后端集成

**输入**：
- `--port`: 后端端口，默认 8080
- `--timeout`: 等待超时秒数，默认 30

**行为**：
1. 等待后端启动（轮询 `/api/health`）
2. 发送 GET 请求到 `/api/health`
3. 验证响应 JSON 格式
4. 输出验证结果

**输出**：
- 成功：`✅ PASS - Backend is healthy`
- 失败：`❌ FAIL - <错误原因>`

---

## 四、核心流程（伪代码）

### 后端健康检查流程

```
当接收到 GET /api/health 请求：

1. 创建 HealthResponse 对象
   - status = "ok"
   - timestamp = Instant.now().toString()

2. 返回 ApiResponse.ok(HealthResponse)
```

### 前端健康检查流程

```
当组件挂载时：

1. 设置状态为 loading

2. 调用 api.GET /api/health
   - 使用 openapi-fetch 客户端
   - 无需认证 token

3. 如果成功：
   - 设置状态为 healthy
   - 显示 data.timestamp

4. 如果失败：
   - 设置状态为 error
   - 显示错误信息
```

---

## 五、数据结构定义（伪代码）

```
// 后端 - Java
record HealthResponse(
    String status,      // 固定值 "ok"
    String timestamp    // ISO 8601 格式
)

// 前端 - TypeScript
type HealthResponse = {
  status: 'ok'
  timestamp: string
}

type ApiResponse<T> = {
  code: number
  message: string
  data: T
}
```

---

## 六、技术方案说明

### 后端技术选择

- **Response 包装**：使用 cartisan-boot 提供的 `ApiResponse<T>`
- **Record**：使用 Java Record 定义 `HealthResponse`
- **时间格式**：`java.time.Instant.toString()` 生成 ISO 8601 格式

### 前端技术选择

- **API 客户端**：使用 `@aieducenter/api-client` 的 `api` 实例
- **状态管理**：使用 React `useState`
- **时间格式化**：使用 `new Date(timestamp).toLocaleString()`

### 脚本技术选择

- **进程管理**：使用 `&` 后台运行，`trap` 捕获退出信号
- **健康检查**：使用 `curl` 轮询
- **JSON 解析**：使用 `jq` 验证响应格式
