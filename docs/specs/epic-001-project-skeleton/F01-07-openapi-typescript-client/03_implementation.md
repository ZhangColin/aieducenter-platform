# F01-07 OpenAPI TypeScript 客户端生成 — 实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 配置从后端 OpenAPI 规范自动生成 TypeScript 类型定义和 API 客户端，支持 token 自动注入和刷新

**Architecture:** 使用 openapi-typescript 生成类型，openapi-fetch 创建客户端，client.ts 通过中间件注入 token 并处理 401 刷新，refresh.ts 实现串行化刷新逻辑

**Tech Stack:** openapi-typescript ^7.4.5, openapi-fetch ^0.12.2, TypeScript ^5.0.0

---

## Task 1: 配置 package.json 依赖

**Files:**
- Modify: `packages/api-client/package.json`

**Step 1: 更新 package.json 添加依赖和脚本**

```json
{
  "name": "@aieducenter/api-client",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "exports": {
    ".": "./src/index.ts",
    "./schema": "./src/api/schema.ts"
  },
  "scripts": {
    "gen:api": "openapi-typescript ./openapi.json -o ./src/api/schema.ts",
    "gen:api:live": "openapi-typescript http://localhost:8080/v3/api-docs -o ./src/api/schema.ts",
    "sync:openapi": "bash ./scripts/sync-openapi.sh",
    "typecheck": "tsc --noEmit"
  },
  "dependencies": {
    "openapi-fetch": "^0.12.2",
    "@aieducenter/shared": "workspace:*"
  },
  "devDependencies": {
    "openapi-typescript": "^7.4.5",
    "typescript": "^5.0.0"
  }
}
```

**Step 2: 安装依赖**

Run: `pnpm install`
Expected: 依赖安装成功，无报错

**Step 3: 提交**

```bash
git add packages/api-client/package.json
git commit -m "feat(api-client): 配置 openapi-typescript 和 openapi-fetch 依赖"
```

---

## Task 2: 创建 sync-openapi.sh 脚本

**Files:**
- Create: `packages/api-client/scripts/sync-openapi.sh`

**Step 1: 创建同步脚本**

```bash
#!/bin/bash
# 从后端同步 OpenAPI JSON 到本地

OPENAPI_URL=${OPENAPI_URL:-http://localhost:8080/v3/api-docs}
OUTPUT_FILE=./openapi.json

echo "正在从 $OPENAPI_URL 获取 OpenAPI schema..."

if curl -f -s "$OPENAPI_URL" -o "$OUTPUT_FILE"; then
  echo "✓ OpenAPI schema 已同步到 $OUTPUT_FILE"
  echo "运行 pnpm gen:api 生成类型"
else
  echo "✗ 获取失败，请确认后端服务是否运行"
  exit 1
fi
```

**Step 2: 赋予执行权限**

Run: `chmod +x packages/api-client/scripts/sync-openapi.sh`
Expected: 权限设置成功

**Step 3: 提交**

```bash
git add packages/api-client/scripts/sync-openapi.sh
git commit -m "feat(api-client): 添加 OpenAPI schema 同步脚本"
```

---

## Task 3: 同步初始 openapi.json

**Files:**
- Create: `packages/api-client/openapi.json`

**Step 1: 确保后端服务运行**

Run: `cd server && ./gradlew bootRun`
Expected: 后端启动成功，日志显示 "Started AieducenterApplication"

**Step 2: 执行同步脚本**

Run: `cd packages/api-client && pnpm sync:openapi`
Expected: 输出 "✓ OpenAPI schema 已同步"

**Step 3: 验证文件生成**

Run: `ls -lh packages/api-client/openapi.json`
Expected: 文件存在且大小 > 0

**Step 4: 提交**

```bash
git add packages/api-client/openapi.json
git commit -m "feat(api-client): 添加初始 OpenAPI schema"
```

---

## Task 4: 生成 schema.ts 类型文件

**Files:**
- Create: `packages/api-client/src/api/schema.ts`

