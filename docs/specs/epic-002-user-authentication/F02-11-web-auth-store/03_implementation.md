# Feature: F02-11 web 端认证状态管理 — 实施计划

> 版本：v1.0 | 日期：2026-03-19
> 状态：Phase 3 - Plan

---

## 目标复述

用 Zustand `persist` 中间件实现 AuthStore，替换 `packages/shared` 中的占位实现。在 `packages/api-client` 中将 `refreshMiddleware` 替换为 `authErrorMiddleware`，处理 401 自动登出。在 `web` 中实现 `AuthGuard` 客户端组件和 `(protected)/layout.tsx`，保护需要登录的路由。

---

## 变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| **修改** | `packages/shared/package.json` | 添加 vitest、happy-dom 依赖 |
| **新增** | `packages/shared/vitest.config.ts` | vitest 配置 |
| **替换** | `packages/shared/src/auth-store.ts` | 完整实现（替换占位） |
| **新增** | `packages/shared/src/auth-store.test.ts` | AuthStore 单元测试 |
| **修改** | `packages/api-client/src/api/client.ts` | 字段重命名 + 替换 refreshMiddleware |
| **修改** | `packages/api-client/src/auth/refresh.ts` | 禁用函数体，标注 TODO: F02-14 |
| **替换** | `packages/api-client/src/api/client.test.ts` | 重写测试，对齐新 API |
| **修改** | `web/package.json` | 添加 vitest、happy-dom、@testing-library/react 依赖 |
| **新增** | `web/vitest.config.ts` | vitest 配置（含路径别名） |
| **新增** | `web/src/components/auth-guard.tsx` | 路由守卫组件 |
| **新增** | `web/src/components/auth-guard.test.tsx` | AuthGuard 单元测试 |
| **修改** | `web/src/app/(protected)/layout.tsx` | 包裹 AuthGuard |

---

## 原子任务清单

### Step 1: packages/shared 添加 vitest

- **文件：** `packages/shared/package.json`、`packages/shared/vitest.config.ts`
- **内容：** devDependencies 中加入 `vitest`、`happy-dom`；vitest.config.ts 设置 environment 为 happy-dom
- **验证：** `cd packages/shared && pnpm vitest run`（无测试文件时正常退出即可）

### Step 2: 编写 AuthStore 测试（红灯）

- **文件：** `packages/shared/src/auth-store.test.ts`
- **测试用例：**
  - `given_no_state_when_login_then_token_and_user_set_and_isAuthenticated_true`
  - `given_authenticated_when_logout_then_all_state_cleared`
  - `given_authenticated_when_setUser_then_user_updated`
  - `given_token_in_localStorage_when_store_created_then_token_restored`
- **验证：** `pnpm vitest run` 红灯（import 失败或类型不匹配）

### Step 3: 实现 AuthStore（绿灯）

- **文件：** `packages/shared/src/auth-store.ts`
- **内容：**
  ```typescript
  import { create } from 'zustand'
  import { persist } from 'zustand/middleware'

  export interface AuthUser {
    userId: string  // Long ID 转字符串，防止 JS Number 精度丢失
    nickname: string
    avatar: string | null
  }

  export interface AuthState {
    token: string | null
    user: AuthUser | null
    isAuthenticated: boolean
    login: (token: string, user: AuthUser) => void
    logout: () => void
    setUser: (user: AuthUser) => void
  }

  export const useAuthStore = create<AuthState>()(
    persist(
      (set) => ({
        token: null,
        user: null,
        isAuthenticated: false,
        login: (token, user) => set({ token, user, isAuthenticated: true }),
        logout: () => set({ token: null, user: null, isAuthenticated: false }),
        setUser: (user) => set({ user }),
      }),
      {
        name: 'aieducenter-auth',
        partialize: (state) => ({ token: state.token }),  // 只持久化 token
      }
    )
  )
  ```
- **验证：** `pnpm vitest run` 全绿

### Step 4: 更新 packages/api-client

**4a — client.ts 字段重命名 + authErrorMiddleware**

- **文件：** `packages/api-client/src/api/client.ts`
- **内容：**
  1. `authMiddleware`：`useAuthStore.getState().accessToken` → `useAuthStore.getState().token`
  2. 删除 `refreshMiddleware`，添加：
     ```typescript
     const authErrorMiddleware = {
       async onResponse({ response }: { response: Response }) {
         if (response.status === 401) {
           if (typeof window !== 'undefined') {
             useAuthStore.getState().logout()
             window.location.href = '/login?reason=session_expired'
           }
         }
         return response
       },
     }
     ```
  3. 将 middleware 列表中 `refreshMiddleware` 替换为 `authErrorMiddleware`
- **验证：** `cd packages/api-client && pnpm tsc --noEmit`

**4b — refresh.ts 禁用并标注 TODO**

- **文件：** `packages/api-client/src/auth/refresh.ts`
- **内容：** 保留并发串行化骨架，函数体改为 `return false`，移除 `setAccessToken` 调用，顶部添加：
  ```typescript
  // TODO: F02-14 实现时，用 authStore.login(newToken, currentUser) 替换此处逻辑
  ```
- **验证：** 编译通过

