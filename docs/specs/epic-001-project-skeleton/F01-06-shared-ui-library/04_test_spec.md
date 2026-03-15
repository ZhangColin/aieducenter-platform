# Feature: F01-06 共享 UI 组件库 — 测试规格

> 版本：v1.0 | 日期：2026-03-16
> 状态：Phase 5 归档

---

## 一、测试策略

本 Feature 为配置型 UI 组件库，侧重视觉验证和集成测试，不包含单元测试（留给后续 Epic）。

| 测试类型 | 方式 | 工具 |
|---------|------|------|
| 编译检查 | 构建应用验证 | Next.js / TypeScript |
| 类型检查 | tsc --noEmit | TypeScript |
| 视觉验证 | 浏览器渲染 | 手动 |
| 主题切换 | 亮/暗模式切换 | next-themes |

---

## 二、验收用例

| AC | 用例 | 验证方式 | 状态 |
|----|------|---------|------|
| AC1 | shadcn init 生成配置文件 | 检查 components.json、tailwind.config.ts | ✅ |
| AC2 | shadcn add 安装 6 个组件 | 检查 src/components/ 目录 | ✅ |
| AC3 | @aieducenter/ui 导出组件 | `import { Button } from '@aieducenter/ui'` | ✅ |
| AC4 | web 引用组件成功 | pnpm --filter web typecheck | ✅ |
| AC5 | web 页面渲染正常 | 访问 /showcase | ✅ |
| AC6 | admin 引用组件成功 | pnpm --filter admin typecheck | ✅ |
| AC7 | 主题切换功能 | ThemeProvider + useTheme | ✅ |
| AC8 | 亮/暗样式正确 | CSS 变量验证 | ✅ |

---

## 三、测试场景

### 3.1 组件导出

```typescript
// 验证所有组件可导入
import {
  Button, Input, Label,
  Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter,
  Dialog, DialogPortal, DialogOverlay, DialogTrigger, DialogClose,
  DialogContent, DialogHeader, DialogFooter, DialogTitle, DialogDescription,
  Table, TableHeader, TableBody, TableFooter, TableHead, TableRow, TableCell,
  ThemeProvider, useTheme
} from '@aieducenter/ui'
```

### 3.2 按钮变体

| Variant | 预期样式 |
|---------|---------|
| default | 深色背景，白色文字 |
| destructive | 红色背景 |
| outline | 边框样式 |
| secondary | 灰色背景 |
| ghost | 悬停时背景变化 |
| link | 下划线样式 |

### 3.3 主题切换

```typescript
// 切换前：html class="light" 或无 class
// 切换后：html class="dark"

// CSS 变量应正确变化：
// --background: 0 0% 100% → 0 0% 3.9%
// --foreground: 0 0% 3.9% → 0 0% 98%
```

---

## 四、手动测试步骤

### 4.1 启动应用

```bash
pnpm dev:web   # http://localhost:3000
pnpm dev:admin # http://localhost:3001
```

### 4.2 验证页面

1. 访问 http://localhost:3000/showcase
2. 检查组件是否正确渲染
3. 点击"切换主题"按钮
4. 验证亮/暗模式切换

---

## 五、已知限制

- 无单元测试覆盖
- 无 E2E 测试
- 无可访问性测试（axe-core）
- 主题切换依赖 next-themes，SSR 场景需注意 hydration

---

## 六、后续改进

- [ ] 添加 Jest + Testing Library 单元测试
- [ ] 添加 Playwright E2E 测试
- [ ] 添加 axe-core 可访问性测试
- [ ] 添加 Storybook 组件文档