**Step 1: 生成类型**

Run: `cd packages/api-client && pnpm gen:api`
Expected: 输出生成日志，无报错

**Step 2: 验证文件生成**

Run: `head -n 20 packages/api-client/src/api/schema.ts`
Expected: 文件包含 `export interface paths` 等 TypeScript 类型定义

**Step 3: 检查 TypeScript 编译**

Run: `cd packages/api-client && pnpm typecheck`
Expected: 无类型错误

**Step 4: 提交**

```bash
git add packages/api-client/src/api/schema.ts
git commit -m "feat(api-client): 生成 OpenAPI TypeScript 类型定义"
```

---

## Task 5: 创建 types.ts 手动补充类型

**Files:**
- Create: `packages/api-client/src/api/types.ts`

**Step 1: 创建 types.ts**

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

**Step 2: 检查 TypeScript 编译**

Run: `cd packages/api-client && pnpm typecheck`
Expected: 无类型错误

**Step 3: 提交**

```bash
git add packages/api-client/src/api/types.ts
git commit -m "feat(api-client): 添加手动补充的 API 类型定义"
```

---

## Task 6: 创建 auth/refresh.ts 刷新逻辑

**Files:**
- Create: `packages/api-client/src/auth/refresh.ts`

**Step 1: 创建刷新模块**

```typescript
import { useAuthStore } from '@aieducenter/shared/auth-store'

let refreshPromise: Promise<boolean> | null = null

/** 后端刷新接口响应格式 */
interface RefreshResponse {
  data: {
    token: string
    expireAt: string
  }
}

/**
 * 刷新 access token（串行化，防止并发 401 时多次刷新）
 *
 * @returns 刷新是否成功
 */
export async function refreshAccessTokenOnce(): Promise<boolean> {
  if (refreshPromise) {
    return refreshPromise
  }

  const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

  refreshPromise = (async () => {
    try {
      const response = await fetch(`${API_URL}/api/v1/auth/refresh`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
      })

      if (!response.ok) return false

      const data = await response.json() as RefreshResponse
      useAuthStore.getState().setAccessToken(data.data.token)
      return true
    } catch {
      return false
    } finally {
      refreshPromise = null
    }
  })()

  return refreshPromise
}
```

**注意**: `@aieducenter/shared/auth-store` 将在 Epic 02 实现，当前可创建临时占位

**Step 2: 创建临时 auth-store 占位**

创建 `packages/shared/src/auth-store.ts`:

```typescript
import { create } from 'zustand'

interface AuthState {
  accessToken: string | null
  setAccessToken: (token: string) => void
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  setAccessToken: (token) => set({ accessToken: token }),
}))
```

**Step 3: 更新 shared package.json 导出**

修改 `packages/shared/package.json`:

```json
{
  "exports": {
    ".": "./src/index.ts",
    "./auth-store": "./src/auth-store.ts"
  }
}
```

**Step 4: 检查 TypeScript 编译**

Run: `cd packages/api-client && pnpm typecheck`
Expected: 无类型错误（可能有 import 警告，可忽略）

**Step 5: 提交**

```bash
git add packages/api-client/src/auth/refresh.ts
git add packages/shared/src/auth-store.ts
git add packages/shared/package.json
git commit -m "feat(api-client): 添加 token 刷新串行化逻辑"
```

---

## Task 7: 创建 client.ts API 客户端

**Files:**
- Create: `packages/api-client/src/api/client.ts`

**Step 1: 创建 API 客户端**

```typescript
import createClient from 'openapi-fetch'
import type { paths } from './schema'
import { useAuthStore } from '@aieducenter/shared/auth-store'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
const REFRESH_PATH = '/api/v1/auth/refresh'

export const api = createClient<paths>({
  baseUrl: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// 注入 access token
api.use({
  async onRequest({ request }) {
    const token = useAuthStore.getState().accessToken
    if (token) {
      request.headers.set('Authorization', `Bearer ${token}`)
    }
    return request
  },
})

// 统一响应处理
api.use({
  async onResponse({ response }) {
    if (response.status === 401 && !response.url.includes(REFRESH_PATH)) {
      const { refreshAccessTokenOnce } = await import('../auth/refresh')
      const refreshed = await refreshAccessTokenOnce()
      if (!refreshed && typeof window !== 'undefined') {
        window.location.href = '/login?reason=token_expired'
      }
    }
    return response
  },
})
```

