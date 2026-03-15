# Feature: F01-06 共享 UI 组件库 — 接口契约

> 版本：v1.0 | 日期：2026-03-16
> 状态：Phase 2 完成

---

## 一、包结构

### 1.1 packages/ui

**package.json 配置**：
```json
{
  "name": "@aieducenter/ui",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "exports": {
    ".": "./src/index.ts"
  },
  "scripts": {
    "typecheck": "tsc --noEmit"
  },
  "peerDependencies": {
    "react": "^19.0.0",
    "react-dom": "^19.0.0"
  },
  "dependencies": {
    "@aieducenter/shared": "workspace:*",
    "class-variance-authority": "^0.7.1",
    "next-themes": "^0.4.6"
  },
  "devDependencies": {
    "typescript": "^5.0.0",
    "tailwindcss": "^3.4.0",
    "postcss": "^8.4.0",
    "autoprefixer": "^10.4.0"
  }
}
```

### 1.2 packages/shared

**新增依赖**：
```json
{
  "dependencies": {
    "clsx": "^2.1.0",
    "tailwind-merge": "^2.2.0"
  }
}
```

**导出接口**：
```typescript
// packages/shared/src/utils/cn.ts
export function cn(...classes: (string | undefined | null | false)[]): string

// packages/shared/src/index.ts
export { cn } from './utils/cn'
```

---

## 二、组件导出接口

### 2.1 主题接口

```typescript
// packages/ui/src/theme/provider.tsx
export interface ThemeProviderProps {
  children: React.ReactNode
  attribute?: string
  defaultTheme?: string
  enableSystem?: boolean
  disableTransitionOnChange?: boolean
}

export function ThemeProvider(props: ThemeProviderProps): React.JSX.Element

// packages/ui/src/theme/index.ts
export { ThemeProvider } from './provider'
export { useTheme } from 'next-themes'
export { type ThemeProviderProps } from './provider'
```

### 2.2 Button 组件

```typescript
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link'
  size?: 'default' | 'sm' | 'lg' | 'icon'
  asChild?: boolean
}

export const Button: React.FC<ButtonProps>
```

### 2.3 Input 组件

```typescript
interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
}

export const Input: React.FC<InputProps>
```

### 2.4 Label 组件

```typescript
interface LabelProps extends React.LabelHTMLAttributes<HTMLLabelElement> {
}

export const Label: React.FC<LabelProps>
```

### 2.5 Card 组件组

```typescript
interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
}

interface CardHeaderProps extends React.HTMLAttributes<HTMLDivElement> {
}

interface CardTitleProps extends React.HTMLAttributes<HTMLHeadingElement> {
}

interface CardDescriptionProps extends React.HTMLAttributes<HTMLParagraphElement> {
}

interface CardContentProps extends React.HTMLAttributes<HTMLDivElement> {
}

interface CardFooterProps extends React.HTMLAttributes<HTMLDivElement> {
}

export const Card: React.FC<CardProps>
export const CardHeader: React.FC<CardHeaderProps>
export const CardTitle: React.FC<CardTitleProps>
export const CardDescription: React.FC<CardDescriptionProps>
export const CardContent: React.FC<CardContentProps>
export const CardFooter: React.FC<CardFooterProps>
```

### 2.6 Dialog 组件组

```typescript
interface DialogProps {
  open?: boolean
  onOpenChange?: (open: boolean) => void
  children: React.ReactNode
}

interface DialogContentProps extends React.HTMLAttributes<HTMLDivElement> {
}

interface DialogHeaderProps extends React.HTMLAttributes<HTMLDivElement> {
}

interface DialogTitleProps extends React.HTMLAttributes<HTMLHeadingElement> {
}

interface DialogDescriptionProps extends React.HTMLAttributes<HTMLParagraphElement> {
}

interface DialogFooterProps extends React.HTMLAttributes<HTMLDivElement> {
}

export const Dialog: React.FC<DialogProps>
export const DialogPortal: React.FC<{ children: React.ReactNode }>
export const DialogOverlay: React.FC<React.HTMLAttributes<HTMLDivElement>>
export const DialogTrigger: React.FC<React.ButtonHTMLAttributes<HTMLButtonElement>>
export const DialogClose: React.FC<React.ButtonHTMLAttributes<HTMLButtonElement>>
export const DialogContent: React.FC<DialogContentProps>
export const DialogHeader: React.FC<DialogHeaderProps>
export const DialogFooter: React.FC<DialogFooterProps>
export const DialogTitle: React.FC<DialogTitleProps>
export const DialogDescription: React.FC<DialogDescriptionProps>
```