**4c — 重写 client.test.ts**

- **文件：** `packages/api-client/src/api/client.test.ts`
- **测试用例：**
  - `given_401_response_when_api_call_then_logout_called_and_redirect_to_login`
  - `given_valid_token_when_api_call_then_authorization_header_set`
  - `given_no_token_when_api_call_then_no_authorization_header`
- **验证：** `pnpm vitest run` 全绿

### Step 5: web 添加 vitest + React Testing Library

- **文件：** `web/package.json`、`web/vitest.config.ts`
- **devDependencies：** `vitest`、`happy-dom`、`@vitejs/plugin-react`、`@testing-library/react`、`@testing-library/jest-dom`
- **vitest.config.ts 关键配置：**
  ```typescript
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') }
  },
  environment: 'happy-dom'
  ```
- **验证：** `cd web && pnpm vitest run`（无测试时正常退出）

### Step 6: 编写 AuthGuard 测试（红灯）

- **文件：** `web/src/components/auth-guard.test.tsx`
- **测试用例：**
  - `given_hydration_pending_when_render_then_show_loading_and_no_redirect`
  - `given_hydrated_no_token_when_render_then_redirect_to_login`
  - `given_hydrated_token_no_user_when_profile_fetch_succeeds_then_render_children`
  - `given_hydrated_token_no_user_when_profile_fetch_returns_401_then_no_duplicate_logout`
  - `given_hydrated_token_no_user_when_profile_fetch_network_error_then_logout_and_redirect`
  - `given_hydrated_already_authenticated_when_render_then_render_children_without_fetch`
- **关键 mock：** `next/navigation`（`useRouter`）、`useAuthStore`（`persist.hasHydrated`、`onFinishHydration`）、`fetch`
- **验证：** `pnpm vitest run` 红灯（组件不存在）

### Step 7: 实现 AuthGuard + protected layout（绿灯）

**7a — AuthGuard 组件**

- **文件：** `web/src/components/auth-guard.tsx`
- **行为规则（按顺序）：**
  1. 等待 `useAuthStore.persist.hasHydrated()` → 否则渲染加载占位，防止 SSR 阶段误跳转
  2. `token === null` → `router.replace('/login')`，不渲染 children
  3. `token` 存在 + `isAuthenticated === false` → 调 `GET /api/account/profile`（plain fetch）
     - 成功（data 存在）→ `login(token, user)` → `ready = true` → 渲染 children
     - `response.status === 401` → 直接 return（authErrorMiddleware 已处理 logout + redirect）
     - 其他异常（catch）→ `logout()` + `router.replace('/login')`
  4. `isAuthenticated === true` → `ready = true`，直接渲染 children
  5. `ready === false` 期间渲染加载占位

- **验证：** `pnpm vitest run` 全绿

**7b — protected layout**

- **文件：** `web/src/app/(protected)/layout.tsx`
- **内容：**
  ```tsx
  import { AuthGuard } from '@/components/auth-guard'

  export default function ProtectedLayout({ children }: { children: React.ReactNode }) {
    return <AuthGuard>{children}</AuthGuard>
  }
  ```
- **验证：** `pnpm tsc --noEmit`（web 目录）

### Step 8: 全量测试

- **命令：**
  ```bash
  cd packages/shared && pnpm vitest run
  cd packages/api-client && pnpm vitest run
  cd web && pnpm vitest run
  ```
- **验证：** 三个包测试全绿，所有 AC1-AC6 验收标准覆盖

---

## 核心流程（伪代码）

### 登录流程（F02-10 调用）
```
POST /api/account/login → token
GET /api/account/profile → user
authStore.login(token, user)
→ persist 自动写入 localStorage('aieducenter-auth')
```

### 刷新页面流程
```
persist hydrate → token 从 localStorage 恢复
AuthGuard：hasHydrated? → token 存在但 isAuthenticated = false
→ GET /api/account/profile（plain fetch）
→ 成功 → login(token, user) → 渲染页面
```

### 401 处理流程
```
API 请求 → authErrorMiddleware.onResponse
→ response.status === 401
→ typeof window !== 'undefined'
→ authStore.logout() + window.location.href = '/login?reason=session_expired'
```

---

## 依赖关系

- 依赖 F02-07 `GET /api/account/profile` 接口已定义（AuthGuard 内部直接调用）
- 被依赖：F02-10（登录注册页）、F02-12（个人中心）、F02-13（admin 登录页）、F02-14（Token 刷新）

---

## 注意事项

1. `AuthGuard` 必须标注 `'use client'`，不能是 Server Component
2. AuthStore 中 `userId` 类型为 `string`（后端 Long ID，JSON 返回时须确保字符串格式）
3. `user` 不持久化到 localStorage，每次应用启动重新拉取，防止 nickname/avatar 陈旧
4. AuthGuard 内 profile 请求使用 `plain fetch`，不走 api-client，避免 OpenAPI schema 依赖
5. `authErrorMiddleware` 的 logout + redirect 只在 `typeof window !== 'undefined'` 时执行（SSR 安全）
6. `refreshMiddleware` 完全删除，`refresh.ts` 函数体改为 `return false`，等 F02-14 实现