**Step 2: 检查 TypeScript 编译**

Run: `cd packages/api-client && pnpm typecheck`
Expected: 无类型错误

**Step 3: 提交**

```bash
git add packages/api-client/src/api/client.ts
git commit -m "feat(api-client): 添加 API 客户端封装"
```

---

## Task 8: 创建 index.ts 统一导出

**Files:**
- Modify: `packages/api-client/src/index.ts`

**Step 1: 更新 index.ts**

```typescript
export { api } from './api/client'
export type { paths, components, operations } from './api/schema'

// 便捷类型导出
export type ApiError = {
  error?: {
    status: number
    message: string
  }
}

// 重新导出手动类型
export type { ApiResponse, FieldError, PageResponse } from './api/types'
```

**Step 2: 检查 TypeScript 编译**

Run: `cd packages/api-client && pnpm typecheck`
Expected: 无类型错误

**Step 3: 提交**

```bash
git add packages/api-client/src/index.ts
git commit -m "feat(api-client): 添加统一导出入口"
```

---

## Task 9: 更新 tsconfig.json 配置

**Files:**
- Modify: `packages/api-client/tsconfig.json`

**Step 1: 确保 tsconfig.json 配置正确**

```json
{
  "extends": "../../tsconfig.base.json",
  "compilerOptions": {
    "outDir": "./dist",
    "rootDir": "./src"
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "dist"]
}
```

**Step 2: 检查 TypeScript 编译**

Run: `cd packages/api-client && pnpm typecheck`
Expected: 无类型错误

**Step 3: 提交（如有变更）**

```bash
git add packages/api-client/tsconfig.json
git commit -m "chore(api-client): 更新 tsconfig 配置"
```

---

## Task 10: 验证整体功能

**Files:**
- Test: 手动验证

**Step 1: 测试 gen:api 脚本**

Run: `cd packages/api-client && pnpm gen:api`
Expected: schema.ts 更新成功

**Step 2: 测试 gen:api:live 脚本（需后端运行）**

Run: `cd packages/api-client && pnpm gen:api:live`
Expected: 从后端生成 schema.ts

**Step 3: 测试 sync:openapi 脚本（需后端运行）**

Run: `cd packages/api-client && pnpm sync:openapi`
Expected: openapi.json 更新成功

**Step 4: 测试导出**

在 Node.js REPL 测试:

```bash
node
> import { api } from '@aieducenter/api-client'
> typeof api
'object'
```

**Step 5: 提交（如有修复）**

```bash
git add -A
git commit -m "fix(api-client): 修复验证发现的问题"
```

---

## 验收检查清单

- [ ] `pnpm gen:api` 可正确生成 `src/api/schema.ts`
- [ ] `pnpm gen:api:live` 可从运行中后端生成类型
- [ ] `pnpm sync:openapi` 可同步最新的 OpenAPI JSON
- [ ] `import { api } from '@aieducenter/api-client'` 可用
- [ ] `import type { paths } from '@aieducenter/api-client/schema'` 可用
- [ ] TypeScript 编译无错误
- [ ] 生成的类型包含后端端点的 Request/Response 结构

## 依赖说明

此 Feature 依赖以下将在后续 Epic 实现的内容：

| 依赖 | 所属 Epic | 说明 |
|------|---------|------|
| `/api/v1/auth/refresh` 端点 | Epic 02 | 后端刷新 token 接口 |
| `@aieducenter/shared/auth-store` 完整实现 | Epic 02 | 包含用户信息、登录状态等 |

当前实现中 `auth-store` 为临时占位，Epic 02 时将扩展完整功能。
