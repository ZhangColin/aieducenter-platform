# Feature: F01-05 前端路由结构与基础布局 — 接口契约

> 版本：v1.0 | 日期：2026-03-16
> 状态：Phase 2 完成

---

## 一、文件结构接口

### 1.1 Web 应用目录结构

```
web/src/app/
├── layout.tsx                 # 根布局（替换现有）
├── page.tsx                   # 根重定向（替换现有）
├── not-found.tsx              # 404 页面
├── globals.css                # 全局样式（保持现有）
├── (public)/                  # 路由组：公开页面
│   ├── layout.tsx             # 公开页面布局
│   └── page.tsx               # 首页
└── (auth)/                    # 路由组：认证页面
    ├── layout.tsx             # 认证页面布局
    └── login/
        └── page.tsx           # 登录页
```

### 1.2 Admin 应用目录结构

```
admin/src/app/
├── layout.tsx                 # 根布局（替换现有，含侧边导航）
├── page.tsx                   # 首页（替换现有）
├── not-found.tsx              # 404 页面
└── globals.css                # 全局样式（保持现有）
```

---

## 二、组件接口规范

### 2.1 根布局（Web）

**文件**：`web/src/app/layout.tsx`

```tsx
// 伪代码
interface RootLayoutProps {
  children: React.ReactNode
}

// 组件结构
<html lang="zh-CN">
  <body>
    {children}
  </body>
</html>
```

**职责**：
- 提供全局 HTML 容器
- 引入全局样式
- 不包含导航（由路由组布局添加）

---

### 2.2 公开页面布局（Web）

**文件**：`web/src/app/(public)/layout.tsx`

```tsx
// 伪代码
interface PublicLayoutProps {
  children: React.ReactNode
}

// 组件结构
<div>
  <header className="top-nav">AI 研云（顶部导航占位）</header>
  <main>{children}</main>
</div>
```

**职责**：
- 继承根布局
- 添加顶部导航占位
- 渲染子页面

---

### 2.3 认证页面布局（Web）

**文件**：`web/src/app/(auth)/layout.tsx`

```tsx
// 伪代码
interface AuthLayoutProps {
  children: React.ReactNode
}

// 组件结构
<div>
  <header className="top-nav">AI 研云（顶部导航占位）</header>
  <main>{children}</main>
</div>
```

**职责**：
- 继承根布局
- 添加顶部导航占位
- 渲染子页面

---

### 2.4 根布局（Admin）

**文件**：`admin/src/app/layout.tsx`

```tsx
// 伪代码
interface RootLayoutProps {
  children: React.ReactNode
}

// 组件结构
<html lang="zh-CN">
  <body>
    <div className="admin-container">
      <aside className="sidebar">运营后台（侧边导航占位）</aside>
      <main>{children}</main>
    </div>
  </body>
</html>
```

**职责**：
- 提供全局 HTML 容器
- 包含侧边导航占位
- 使用 flex 或 grid 布局分隔侧边和主内容

---

### 2.5 404 页面组件

**文件**：`web/src/app/not-found.tsx` / `admin/src/app/not-found.tsx`

```tsx
// 伪代码
// 组件结构
<div>
  <h1>404</h1>
  <p>页面不存在</p>
  <a href="/">返回首页</a>
</div>
```

**职责**：
- Next.js Not Found 处理
- 显示友好的错误信息
- 提供返回首页的链接

---

## 三、路由映射

### 3.1 Web 应用路由

| 路由 | 文件 | 说明 |
|------|------|------|
| `/` | `app/(public)/page.tsx` | 首页 |
| `/login` | `app/(auth)/login/page.tsx` | 登录页 |
| `/nonexistent` | `app/not-found.tsx` | 404 页面 |

**路由组行为**：
- `(public)` 和 `(auth)` 不出现在 URL 中
- 路由组内的 layout 会自动包裹该组下的页面

### 3.2 Admin 应用路由

