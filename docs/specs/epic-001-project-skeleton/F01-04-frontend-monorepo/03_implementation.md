# Feature: F01-04 前端 Monorepo 搭建 — 实施计划

> 版本：v1.0 | 日期：2026-03-16
> 状态：Phase 3 完成

---

## 目标复述

搭建前端 Monorepo 结构，包含 web（用户端）和 admin（管理后台）两个 Next.js 15 应用，以及 packages 下 ui、api-client、shared 三个共享包空壳。配置 pnpm workspace、统一 TypeScript 配置（paths 别名）、使所有包可 typecheck、应用可独立运行和构建。

---

## 变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 创建 | `pnpm-workspace.yaml` | Workspace 配置 |
| 创建 | `package.json` | 根目录 scripts |
| 创建 | `tsconfig.base.json` | 共享 TS 配置 |
| 创建 | `.npmrc` | pnpm 配置 |
| 创建 | `web/package.json` | Web 应用依赖 |
| 创建 | `web/tsconfig.json` | Web TS 配置 |
| 创建 | `web/next.config.ts` | Next.js 配置 |
| 创建 | `web/src/app/layout.tsx` | 根布局 |
| 创建 | `web/src/app/page.tsx` | 首页 |
| 创建 | `web/src/app/globals.css` | 全局样式 |
| 创建 | `admin/package.json` | Admin 应用依赖 |
| 创建 | `admin/tsconfig.json` | Admin TS 配置 |
| 创建 | `admin/next.config.ts` | Next.js 配置 |
| 创建 | `admin/src/app/layout.tsx` | 根布局 |
| 创建 | `admin/src/app/page.tsx` | 首页 |
| 创建 | `admin/src/app/globals.css` | 全局样式 |
| 创建 | `packages/ui/package.json` | UI 包配置 |
| 创建 | `packages/ui/tsconfig.json` | UI TS 配置 |
| 创建 | `packages/ui/src/index.ts` | 导出入口 |
| 创建 | `packages/api-client/package.json` | API 包配置 |
| 创建 | `packages/api-client/tsconfig.json` | API TS 配置 |
| 创建 | `packages/api-client/src/index.ts` | 导出入口 |
| 创建 | `packages/shared/package.json` | Shared 包配置 |
| 创建 | `packages/shared/tsconfig.json` | Shared TS 配置 |
| 创建 | `packages/shared/src/index.ts` | 导出入口 |
| 修改 | `.gitignore` | 添加前端忽略规则 |

**总计**：25 个文件操作（24 创建 + 1 修改）

---

## 核心流程（伪代码）

```
1. 创建根配置文件
   ├── pnpm-workspace.yaml（定义 workspace 成员）
   ├── package.json（定义 scripts）
   ├── tsconfig.base.json（共享 TS 配置 + paths）
   └── .npmrc（pnpm 配置）

2. 创建 web 应用
   ├── package.json（依赖 + scripts）
   ├── tsconfig.json（extends base）
   ├── next.config.ts（transpilePackages）
   └── src/app/（layout + page + globals.css）

3. 创建 admin 应用
   ├── package.json（依赖 + scripts）
   ├── tsconfig.json（extends base）
   ├── next.config.ts（transpilePackages）
   └── src/app/（layout + page + globals.css）

4. 创建共享包（空壳）
   ├── packages/ui/*（配置 + index.ts）
   ├── packages/api-client/*（配置 + index.ts）
   └── packages/shared/*（配置 + index.ts）

5. 更新 .gitignore（前端忽略规则）

6. 验收
   ├── pnpm install
   ├── pnpm -r typecheck
   ├── pnpm --filter web dev（验证 3000）
   └── pnpm --filter admin dev（验证 3001）
```

---

## 原子任务清单

### Step 1: 创建根配置文件

**文件**：
- `pnpm-workspace.yaml`
- `package.json`
- `tsconfig.base.json`
- `.npmrc`

**内容**：
- Workspace 配置：包含 web、admin、packages/*
- 根 scripts：dev、build、clean、typecheck
- 共享 TS 配置：paths 别名指向 packages/*/src
- pnpm 配置：shamefully-hoist=false

**验证**：文件创建成功，内容符合 02_interface.md 规范

---

### Step 2: 创建 web 应用结构

**文件**：
- `web/package.json`
- `web/tsconfig.json`
- `web/next.config.ts`
- `web/src/app/layout.tsx`
- `web/src/app/page.tsx`
- `web/src/app/globals.css`

**内容**：
- package.json：依赖 Next.js 15、React 19、workspace:* 引用内部包
- tsconfig.json：extends ../tsconfig.base.json，jsx=preserve
- next.config.ts：reactStrictMode=true，transpilePackages
- layout.tsx：最小根布局（children 渲染）
- page.tsx：简单首页（Hello World）
- globals.css：空或基础 reset

**验证**：文件创建成功，目录结构正确

---

### Step 3: 创建 admin 应用结构

**文件**：
- `admin/package.json`
- `admin/tsconfig.json`
- `admin/next.config.ts`
- `admin/src/app/layout.tsx`
- `admin/src/app/page.tsx`
- `admin/src/app/globals.css`

**内容**：与 web 应用结构相同，差异仅在于 dev 端口（3001）

**验证**：文件创建成功，目录结构正确

---

### Step 4: 创建 packages/ui 空壳

**文件**：
- `packages/ui/package.json`
- `packages/ui/tsconfig.json`
- `packages/ui/src/index.ts`

**内容**：
- package.json：name=@aieducenter/ui，exports=./src/index.ts
- tsconfig.json：extends ../../tsconfig.base.json
- index.ts：export {}

**验证**：文件创建成功

---

### Step 5: 创建 packages/api-client 空壳