### 2.7 Table 组件组

```typescript
interface TableProps extends React.HTMLAttributes<HTMLTableElement> {
}

interface TableHeaderProps extends React.HTMLAttributes<HTMLTableSectionElement> {
}

interface TableBodyProps extends React.HTMLAttributes<HTMLTableSectionElement> {
}

interface TableFooterProps extends React.HTMLAttributes<HTMLTableSectionElement> {
}

interface TableRowProps extends React.HTMLAttributes<HTMLTableRowElement> {
}

interface TableHeadProps extends React.ThHTMLAttributes<HTMLTableCellElement> {
}

interface TableCellProps extends React.TdHTMLAttributes<HTMLTableCellElement> {
}

export const Table: React.FC<TableProps>
export const TableHeader: React.FC<TableHeaderProps>
export const TableBody: React.FC<TableBodyProps>
export const TableFooter: React.FC<TableFooterProps>
export const TableRow: React.FC<TableRowProps>
export const TableHead: React.FC<TableHeadProps>
export const TableCell: React.FC<TableCellProps>
```

---

## 三、主入口导出

```typescript
// packages/ui/src/index.ts
export { ThemeProvider, useTheme, type ThemeProviderProps } from './theme'
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

---

## 四、样式接口

### 4.1 CSS 变量

使用 CSS 自定义属性实现主题，变量命名遵循 shadcn 约定：

```css
/* 亮色主题 */
--background
--foreground
--card
--card-foreground
--popover
--popover-foreground
--primary
--primary-foreground
--secondary
--secondary-foreground
--muted
--muted-foreground
--accent
--accent-foreground
--destructive
--destructive-foreground
--border
--input
--ring
--radius

/* 暗色主题使用相同变量名，不同值 */
```

### 4.2 主题切换机制

- 使用 `class` 属性切换：`<html class="dark">`
- next-themes 提供的 useTheme hook 获取当前主题
- ThemeProvider 必须包裹在应用根组件外层

---

## 五、配置文件

### 5.1 components.json

```json
{
  "$schema": "https://ui.shadcn.com/schema.json",
  "style": "new-york",
  "rsc": false,
  "tsx": true,
  "tailwind": {
    "config": "tailwind.config.ts",
    "css": "src/index.css",
    "baseColor": "neutral",
    "cssVariables": true
  },
  "aliases": {
    "components": "@/components",
    "utils": "@/lib",
    "ui": "@/components"
  }
}
```

### 5.2 tailwind.config.ts

```typescript
import type { Config } from 'tailwindcss'

const config: Config = {
  darkMode: ['class'],
  content: [
    './src/**/*.{ts,tsx}',
  ],
  theme: {
    extend: {
      borderRadius: {
        lg: 'var(--radius)',
        md: 'calc(var(--radius) - 2px)',
        sm: 'calc(var(--radius) - 4px)',
      },
      colors: {
        background: 'hsl(var(--background))',
        foreground: 'hsl(var(--foreground))',
        // ... 其他颜色变量映射
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
}

export default config
```

---

## 六、使用示例

### 6.1 在 web/admin 中引入样式

```tsx
// web/src/app/layout.tsx 或 admin/src/app/layout.tsx
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

### 6.2 使用组件

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

export function LoginForm() {
  return (
    <Card>
      <CardHeader>
        <CardTitle>登录</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div>
          <Label htmlFor="username">用户名</Label>
          <Input id="username" placeholder="输入用户名" />
        </div>
        <Button className="w-full">登录</Button>
      </CardContent>
    </Card>
  )
}
```

### 6.3 主题切换

```tsx
import { useTheme } from '@aieducenter/ui'
import { Button } from '@aieducenter/ui'

export function ThemeToggle() {
  const { theme, setTheme } = useTheme()

  return (
    <Button onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}>
      切换主题
    </Button>
  )
}
```

---

## 七、错误处理

本 Feature 为纯 UI 组件库，无业务逻辑错误处理。错误处理由使用方（web/admin）负责。
