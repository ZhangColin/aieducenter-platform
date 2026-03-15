# Feature: F01-05 前端路由结构与基础布局 — 实施计划

> 版本：v1.0 | 日期：2026-03-16
> 状态：Phase 3 完成

---

## 目标复述

在 web 和 admin 应用中创建路由组结构和基础布局组件。web 应用添加 `(public)` 和 `(auth)` 路由组（首页、登录页），admin 应用添加侧边导航布局。所有路由可访问且无报错。

---

## 变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `web/src/app/layout.tsx` | 根布局（简化为全局容器） |
| 修改 | `web/src/app/page.tsx` | 重定向到首页 |
| 新增 | `web/src/app/(public)/layout.tsx` | 公开页面布局（顶部导航） |
| 新增 | `web/src/app/(public)/page.tsx` | 首页 |
| 新增 | `web/src/app/(auth)/layout.tsx` | 认证页面布局（顶部导航） |
| 新增 | `web/src/app/(auth)/login/page.tsx` | 登录页 |
| 新增 | `web/src/app/not-found.tsx` | 404 页面 |
| 修改 | `admin/src/app/layout.tsx` | 根布局（侧边导航） |
| 修改 | `admin/src/app/page.tsx` | 首页 |
| 新增 | `admin/src/app/not-found.tsx` | 404 页面 |

**总计**：10 个文件操作（4 修改 + 6 新增）

---

## 核心流程（伪代码）

```
1. Web 应用
   ├── 替换根 layout.tsx（全局容器）
   ├── 替换根 page.tsx（重定向）
   ├── 创建 (public) 路由组
   │   ├── layout.tsx（顶部导航）
   │   └── page.tsx（首页）
   ├── 创建 (auth) 路由组
   │   ├── layout.tsx（顶部导航）
   │   └── login/page.tsx（登录页）
   └── 创建 not-found.tsx

2. Admin 应用
   ├── 替换根 layout.tsx（侧边导航）
   ├── 替换根 page.tsx（首页）
   └── 创建 not-found.tsx

3. 验证
   ├── pnpm -r typecheck
   ├── pnpm --filter web build
   └── pnpm --filter admin build
```

---

## 原子任务清单

### Step 1: 修改 Web 根布局

**文件**：`web/src/app/layout.tsx`

**内容**：简化为全局容器，移除直接内容

```tsx
export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  )
}
```

**验证**：文件更新成功

---

### Step 2: 修改 Web 根页面

**文件**：`web/src/app/page.tsx`

**内容**：重定向到首页（由于 `/` 就是首页，可保持简单组件）

```tsx
export default function HomePage() {
  return (
    <main>
      <h1>AI 研云</h1>
      <p>用户端首页</p>
    </main>
  )
}
```

**验证**：文件更新成功

---

### Step 3: 创建 Web 公开页面布局

**文件**：`web/src/app/(public)/layout.tsx`

**内容**：

```tsx
export default function PublicLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <>
      <header style={{ padding: '1rem', borderBottom: '1px solid #e5e5e5' }}>
        AI 研云
      </header>
      <main>{children}</main>
    </>
  )
}
```

**验证**：文件创建成功

---

### Step 4: 创建 Web 首页

**文件**：`web/src/app/(public)/page.tsx`

**内容**：

```tsx
export default function HomePage() {
  return (
    <main style={{ padding: '2rem' }}>
      <h1>欢迎使用 AI 研云</h1>
      <p>智能对话与创作平台</p>
    </main>
  )
}
```

**验证**：文件创建成功

---

### Step 5: 创建 Web 认证页面布局

**文件**：`web/src/app/(auth)/layout.tsx`

**内容**：

```tsx
export default function AuthLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <>
      <header style={{ padding: '1rem', borderBottom: '1px solid #e5e5e5' }}>
        AI 研云
      </header>
      <main>{children}</main>
    </>
  )
}
```

**验证**：文件创建成功

---

### Step 6: 创建 Web 登录页

**文件**：`web/src/app/(auth)/login/page.tsx`

**内容**：

```tsx
export default function LoginPage() {
  return (
    <main style={{ padding: '2rem' }}>
      <h1>登录</h1>
      <p>登录页面（占位）</p>
    </main>
  )
}
```

**验证**：文件创建成功

---

### Step 7: 创建 Web 404 页面

**文件**：`web/src/app/not-found.tsx`

**内容**：

```tsx
import Link from 'next/link'

export default function NotFound() {
  return (
    <div style={{ padding: '2rem', textAlign: 'center' }}>
      <h1>404</h1>
      <p>页面不存在</p>
      <Link href="/">返回首页</Link>
    </div>
  )
}
```

**验证**：文件创建成功

---

### Step 8: 修改 Admin 根布局

**文件**：`admin/src/app/layout.tsx`

**内容**：

```tsx
export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="zh-CN">
      <body>
        <div style={{ display: 'flex', minHeight: '100vh' }}>
          <aside style={{ width: '240px', padding: '1rem', borderRight: '1px solid #e5e5e5' }}>
            运营后台
          </aside>
          <main style={{ flex: 1 }}>{children}</main>
        </div>
      </body>
    </html>
  )
}
```

