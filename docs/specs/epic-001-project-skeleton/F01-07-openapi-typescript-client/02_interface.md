# Feature: F01-07 OpenAPI TypeScript 客户端生成 — 接口契约

> **Epic**: Epic 001 - 项目骨架
> **版本**: v1.0
> **日期**: 2026-03-16

---

## 一、接口定义

### 1.1 NPM 脚本接口

| 脚本名 | 参数 | 行为 | 验收方式 |
|--------|------|------|----------|
| `gen:api` | 无 | 从 `openapi.json` 生成类型到 `src/api/schema.ts` | 文件生成，无报错 |
| `gen:api:live` | 无 | 从 `http://localhost:8080/v3/api-docs` 生成类型 | 后端运行时文件生成 |
| `sync:openapi` | `OPENAPI_URL`（可选） | 从后端拉取 OpenAPI JSON 到 `openapi.json` | 文件更新，时间戳最新 |

### 1.2 导出接口

**主入口：**
```typescript
import { api } from '@aieducenter/api-client'
import type { paths, components, operations } from '@aieducenter/api-client'
```

**Schema 导出：**
```typescript
import type { paths } from '@aieducenter/api-client/schema'
```

---

## 二、类型定义（伪代码）

### 2.1 生成的 Schema 类型

由 `openapi-typescript` 自动生成，包含：

```typescript
// paths: 所有端点的路径、方法、参数、响应类型
type paths = {
  '/api/v1/users': {
    get: {
      parameters: { query?: { page?: number, size?: number } }
      responses: {
        200: { content: { 'application/json': ApiResponse<PageResponse<UserDto>> } }
        401: { content: { 'application/json': ErrorResponse } }
      }
    }
    // ... 其他端点
  }
  // ... 其他路径
}

// components: Schema 组件定义
type components = {
  schemas: {
    UserDto: { id: number, name: string, email: string }
    // ... 其他 DTO
  }
}
```

### 2.2 手动补充类型

**src/api/types.ts:**

```typescript
/** 后端统一响应格式（cartisan-web ApiResponse<T>） */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  requestId: string
  errors?: FieldError[]
}

/** 字段级错误 */
export interface FieldError {
  field: string
  message: string
  errorCode?: string
}

/** 分页响应 */
export interface PageResponse<T> {
  items: T[]
  total: number
  page: number
  size: number
}
```

---

## 三、API 客户端接口（伪代码）

### 3.1 client.ts 导出接口

**导出：**

```typescript
// 创建好的客户端实例
export const api: ApiClient<paths>

// 类型
export type ApiClient<Paths> = {
  get<Path, P>(path: Path, options?: P): Promise<ResponseType>
  post<Path, P>(path: Path, options?: P): Promise<ResponseType>
  put<Path, P>(path: Path, options?: P): Promise<ResponseType>
  delete<Path, P>(path: Path, options?: P): Promise<ResponseType>
}
```

**中间件注册接口：**

```typescript
api.use({
  onRequest?(context: { request: Request }): Promise<Request>
  onResponse?(context: { request: Request, response: Response }): Promise<Response>
})
```

### 3.2 auth/refresh.ts 导出接口

**函数签名：**

```typescript
/**
 * 刷新 access token（串行化）
 *
 * 行为：
 * - 如果已有刷新在进行，等待其结果
 * - 调用后端刷新接口，cookie 中的 refresh token 自动携带
 * - 成功则更新 Zustand store 中的 access token
 * - 失败则返回 false
 *
 * @returns 刷新是否成功
 */
export async function refreshAccessTokenOnce(): Promise<boolean>
```

**依赖的外部接口：**

```typescript
// @aieducenter/shared/auth-store 必须提供
interface AuthStore {
  accessToken: string | null
  setAccessToken(token: string): void
}
```

---

## 四、核心流程（伪代码）

### 4.1 正常请求流程

```
1. 调用方：api.GET('/api/v1/users', { params: { query: { page: 1 } } })
2. client.ts onRequest:
   - 从 authStore 获取 accessToken
   - 注入 Authorization: Bearer {token} 头
3. 发送请求到后端
4. 接收响应
5. 返回 { data, error } 给调用方
```

### 4.2 401 刷新流程