| 路由 | 文件 | 说明 |
|------|------|------|
| `/` | `app/page.tsx` | 首页 |
| `/nonexistent` | `app/not-found.tsx` | 404 页面 |

---

## 四、样式接口

### 4.1 占位导航样式

使用简单的 className 或内联样式：

```tsx
// 顶部导航（Web）
<header style={{ padding: '1rem', borderBottom: '1px solid #e5e5e5' }}>
  AI 研云
</header>

// 侧边导航（Admin）
<aside style={{ width: '240px', padding: '1rem', borderRight: '1px solid #e5e5e5' }}>
  运营后台
</aside>

// 容器布局（Admin）
<div style={{ display: 'flex', minHeight: '100vh' }}>
  <aside>...</aside>
  <main style={{ flex: 1 }}>{children}</main>
</div>
```

### 4.2 全局样式

保持 F01-04 的 `globals.css`（reset 样式）：

```css
* {
  box-sizing: border-box;
  padding: 0;
  margin: 0;
}

html, body {
  max-width: 100vw;
  overflow-x: hidden;
}

body {
  min-height: 100vh;
}
```

---

## 五、文件清单

| 应用 | 文件 | 操作 | 行数预估 |
|------|------|------|----------|
| web | `app/layout.tsx` | 修改 | ~15 |
| web | `app/page.tsx` | 修改 | ~5 |
| web | `app/(public)/layout.tsx` | 新增 | ~20 |
| web | `app/(public)/page.tsx` | 新增 | ~10 |
| web | `app/(auth)/layout.tsx` | 新增 | ~20 |
| web | `app/(auth)/login/page.tsx` | 新增 | ~10 |
| web | `app/not-found.tsx` | 新增 | ~15 |
| admin | `app/layout.tsx` | 修改 | ~30 |
| admin | `app/page.tsx` | 修改 | ~10 |
| admin | `app/not-found.tsx` | 新增 | ~15 |

**总计**：10 个文件，约 150 行代码

---

## 六、验证命令

| 验收项 | 命令 | 期望输出 |
|--------|------|----------|
| 路由组存在 | `ls web/src/app/\(public\)/` | layout.tsx, page.tsx |
| 类型检查 | `pnpm -r typecheck` | 全部通过 |
| Web 构建 | `pnpm --filter web build` | 成功 |
| Admin 构建 | `pnpm --filter admin build` | 成功 |

---

## 七、技术决策记录

### 决策 1：路由组仅包含必要页面

**方案**：`(public)` 仅首页，`(auth)` 仅登录页

**理由**：
- 按需创建路由，不预建空页面
- 符合 YAGNI 原则
- 后续业务 Feature 可按需添加路由

---

### 决策 2：导航栏使用占位组件

**方案**：使用简单 div + 内联样式，不引入 UI 组件库

**理由**：
- F01-06 将引入 shadcn/ui，届时可替换
- 当前只需验证路由结构可访问
- 避免过早依赖

---

### 决策 3：404 页面使用 Next.js not-found.tsx

**方案**：使用 App Router 的 `not-found.tsx` 约定

**理由**：
- Next.js 15 标准做法
- 自动匹配所有未定义路由
- 支持 file:// 和 href:// 跳转

---

### 决策 4：Admin 使用侧边布局

**方案**：根布局包含 flex 容器，侧边 + 主内容

**理由**：
- 后台管理系统常见布局
- 为后续菜单扩展预留空间
- 与 web 应用顶部导航区分

---

## 八、注意事项

### 8.1 Next.js 路由组

- 路由组目录用括号包裹：`(public)`、`(auth)`
- 路由组名不会出现在 URL 中
- 路由组内的 layout 会自动包裹该组下的所有页面

### 8.2 404 页面

- `not-found.tsx` 放在 `app/` 目录下
- 当访问不存在的路由时自动渲染
- 可使用 `Link` 或 `<a>` 标签返回首页

### 8.3 样式隔离

- 路由组 layout 中的样式只作用于该组内页面
- 根布局样式作用于所有页面
- `globals.css` 在根布局中引入