**验证**：文件更新成功

---

### Step 9: 修改 Admin 首页

**文件**：`admin/src/app/page.tsx`

**内容**：

```tsx
export default function HomePage() {
  return (
    <main style={{ padding: '2rem' }}>
      <h1>运营后台</h1>
      <p>平台运营管理</p>
    </main>
  )
}
```

**验证**：文件更新成功

---

### Step 10: 创建 Admin 404 页面

**文件**：`admin/src/app/not-found.tsx`

**内容**：

```tsx
import Link from 'next/link'

export default function NotFound() {
  return (
    <div style={{ padding: '2rem', textAlign: 'center' }}>
      <h1>404</h1>
      <p>页面不存在</p>
      <Link href="/">返回首页</Link>
    </div>
  )
}
```

**验证**：文件创建成功

---

### Step 11: 类型检查验证

**命令**：`pnpm -r typecheck`

**验证**：所有包类型检查通过

---

### Step 12: 构建验证

**命令**：
```bash
pnpm --filter web build
pnpm --filter admin build
```

**验证**：两个应用构建成功

---

### Step 13: 路由访问验证（手动）

**命令**：
```bash
# Web 应用
pnpm --filter web dev
# 访问 http://localhost:3000
# 访问 http://localhost:3000/login
# 访问 http://localhost:3000/nonexistent

# Admin 应用
pnpm --filter admin dev
# 访问 http://localhost:3001
# 访问 http://localhost:3001/nonexistent
```

**验证**：所有路由可访问，显示正确内容，404 页面生效

---

## 任务依赖关系

```
Step 1（Web 根 layout）
   │
   ├──→ Step 2（Web 根 page）
   │
   ├──→ Step 3（(public) layout）
   │        │
   │        └──→ Step 4（(public) page）
   │
   ├──→ Step 5（(auth) layout）
   │        │
   │        └──→ Step 6（login page）
   │
   └──→ Step 7（not-found）
            │
            └──→ Step 11（typecheck）

Step 8（Admin 根 layout）
   │
   ├──→ Step 9（Admin page）
   │
   └──→ Step 10（not-found）
            │
            └──→ Step 11（typecheck）

Step 11 → Step 12 → Step 13
```

**并行任务**：Step 1-7（Web）和 Step 8-10（Admin）可并行

**关键路径**：所有文件创建 → Step 11（typecheck）→ Step 12（build）→ Step 13（手动验证）

---

## 验收标准映射

| AC | 对应 Step | 验证方式 |
|----|-----------|----------|
| AC1: Web 路由组结构 | Step 3-6 | ls 命令验证目录 |
| AC2: Web 首页可访问 | Step 2, 4 | 手动访问 / |
| AC3: Web 登录页可访问 | Step 6 | 手动访问 /login |
| AC4: Web 顶部导航占位 | Step 3, 5 | 源码/渲染验证 |
| AC5: Admin 侧边导航占位 | Step 8 | 源码/渲染验证 |
| AC6: 404 页面存在 | Step 7, 10 | 手动访问不存在的路由 |
| AC7: 所有路由无报错 | Step 11-12 | typecheck + build |

---

## 完成检查

- [ ] Step 1: 修改 Web 根布局
- [ ] Step 2: 修改 Web 根页面
- [ ] Step 3: 创建 Web 公开页面布局
- [ ] Step 4: 创建 Web 首页
- [ ] Step 5: 创建 Web 认证页面布局
- [ ] Step 6: 创建 Web 登录页
- [ ] Step 7: 创建 Web 404 页面
- [ ] Step 8: 修改 Admin 根布局
- [ ] Step 9: 修改 Admin 首页
- [ ] Step 10: 创建 Admin 404 页面
- [ ] Step 11: 类型检查通过
- [ ] Step 12: 构建成功
- [ ] Step 13: 路由访问验证（手动）

---

## 预估工作量

| Step | 预估时间 | 说明 |
|------|----------|------|
| Step 1-2 | 5 分钟 | Web 根布局修改 |
| Step 3-4 | 5 分钟 | (public) 路由组 |
| Step 5-6 | 5 分钟 | (auth) 路由组 |
| Step 7 | 3 分钟 | Web 404 |
| Step 8-9 | 5 分钟 | Admin 布局 |
| Step 10 | 3 分钟 | Admin 404 |
| Step 11 | 2 分钟 | typecheck |
| Step 12 | 5 分钟 | build |
| Step 13 | 5 分钟 | 手动验证 |

**总计**：约 40 分钟（单人）

---

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 路由组嵌套问题 | 路由不生效 | 确保 (public)/(auth) 目录名用括号包裹 |
| Link 导入错误 | 构建失败 | 确认 Next.js Link 正确导入 |
| 类型错误 | typecheck 失败 | 确保 React 组件类型定义正确 |
| 样式冲突 | 布局错乱 | 使用简单内联样式，避免全局污染 |