```
1. 请求返回 401
2. client.ts onResponse:
   - 检查是否刷新接口本身（是 → 直接返回）
   - 调用 refreshAccessTokenOnce()
3. refresh.ts:
   - 检查是否已有刷新在进行（是 → 返回现有 Promise）
   - 发起刷新请求（credentials: include 携带 cookie）
   - 成功 → authStore.setAccessToken(newToken) → 返回 true
   - 失败 → 返回 false
4. client.ts:
   - 刷新失败 → 跳转 /login?reason=token_expired
   - 返回原 401 response 给调用方
```

### 4.3 并发 401 串行化流程

```
时刻 T0: 请求 A、B、C 同时发出，都携带过期的 token
时刻 T1: 三个请求同时返回 401
时刻 T2:
  - A 进入 onResponse，调用 refreshAccessTokenOnce()
  - B 进入 onResponse，调用 refreshAccessTokenOnce() → 返回相同的 Promise
  - C 进入 onResponse，调用 refreshAccessTokenOnce() → 返回相同的 Promise
时刻 T3: 刷新完成，token 更新到 store
时刻 T4: A、B、C 都收到刷新成功的结果
```

---

## 五、数据库/文件变更

| 类型 | 路径 | 说明 |
|------|------|------|
| 新建 | `packages/api-client/src/api/schema.ts` | openapi-typescript 生成 |
| 新建 | `packages/api-client/src/api/client.ts` | API 客户端封装 |
| 新建 | `packages/api-client/src/api/types.ts` | 手动补充类型 |
| 新建 | `packages/api-client/src/auth/refresh.ts` | Token 刷新逻辑 |
| 新建 | `packages/api-client/src/index.ts` | 统一导出 |
| 新建 | `packages/api-client/openapi.json` | 静态 OpenAPI schema |
| 新建 | `packages/api-client/scripts/sync-openapi.sh` | 同步脚本 |
| 修改 | `packages/api-client/package.json` | 依赖与脚本配置 |

---

## 六、错误码映射

| HTTP 状态 | 后端 CodeMessage | 前端处理 |
|-----------|-----------------|----------|
| 200 | - | 正常返回 data |
| 401 | UNAUTHORIZED | 自动刷新 token，失败跳登录 |
| 403 | FORBIDDEN | 返回 response，由调用方处理 |
| 404 | NOT_FOUND | 返回 response，由调用方处理 |
| 4xx | 其他 | 返回 response，由调用方处理 |
| 500 | INTERNAL_ERROR | 返回 response，由调用方处理 |

---

## 七、外部依赖

### 7.1 后端依赖

| 接口 | 方法 | 说明 | 状态 |
|------|------|------|------|
| `/v3/api-docs` | GET | 获取 OpenAPI JSON | ✅ 已实现（F01-03） |
| `/api/v1/auth/refresh` | POST | 刷新 access token | ⏳ 待实现（Epic 02） |

### 7.2 前端依赖

| 包 | 导出 | 说明 | 状态 |
|----|------|------|------|
| `@aieducenter/shared/auth-store` | `useAuthStore` | 认证状态管理 | ⏳ 待实现（Epic 02） |

### 7.3 第三方依赖

| 包 | 版本 | 用途 |
|----|------|------|
| `openapi-typescript` | ^7.4.5 | 类型生成 |
| `openapi-fetch` | ^0.12.2 | Fetch 客户端 |

---

## 八、技术方案说明

### 8.1 为什么选择 openapi-typescript + openapi-fetch

| 方案 | 优点 | 缺点 | 选择 |
|------|------|------|------|
| openapi-typescript + fetch | 类型安全，灵活，与 Next.js 原生 fetch 无缝集成 | 需要手动封装 | ✅ 推荐 |
| openapi-generator-cli | 功能完整，自动生成 SDK | 代码量大，更新困难 | ❌ |
| orval | 功能强大，可生成 React Query hooks | 配置复杂 | ❌ |

### 8.2 为什么不自动重试

Fetch Request 的 body 是流式的，读取一次后就被消耗，无法直接用同一个 Request 对象重试。自动重试需要：
1. 在 onRequest 时 clone Request 并存储
2. 401 时取出 clone 进行重试

这增加了复杂度和内存占用。因此选择由调用方决定是否重试，client 只负责刷新 token。

### 8.3 为什么需要刷新串行化

多个并发请求同时 401 时，如果都独立触发刷新：
- 后端可能收到多个刷新请求，造成资源浪费
- 可能出现竞态条件，token 状态不一致

使用单例 Promise 模式，确保同一时间只有一个刷新在进行，其他 401 请求等待其结果。