**文件**：
- `packages/api-client/package.json`
- `packages/api-client/tsconfig.json`
- `packages/api-client/src/index.ts`

**内容**：
- package.json：name=@aieducenter/api-client，exports=./src/index.ts
- tsconfig.json：extends ../../tsconfig.base.json
- index.ts：export {}

**验证**：文件创建成功

---

### Step 6: 创建 packages/shared 空壳

**文件**：
- `packages/shared/package.json`
- `packages/shared/tsconfig.json`
- `packages/shared/src/index.ts`

**内容**：
- package.json：name=@aieducenter/shared，exports=./src/index.ts
- tsconfig.json：extends ../../tsconfig.base.json
- index.ts：export {}

**验证**：文件创建成功

---

### Step 7: 更新 .gitignore

**文件**：`.gitignore`

**内容**：添加前端忽略规则
```
node_modules/
.next/
*.log
.DS_Store
```

**验证**：文件更新成功

---

### Step 8: 安装依赖并验证类型检查

**命令**：
```bash
pnpm install
pnpm -r typecheck
```

**验证**：
- pnpm install 成功，无报错
- typecheck 全部通过
- 无类型错误

---

### Step 9: 验证 web 应用可运行

**命令**：
```bash
pnpm --filter web dev
```

**验证**：
- 开发服务器在 3000 端口启动
- 访问 http://localhost:3000 显示首页
- 无报错

---

### Step 10: 验证 admin 应用可运行

**命令**：
```bash
pnpm --filter admin dev
```

**验证**：
- 开发服务器在 3001 端口启动
- 访问 http://localhost:3001 显示首页
- 无报错

---

### Step 11: 验证构建成功

**命令**：
```bash
pnpm --filter web build
pnpm --filter admin build
```

**验证**：
- 两个应用构建成功
- 生成 `.next` 目录
- 无构建错误

---

## 任务依赖关系

```
Step 1（根配置）
   │
   ├──→ Step 2（web 应用）
   │        │
   │        └──→ Step 8（依赖安装）
   │                 │
   │                 ├──→ Step 9（web 验证）
   │                 └──→ Step 11（构建验证）
   │
   ├──→ Step 3（admin 应用）
   │        │
   │        └──→ Step 8（依赖安装）
   │                 │
   │                 ├──→ Step 10（admin 验证）
   │                 └──→ Step 11（构建验证）
   │
   ├──→ Step 4（packages/ui）
   │        │
   │        └──→ Step 8（依赖安装）
   │
   ├──→ Step 5（packages/api-client）
   │        │
   │        └──→ Step 8（依赖安装）
   │
   ├──→ Step 6（packages/shared）
   │        │
   │        └──→ Step 8（依赖安装）
   │
   └──→ Step 7（.gitignore）
            │
            └──→ 独立任务
```

**关键路径**：Step 1 → Step 2/3/4/5/6 → Step 8 → Step 9/10/11

**并行任务**：Step 2、3、4、5、6、7 可并行创建

---

## 验收标准映射

| AC | 对应 Step | 验证方式 |
|----|-----------|----------|
| AC1: Workspace 配置正确 | Step 1 | cat pnpm-workspace.yaml |
| AC2: 共享包目录存在 | Step 4/5/6 | ls packages/ |
| AC3: Web 应用可独立运行 | Step 2 + Step 9 | pnpm --filter web dev |
| AC4: Admin 应用可独立运行 | Step 3 + Step 10 | pnpm --filter admin dev |
| AC5: 所有包通过类型检查 | Step 8 | pnpm -r typecheck |
| AC6: 应用可完整构建 | Step 11 | pnpm --filter web/admin build |
| AC7: TypeScript paths 配置有效 | Step 1 + Step 2/3 | cat tsconfig.base.json + web/tsconfig.json |

---

## 完成检查

- [ ] Step 1: 根配置文件创建
- [ ] Step 2: web 应用结构创建
- [ ] Step 3: admin 应用结构创建
- [ ] Step 4: packages/ui 空壳创建
- [ ] Step 5: packages/api-client 空壳创建
- [ ] Step 6: packages/shared 空壳创建
- [ ] Step 7: .gitignore 更新
- [ ] Step 8: 依赖安装 + typecheck 通过
- [ ] Step 9: web 应用可运行
- [ ] Step 10: admin 应用可运行
- [ ] Step 11: 构建成功

---

## 预估工作量

| Step | 预估时间 | 说明 |
|------|----------|------|
| Step 1 | 5 分钟 | 4 个配置文件 |
| Step 2 | 10 分钟 | web 应用 6 个文件 |
| Step 3 | 10 分钟 | admin 应用 6 个文件（与 web 类似） |
| Step 4 | 3 分钟 | ui 包 3 个文件 |
| Step 5 | 3 分钟 | api-client 包 3 个文件 |
| Step 6 | 3 分钟 | shared 包 3 个文件 |
| Step 7 | 2 分钟 | .gitignore 更新 |
| Step 8 | 5 分钟 | pnpm install + typecheck |
| Step 9 | 5 分钟 | 启动验证 |
| Step 10 | 5 分钟 | 启动验证 |
| Step 11 | 5 分钟 | 构建验证 |

**总计**：约 1 小时（单人）

---

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| pnpm 版本过低 | 安装失败 | 检查 pnpm >= 9.0.0，不满足则提示升级 |
| Node.js 版本过低 | 依赖安装失败 | 检查 Node >= 20.0.0，不满足则提示升级 |
| transpilePackages 配置错误 | workspace 包无法导入 | 严格按照 02_interface.md 配置 |
| paths 配置错误 | TypeScript 找不到模块 | 验证 typecheck 必须通过 |
| 端口被占用 | dev 启动失败 | 提示用户检查端口或修改 -p 参数 |
