# Feature: F01-06 共享 UI 组件库

> 版本：v1.0 | 日期：2026-03-16
> 状态：Phase 1 完成
> Epic: Epic 1 项目骨架

---

## 背景

在 F01-04 中已经搭建了前端 Monorepo 基础结构，包括 web、admin 和 packages/ui。现在需要在 packages/ui 中建立可复用的 UI 组件库，避免在 web 和 admin 中重复编写相同的组件代码，同时为后续 Epic 提供一致的 UI 基础设施。

shadcn/ui 是一个基于 Radix UI 和 Tailwind CSS 的组件集合，采用"复制到项目"的模式，适合作为共享组件库的基础。

---

## 目标

- 创建基于 shadcn/ui 的共享组件库 `@aieducenter/ui`
- 提供 6 个基础组件：Button、Input、Label、Card、Dialog、Table
- 实现主题切换能力（亮色/暗色模式）
- web 和 admin 可以直接引用共享组件

---

## 范围

### 包含（In Scope）

- 在 packages/ui 中初始化 shadcn/ui
- 安装并导出 6 个 shadcn 基础组件
- 配置 New York 风格的主题系统
- 实现 ThemeProvider 组件（基于 next-themes）
- 在 packages/shared 中添加 cn 工具函数
- 配置 Tailwind CSS 和 PostCSS

### 不包含（Out of Scope）

- 单元测试（留给后续 Epic）
- E2E 测试（留给后续 Epic）
- 可访问性测试（留给后续 Epic）
- 自定义组件（非 shadcn 提供）
- 组件封装层（直接使用 shadcn 组件）

---

## 验收标准（Acceptance Criteria）

- **AC1**: 在 packages/ui 下执行 shadcn init，成功生成 components.json 和配置文件
- **AC2**: 在 packages/ui 下执行 shadcn add 命令，成功添加 6 个组件
- **AC3**: web 应用可以 `import { Button } from '@aieducenter/ui'`，类型检查通过
- **AC4**: admin 应用可以 `import { Button } from '@aieducenter/ui'`，类型检查通过
- **AC5**: web 中创建示例页面，组件渲染正常，样式符合 New York 风格
- **AC6**: 切换主题（亮色/暗色），组件样式正确变化

---

## 约束

- **风格**: 必须使用 shadcn/ui 的 New York 风格
- **导出方式**: 扁平导出，所有组件从 `@aieducenter/ui` 主入口导出
- **工具函数**: cn 函数实现在 packages/shared，packages/ui 通过依赖引用
- **peerDependencies**: react 和 react-dom 由 web/admin 提供，packages/ui 不直接依赖
- **CSS**: 使用 CSS 变量实现主题，支持亮色和暗色模式

---

## 依赖

- **F01-04**: 前端 Monorepo 搭建（已完成）
  - pnpm-workspace.yaml 配置
  - packages/ui 目录存在
  - web/admin 已配置对 @aieducenter/ui 的依赖

---

## 技术选型

| 技术 | 版本 | 用途 |
|------|------|------|
| shadcn/ui | latest | 组件基础 |
| next-themes | latest | 主题切换 |
| clsx | latest | className 合并 |
| tailwind-merge | latest | Tailwind 类名合并 |
| class-variance-authority | latest | 组件变体管理 |
