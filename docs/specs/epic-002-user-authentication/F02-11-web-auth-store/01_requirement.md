# Feature: F02-11 web 端认证状态管理

> 版本：v1.0 | 日期：2026-03-19
> 状态：Phase 1 - Research

---

## 背景

F02-11 为 web 端（用户侧）提供认证状态基础设施，是前端登录体系的核心。后续 F02-10（登录注册页）、F02-12（个人中心）、F02-13（admin 登录页）均依赖本 Feature 提供的 AuthStore 和路由守卫。

后端采用 Sa-Token 签发 7 天有效期的单 Token，当前不提供 Token 刷新端点（刷新机制后置至 F02-14）。Token 失效时直接跳转登录页是本阶段唯一的 401 处理策略。

---

## 目标

1. 用 Zustand `persist` 中间件实现 Token 的 localStorage 持久化，替换占位实现
2. 实现 AuthGuard 路由守卫组件，保护 `(protected)` 路由组
3. 在 `packages/api-client` 中处理 401，自动清空认证状态并跳转登录页
4. 为 F02-10 / F02-12 / F02-13 / F02-14 预留标准接口

---

## 范围

### 包含（In Scope）

| 内容 | 说明 |
|------|------|
| **AuthStore** | 完整替换 `packages/shared/src/auth-store.ts` 占位实现 |
| **路由守卫** | `web/src/components/auth-guard.tsx` 客户端守卫组件 |
| **受保护路由** | `web/src/app/(protected)/layout.tsx` 包裹 AuthGuard |
| **401 处理** | `packages/api-client/src/api/client.ts` 替换 refreshMiddleware 为 authErrorMiddleware |
| **Refresh 桩** | `packages/api-client/src/auth/refresh.ts` 禁用函数体，标注 TODO: F02-14 |
| **单元测试** | AuthStore、AuthGuard、client.ts 401 处理 |

### 不包含（Out of Scope）

| 内容 | 原因 | 归属 Feature |
|------|------|-------------|
| 登录页 UI | 属于页面层 | F02-10 |
| 个人中心页面 | 属于页面层 | F02-12 |
| admin 登录页 | 属于页面层 | F02-13 |
| Token 刷新机制 | 后端暂无刷新端点，后置 | F02-14 |

---

## 验收标准（Acceptance Criteria）

### AC1: Token 持久化
- [ ] 登录后刷新页面，localStorage 中 `aieducenter-auth` key 存在且包含 token
- [ ] 单元测试验证 login() 后 persist 写入 localStorage

### AC2: 刷新页面免重新登录
- [ ] persist hydration 恢复 token 后，AuthGuard 自动调用 `/api/account/profile` 重新加载 user
- [ ] 加载成功后 isAuthenticated = true，正常渲染页面
- [ ] 单元测试验证此流程

### AC3: 未登录跳转 /login
- [ ] token 为 null 时，AuthGuard 调用 `router.replace('/login')`
- [ ] 不渲染 children
- [ ] 单元测试验证跳转行为

### AC4: Token 失效（API 返回 401）跳转 /login
- [ ] client.ts 中 401 触发 `authStore.logout()` + `window.location.href = '/login?reason=session_expired'`
- [ ] 仅在浏览器环境执行（SSR 安全）
- [ ] 单元测试验证 logout 被调用

### AC5: 登出清空所有状态
- [ ] `authStore.logout()` 执行后 token / user / isAuthenticated 均为初始值
- [ ] localStorage 中 `aieducenter-auth` 的 token 字段清空
- [ ] 单元测试验证

### AC6: setUser 更新用户信息
- [ ] `authStore.setUser(user)` 仅更新 user 字段，不影响 token 和 isAuthenticated
- [ ] 单元测试验证

---

## 约束

### 技术约束
- Token 存储位置：localStorage（不使用 Cookie，避免 CSRF 复杂性）
- AuthStore 代码位置：`packages/shared`，web 和 admin 共用同一份代码，运行时各自隔离
- 路由守卫实现：客户端组件（不使用 Next.js middleware.ts，因服务端无法读 localStorage）
- user 信息不持久化，每次应用启动重新拉取，防止数据陈旧
- AuthGuard 内 profile 请求使用 plain fetch，不走 api-client（避免 OpenAPI schema 依赖）

### 安全约束
- authErrorMiddleware 的 logout + redirect 只在 `typeof window !== 'undefined'` 时执行
- userId 类型为 string（后端 Long ID，JSON 传输避免 JS Number 精度丢失）

---

## 依赖关系

| 依赖 Feature | 依赖类型 | 说明 |
|-------------|---------|------|
| F02-07 个人信息管理 | 软依赖 | AuthGuard 内调用 GET /api/account/profile，接口需已定义 |

---

## 参考资料

- [Epic 2 Backlog](../00_epic_backlog.md)
- [设计文档](../../../superpowers/specs/2026-03-19-f02-11-auth-store-design.md)
