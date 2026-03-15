# Feature: F01-06 共享 UI 组件库 — 实施计划

> 版本：v1.0 | 日期：2026-03-16
> 状态：Phase 3 完成

---

## 目标复述

在 packages/ui 中初始化 shadcn/ui，安装并导出 6 个基础组件（Button、Input、Label、Card、Dialog、Table），配置 New York 风格的主题系统，实现 ThemeProvider，确保 web 和 admin 可以引用共享组件。

---

## 变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新增 | packages/shared/src/utils/cn.ts | cn 工具函数实现 |
| 修改 | packages/shared/src/index.ts | 导出 cn 函数 |
| 修改 | packages/shared/package.json | 新增 clsx、tailwind-merge 依赖 |
| 修改 | packages/ui/package.json | 新增 shadcn 相关依赖 |
| 新增 | packages/ui/components.json | shadcn 配置 |
| 新增 | packages/ui/tailwind.config.ts | Tailwind 配置 |
| 新增 | packages/ui/postcss.config.mjs | PostCSS 配置 |
| 新增 | packages/ui/src/index.css | 全局样式 + CSS 变量 |
| 新增 | packages/ui/src/lib/utils.ts | re-export cn 函数 |
| 新增 | packages/ui/src/theme/provider.tsx | ThemeProvider 组件 |
| 新增 | packages/ui/src/theme/index.ts | 主题相关导出 |
| 新增 | packages/ui/src/components/button/ | Button 组件 |
| 新增 | packages/ui/src/components/input/ | Input 组件 |
| 新增 | packages/ui/src/components/label/ | Label 组件 |
| 新增 | packages/ui/src/components/card/ | Card 组件组 |
| 新增 | packages/ui/src/components/dialog/ | Dialog 组件组 |
| 新增 | packages/ui/src/components/table/ | Table 组件组 |
| 修改 | packages/ui/src/index.ts | 扁平导出所有组件 |
| 新增 | web/src/app/showcase/page.tsx | 组件展示页面（验证用） |
| 修改 | web/src/app/layout.tsx | 引入 ThemeProvider 和样式 |

---

## 核心流程（伪代码）

1. **安装依赖**
   - packages/shared 安装 clsx、tailwind-merge
   - packages/ui 安装 class-variance-authority、next-themes

2. **实现 cn 工具函数**
   - packages/shared/src/utils/cn.ts: 实现 className 合并逻辑

3. **初始化 shadcn**
   - 在 packages/ui 下执行 shadcn init
   - 选择 New York 风格、CSS 变量模式

4. **安装组件**
   - 在 packages/ui 下执行 shadcn add button input label card dialog table

5. **配置导出**
   - 创建 src/index.ts，扁平导出所有组件
   - 创建 theme/provider.tsx 和 theme/index.ts

6. **验证**
   - web 中创建展示页面
   - 引入 ThemeProvider 和样式

---

## 原子任务清单

### Step 1: 更新 packages/shared

**文件**: `packages/shared/package.json`
- 在 dependencies 中新增：
  - `"clsx": "^2.1.0"`
  - `"tailwind-merge": "^2.2.0"`

**文件**: `packages/shared/src/utils/cn.ts`
```typescript
import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
```

**文件**: `packages/shared/src/index.ts`
- 新增导出：`export { cn } from './utils/cn'`

**验证**: `pnpm --filter shared typecheck` 通过

---

### Step 2: 配置 packages/ui 依赖

**文件**: `packages/ui/package.json`
- 更新 dependencies：
  ```json
  {
    "@aieducenter/shared": "workspace:*",
    "class-variance-authority": "^0.7.1",
    "next-themes": "^0.4.6"
  }
  ```
- 更新 devDependencies：
  ```json
  {
    "tailwindcss": "^3.4.0",
    "postcss": "^8.4.0",
    "autoprefixer": "^10.4.0",
    "tailwindcss-animate": "^1.0.7"
  }
  ```

**验证**: `pnpm install` 无错误

---

### Step 3: 初始化 shadcn/ui

**在 packages/ui 目录下执行**：
```bash
pnpm dlx shadcn@latest init
```

**交互式选择**：
- Style: New York
- Base color: Neutral
- CSS variables: Yes
- Tailwind config: tailwind.config.ts
- Components path: src/components
- Utils path: src/lib
- rsc: No

**生成文件**：
- `components.json`
- `tailwind.config.ts`
- `postcss.config.mjs`
- `src/index.css`
- `src/lib/utils.ts`

**验证**: 文件已生成，且 `src/lib/utils.ts` 内容为：
```typescript
import { cn } from "@aieducenter/shared"
export { cn }
```

---

### Step 4: 安装 6 个组件

**在 packages/ui 目录下执行**：
```bash
pnpm dlx shadcn@latest add button input label card dialog table
```

