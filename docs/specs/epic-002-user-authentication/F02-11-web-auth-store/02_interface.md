# Feature: F02-11 web 端认证状态管理 — 接口契约

> 版本：v1.0 | 日期：2026-03-19
> 状态：Phase 2 - Design

---

## 一、AuthStore 模块接口

**模块路径：** `packages/shared/src/auth-store.ts`

**导出：** `useAuthStore`、`AuthUser`、`AuthState`

### 1.1 数据类型

#### AuthUser

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | string | 用户 ID（后端 Long，JSON 传输时约定字符串格式，避免 JS Number 精度丢失） |
| nickname | string | 用户昵称 |
| avatar | string \| null | 头像 URL，无头像时为 null |

#### AuthState

| 字段/方法 | 类型 | 说明 |
|----------|------|------|
| token | string \| null | Bearer Token，来自后端登录接口 |
| user | AuthUser \| null | 当前用户信息，不持久化 |
| isAuthenticated | boolean | 是否已完成认证（token + user 均已加载） |
| login(token, user) | void | 登录成功后调用：存储 token + user，isAuthenticated = true |
| logout() | void | 登出：清空所有状态，persist 自动清除 localStorage |
| setUser(user) | void | 更新用户信息（F02-12 个人中心修改 nickname/avatar 后调用） |

### 1.2 Persist 配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| name | `'aieducenter-auth'` | localStorage key |
| partialize | `state => ({ token: state.token })` | 只持久化 token，user 不持久化 |

---

## 二、AuthGuard 组件接口

**文件路径：** `web/src/components/auth-guard.tsx`

**指令：** `'use client'`（必须，不能是 Server Component）

### 2.1 Props

| 属性 | 类型 | 必填 | 说明 |
|------|------|------|------|
| children | React.ReactNode | 是 | 受保护的页面内容 |

### 2.2 行为规则

按顺序执行：

| # | 条件 | 行为 |
|---|------|------|
| 0 | persist hydration 未完成 | 渲染加载占位，不做任何跳转（防止 SSR 阶段误判） |
| 1 | hydrated + token = null | `router.replace('/login')`，不渲染 children |
| 2a | hydrated + token 存在 + isAuthenticated = false（profile fetch 成功） | `login(token, user)` → ready = true → 渲染 children |
| 2b | hydrated + token 存在 + isAuthenticated = false（profile fetch 返回 401） | 直接 return，不重复 logout（authErrorMiddleware 已处理） |
| 2c | hydrated + token 存在 + isAuthenticated = false（网络异常，catch 块） | `logout()` + `router.replace('/login')` |
| 3 | hydrated + isAuthenticated = true | ready = true → 直接渲染 children，不重复 fetch |
| 4 | ready = false | 渲染加载占位 |

### 2.3 Profile Fetch 规范

- 使用 `plain fetch`（不使用 api-client），避免依赖 OpenAPI 生成的 schema
- 端点：`GET /api/account/profile`
- 请求头：`Authorization: Bearer {token}`
- 响应解析：读取 `data.userId`、`data.nickname`、`data.avatar` 字段

---

## 三、api-client 变更接口

### 3.1 authMiddleware（字段重命名）

**文件：** `packages/api-client/src/api/client.ts`

```typescript
// 旧：useAuthStore.getState().accessToken
// 新：
const authMiddleware = {
  async onRequest({ request }) {
    const token = useAuthStore.getState().token
    if (token) {
      request.headers.set('Authorization', `Bearer ${token}`)
    }
    return request
  },
}
```

### 3.2 authErrorMiddleware（替换 refreshMiddleware）

**文件：** `packages/api-client/src/api/client.ts`

```typescript
const authErrorMiddleware = {
  async onResponse({ response }) {
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

**说明：**
- `refreshMiddleware` 完全删除，由 `authErrorMiddleware` 替代
- `typeof window !== 'undefined'` 检查保证 SSR 安全
- 是 401 的唯一权威处理者，AuthGuard 检测到 401 直接 return，不重复调用 logout

### 3.3 refresh.ts（禁用并标注 TODO）

**文件：** `packages/api-client/src/auth/refresh.ts`

```typescript
// TODO: F02-14 实现时，用 authStore.login(newToken, currentUser) 替换此处逻辑

export async function refreshAccessTokenOnce(): Promise<boolean> {
  return false  // F02-14 前禁用
}
```

**说明：** 保留并发串行化骨架结构，仅禁用函数体，等 F02-14 接入真实刷新逻辑。

---

## 四、(protected)/layout.tsx

**文件：** `web/src/app/(protected)/layout.tsx`

```tsx
import { AuthGuard } from '@/components/auth-guard'

export default function ProtectedLayout({ children }: { children: React.ReactNode }) {
  return <AuthGuard>{children}</AuthGuard>
}
```

---

## 五、错误处理

| 场景 | 处理方式 | 备注 |
|------|---------|------|
| API 返回 401 | authErrorMiddleware：logout + 跳转 `/login?reason=session_expired` | 浏览器环境才执行 |
| profile fetch 返回 401 | AuthGuard：直接 return，不重复处理 | authErrorMiddleware 已处理 |
| profile fetch 网络异常 | AuthGuard catch 块：logout + router.replace('/login') | 兜底保护 |
| 未登录访问受保护路由 | AuthGuard：router.replace('/login') | token = null 时触发 |
| SSR 阶段（hydration 未完成） | 渲染加载占位，不执行任何 auth 逻辑 | 防止误跳转 |
