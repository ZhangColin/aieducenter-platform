# Feature: F01-04 前端 Monorepo 搭建

> 版本：v1.0 | 日期：2026-03-16
> 状态：Phase 1 完成

---

## 背景

项目需要创建前端 Monorepo 结构，支持两个 Next.js 应用（web 用户端、admin 管理后台）和三个共享包（ui、api-client、shared）。这是前端开发的基础设施，为后续 F01-05（路由结构）、F01-06（UI 组件库）、F01-07（API Client 生成）提供可运行的前端环境。

当前项目后端骨架（F01-01）已完成，前端部分完全空白，需要从零开始搭建。

---

## 目标

- 创建 pnpm workspace Monorepo 结构
- 配置 web 和 admin 两个可独立运行的 Next.js 15 应用
- 创建 packages 下三个共享包的空壳占位（shadcn/ui 留到 F01-06）
- 统一 TypeScript 配置，支持 paths 别名引用内部包
- 所有包可通过 typecheck，应用可完整构建

---

## 范围

### 包含（In Scope）

- pnpm-workspace.yaml 配置
- 根目录 package.json（统一 scripts）
- 根目录 tsconfig.base.json（共享 TypeScript 配置 + paths 别名）
- web/ 应用：Next.js 15 + App Router + TypeScript
- admin/ 应用：Next.js 15 + App Router + TypeScript
- packages/ui/：空壳占位，仅包结构
- packages/api-client/：空壳占位，仅包结构
- packages/shared/：空壳占位，仅包结构
- .npmrc：pnpm 配置
- workspace:* 协议引用内部包
- transpilePackages 配置（Next.js 转译 workspace 包）

### 不包含（Out of Scope）

- shadcn/ui 初始化（留到 F01-06）
- Tailwind CSS 配置（留到 F01-06）
- 路由结构实现（留到 F01-05）
- OpenAPI 客户端生成（留到 F01-07）
- 生产部署配置（Docker、standalone 模式等）
- 跨平台兼容性优化（concurrently、rimraf 等）

---

## 验收标准（Acceptance Criteria）

### AC1: Workspace 配置正确
- **验收方式**：`cat pnpm-workspace.yaml`
- **期望**：文件包含 `packages: ["web", "admin", "packages/*"]` 或等效配置

### AC2: 共享包目录存在
- **验收方式**：`ls packages/`
- **期望**：可见 `ui`、`api-client`、`shared` 三个目录

### AC3: Web 应用可独立运行
- **验收方式**：`pnpm --filter web dev`
- **期望**：开发服务器在 3000 端口启动，无报错

### AC4: Admin 应用可独立运行
- **验收方式**：`pnpm --filter admin dev`
- **期望**：开发服务器在 3001 端口启动，无报错

### AC5: 所有包通过类型检查
- **验收方式**：`pnpm -r typecheck`
- **期望**：所有包类型检查通过，无错误

### AC6: 应用可完整构建
- **验收方式**：`pnpm --filter web build && pnpm --filter admin build`
- **期望**：构建成功，生成 `.next` 目录

### AC7: TypeScript paths 配置有效
- **验收方式**：
  1. 查看 `web/tsconfig.json` 确认为 `extends: "../tsconfig.base.json"`
  2. `tsconfig.base.json` 包含 `@aieducenter/*` paths 定义
- **期望**：配置继承关系正确，paths 指向 `packages/*/src`

---

## 约束

### 技术约束
- Next.js 版本：15.x（App Router）
- React 版本：19.x（Next.js 15 内置）
- Node.js 版本：>= 20.0.0
- pnpm 版本：>= 9.0.0
- TypeScript 版本：5.x

### 架构约束
- 使用 workspace:* 协议引用内部包
- TypeScript 配置通过 tsconfig.base.json 统一管理
- web/admin 端口：3000 / 3001
- packages/* 为空壳占位，不引入 shadcn/ui 等依赖

### 性能约束
- typecheck 时间 < 30 秒（冷启动）
- dev 启动时间 < 10 秒
- build 时间 < 60 秒

---

## 依赖关系

- **无依赖**：F01-04 可独立于后端 Feature 并行开发
- **被依赖**：F01-05（路由结构）、F01-06（UI 组件库）、F01-07（API Client）依赖本 Feature