**生成目录结构**：
```
src/components/
├── button/
│   └── button.tsx
├── input/
│   └── input.tsx
├── label/
│   └── label.tsx
├── card/
│   ├── card.tsx
│   └── index.ts
├── dialog/
│   └── dialog.tsx
└── table/
    └── table.tsx
```

**验证**: 组件文件已生成，无报错

---

### Step 5: 创建主题模块

**文件**: `packages/ui/src/theme/provider.tsx`
```tsx
'use client'

import { ThemeProvider as NextThemesProvider } from 'next-themes'
import type { ThemeProviderProps } from 'next-themes'

export function ThemeProvider({ children, ...props }: ThemeProviderProps) {
  return <NextThemesProvider {...props}>{children}</NextThemesProvider>
}
```

**文件**: `packages/ui/src/theme/index.ts`
```ts
export { ThemeProvider } from './provider'
export { useTheme } from 'next-themes'
export { type ThemeProviderProps } from 'next-themes'
```

**验证**: `pnpm --filter "@aieducenter/ui" typecheck` 通过

---

### Step 6: 配置主入口导出

**文件**: `packages/ui/src/index.ts`
```ts
// Theme
export { ThemeProvider, useTheme, type ThemeProviderProps } from './theme'

// Components
export { Button } from './components/button'
export { Input } from './components/input'
export { Label } from './components/label'
export {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter
} from './components/card'
export {
  Dialog,
  DialogPortal,
  DialogOverlay,
  DialogTrigger,
  DialogClose,
  DialogContent,
  DialogHeader,
  DialogFooter,
  DialogTitle,
  DialogDescription
} from './components/dialog'
export {
  Table,
  TableHeader,
  TableBody,
  TableFooter,
  TableHead,
  TableRow,
  TableCell
} from './components/table'
```

**验证**:
- `pnpm --filter web typecheck` 无错误
- 可以 `import { Button } from '@aieducenter/ui'`

---

### Step 7: web 应用集成

**文件**: `web/src/app/layout.tsx`
```tsx
import { ThemeProvider } from '@aieducenter/ui'
import '@aieducenter/ui/src/index.css'

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN" suppressHydrationWarning>
      <body>
        <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
          {children}
        </ThemeProvider>
      </body>
    </html>
  )
}
```

**文件**: `web/src/app/showcase/page.tsx`（新建）
```tsx
import {
  Button,
  Input,
  Label,
  Card,
  CardHeader,
  CardTitle,
  CardContent
} from '@aieducenter/ui'

export default function ShowcasePage() {
  return (
    <div className="container mx-auto py-8">
      <Card>
        <CardHeader>
          <CardTitle>组件展示</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <Label>输入框</Label>
            <Input placeholder="测试输入" />
          </div>
          <div className="flex gap-2">
            <Button>默认按钮</Button>
            <Button variant="outline">轮廓按钮</Button>
            <Button variant="secondary">次要按钮</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

**验证**:
- `pnpm --filter web build` 成功
- 访问 http://localhost:3000/showcase 页面正常显示

---

### Step 8: 主题切换验证

**修改**: `web/src/app/showcase/page.tsx`
```tsx
import { useTheme } from '@aieducenter/ui'
import { Button } from '@aieducenter/ui'

// 在组件中添加
const { theme, setTheme } = useTheme()

// 添加切换按钮
<Button onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}>
  切换主题 ({theme})
</Button>
```

**验证**:
- 点击按钮，主题在亮/暗之间切换
- 组件样式随主题正确变化

---

### Step 9: admin 应用集成

**文件**: `admin/src/app/layout.tsx`
- 与 web 相同的集成方式

**文件**: `admin/src/app/showcase/page.tsx`（新建）
- 与 web 相同的展示页面

**验证**:
- `pnpm --filter admin build` 成功
- 访问 http://localhost:3001/showcase 页面正常显示

---

### Step 10: 全量验证

**执行**:
```bash
pnpm build
```

**预期结果**:
- web 构建成功
- admin 构建成功

**执行**:
```bash
pnpm -r typecheck
```

**预期结果**:
- 所有包类型检查通过

---

## 注意事项

1. **shadcn 命令位置**: 所有 shadcn 命令必须在 `packages/ui` 目录下执行
2. **样式引入**: web/admin 的根 layout 必须引入 `@aieducenter/ui/src/index.css`
3. **suppressHydrationWarning**: html 标签需要此属性以避免 next-themes 的 hydration 警告
4. **'use client'**: ThemeProvider 必须是客户端组件
5. **组件类型**: 确保每个组件正确导出 TypeScript 类型

---

## 预估时间

| Step | 预估时间 |
|------|---------|
| Step 1-2 | 10 分钟 |
| Step 3-4 | 15 分钟 |
| Step 5-6 | 10 分钟 |
| Step 7-8 | 15 分钟 |
| Step 9 | 10 分钟 |
| Step 10 | 10 分钟 |
| **总计** | **约 70 分钟** |
