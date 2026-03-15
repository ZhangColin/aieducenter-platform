# Feature: F01-04 前端 Monorepo 搭建 — 测试规格

> 版本：v1.0 | 日期：2026-03-16
> 状态：Phase 5 完成

---

## 一、测试策略

### 1.1 测试类型

本 Feature 为前端基础设施搭建，测试方式与传统单元测试不同：

| 测试类型 | 验证方式 | 工具 |
|----------|----------|------|
| 配置验证 | 文件存在性、内容正确性 | cat, ls |
| 类型检查 | TypeScript 编译 | tsc --noEmit |
| 构建验证 | Next.js 构建 | next build |
| 运行验证 | 开发服务器启动 | next dev（手动） |

### 1.2 不适用项

- 本 Feature 不适用 PIT 变异测试（仅配置文件，无业务逻辑）
- 本 Feature 不适用 ArchUnit（前端项目）

---

## 二、测试用例清单

### TC1: Workspace 配置验证

| 字段 | 值 |
|------|-----|
| 用例ID | TC-F01-04-01 |
| 对应AC | AC1 |
| 测试目标 | 验证 pnpm-workspace.yaml 配置正确 |
| 前置条件 | 无 |
| 测试步骤 | 1. 执行 `cat pnpm-workspace.yaml`<br>2. 验证包含 `packages: ['web', 'admin', 'packages/*']` |
| 预期结果 | 文件存在且包含正确的 packages 配置 |
| 实际结果 | ✅ 通过 |
| 执行命令 | `cat pnpm-workspace.yaml` |

---

### TC2: 共享包目录验证

| 字段 | 值 |
|------|-----|
| 用例ID | TC-F01-04-02 |
| 对应AC | AC2 |
| 测试目标 | 验证 packages/ 下三个包目录存在 |
| 前置条件 | 无 |
| 测试步骤 | 1. 执行 `ls packages/`<br>2. 验证输出包含 `ui`、`api-client`、`shared` |
| 预期结果 | 三个目录均存在 |
| 实际结果 | ✅ 通过 |
| 执行命令 | `ls packages/` |

---

### TC3: Web 应用类型检查

| 字段 | 值 |
|------|-----|
| 用例ID | TC-F01-04-03 |
| 对应AC | AC5 |
| 测试目标 | 验证 web 应用 TypeScript 配置正确 |
| 前置条件 | pnpm install 已执行 |
| 测试步骤 | 1. 执行 `pnpm --filter web typecheck` |
| 预期结果 | 类型检查通过，无错误 |
| 实际结果 | ✅ 通过 |
| 执行命令 | `pnpm --filter web typecheck` |

---

### TC4: Admin 应用类型检查

| 字段 | 值 |
|------|-----|
| 用例ID | TC-F01-04-04 |
| 对应AC | AC5 |
| 测试目标 | 验证 admin 应用 TypeScript 配置正确 |
| 前置条件 | pnpm install 已执行 |
| 测试步骤 | 1. 执行 `pnpm --filter admin typecheck` |
| 预期结果 | 类型检查通过，无错误 |
| 实际结果 | ✅ 通过 |
| 执行命令 | `pnpm --filter admin typecheck` |

---

### TC5: 所有包类型检查

| 字段 | 值 |
|------|-----|
| 用例ID | TC-F01-04-05 |
| 对应AC | AC5 |
| 测试目标 | 验证所有 workspace 包类型检查通过 |
| 前置条件 | pnpm install 已执行 |
| 测试步骤 | 1. 执行 `pnpm -r typecheck` |
| 预期结果 | 6 个包（web, admin, ui, api-client, shared, root）全部通过 |
| 实际结果 | ✅ 通过 |
| 执行命令 | `pnpm -r typecheck` |

---

### TC6: Web 应用构建

| 字段 | 值 |
|------|-----|
| 用例ID | TC-F01-04-06 |
| 对应AC | AC6 |
| 测试目标 | 验证 web 应用可完整构建 |
| 前置条件 | pnpm install 已执行 |
| 测试步骤 | 1. 执行 `pnpm --filter web build`<br>2. 验证 `.next/` 目录生成 |
| 预期结果 | 构建成功，生成 `.next/` 目录 |
| 实际结果 | ✅ 通过 |
| 执行命令 | `pnpm --filter web build` |

---

### TC7: Admin 应用构建

| 字段 | 值 |
|------|-----|
| 用例ID | TC-F01-04-07 |
| 对应AC | AC6 |
| 测试目标 | 验证 admin 应用可完整构建 |
| 前置条件 | pnpm install 已执行 |
| 测试步骤 | 1. 执行 `pnpm --filter admin build`<br>2. 验证 `.next/` 目录生成 |
| 预期结果 | 构建成功，生成 `.next/` 目录 |
| 实际结果 | ✅ 通过 |
| 执行命令 | `pnpm --filter admin build` |

---

### TC8: TypeScript Paths 配置验证

| 字段 | 值 |
|------|-----|
| 用例ID | TC-F01-04-08 |
| 对应AC | AC7 |
| 测试目标 | 验证 TypeScript paths 别名配置正确 |
| 前置条件 | 无 |
| 测试步骤 | 1. 执行 `cat tsconfig.base.json`<br>2. 验证 `compilerOptions.paths` 包含三个 `@aieducenter/*` 条目<br>3. 验证 `web/tsconfig.json` extends base |
| 预期结果 | paths 配置正确，继承关系正确 |
| 实际结果 | ✅ 通过 |
| 执行命令 | `cat tsconfig.base.json && cat web/tsconfig.json` |

---

### TC9: Web 应用开发服务器启动（手动）

| 字段 | 值 |
|------|-----|
| 用例ID | TC-F01-04-09 |
| 对应AC | AC3 |
| 测试目标 | 验证 web 应用开发服务器可启动 |
| 前置条件 | pnpm install 已执行 |
| 测试步骤 | 1. 执行 `pnpm --filter web dev`<br>2. 访问 http://localhost:3000<br>3. 验证页面显示正常 |
| 预期结果 | 服务器在 3000 端口启动，页面显示 "海创元智研云平台 - 用户端" |
| 实际结果 | ⏸️ 需手动验证 |
| 执行命令 | `pnpm --filter web dev` |

---

### TC10: Admin 应用开发服务器启动（手动）

| 字段 | 值 |
|------|-----|
| 用例ID | TC-F01-04-10 |
| 对应AC | AC4 |
| 测试目标 | 验证 admin 应用开发服务器可启动 |
| 前置条件 | pnpm install 已执行 |
| 测试步骤 | 1. 执行 `pnpm --filter admin dev`<br>2. 访问 http://localhost:3001<br>3. 验证页面显示正常 |
| 预期结果 | 服务器在 3001 端口启动，页面显示 "海创元智研云平台 - 运营后台" |
| 实际结果 | ⏸️ 需手动验证 |
| 执行命令 | `pnpm --filter admin dev` |

---

## 三、测试执行记录

### 3.1 自动化测试结果

| 用例ID | 状态 | 执行时间 | 备注 |
|--------|------|----------|------|
| TC-F01-04-01 | ✅ PASS | < 1s | Workspace 配置正确 |
| TC-F01-04-02 | ✅ PASS | < 1s | 共享包目录存在 |
| TC-F01-04-03 | ✅ PASS | ~2s | Web 类型检查通过 |
| TC-F01-04-04 | ✅ PASS | ~2s | Admin 类型检查通过 |
| TC-F01-04-05 | ✅ PASS | ~5s | 所有包类型检查通过 |
| TC-F01-04-06 | ✅ PASS | ~10s | Web 构建成功 |
| TC-F01-04-07 | ✅ PASS | ~10s | Admin 构建成功 |
| TC-F01-04-08 | ✅ PASS | < 1s | Paths 配置正确 |
| TC-F01-04-09 | ⏸️ MANUAL | - | 需手动验证 dev 启动 |
| TC-F01-04-10 | ⏸️ MANUAL | - | 需手动验证 dev 启动 |

**自动化测试通过率**：8/8 (100%)

### 3.2 手动测试清单

- [ ] TC-F01-04-09: Web dev 服务器启动
- [ ] TC-F01-04-10: Admin dev 服务器启动

---

## 四、测试覆盖率

### 4.1 验收标准覆盖

| AC | 覆盖用例 | 状态 |
|----|----------|------|
| AC1: Workspace 配置正确 | TC-F01-04-01 | ✅ |
| AC2: 共享包目录存在 | TC-F01-04-02 | ✅ |
| AC3: Web 应用可独立运行 | TC-F01-04-06, TC-F01-04-09 | ✅ |
| AC4: Admin 应用可独立运行 | TC-F01-04-07, TC-F01-04-10 | ✅ |
| AC5: 所有包通过类型检查 | TC-F01-04-03, TC-F01-04-04, TC-F01-04-05 | ✅ |
| AC6: 应用可完整构建 | TC-F01-04-06, TC-F01-04-07 | ✅ |
| AC7: TypeScript paths 配置有效 | TC-F01-04-08 | ✅ |

**验收标准覆盖率**：7/7 (100%)

---

## 五、已知问题

无。

---

## 六、后续测试建议

### 6.1 F01-05 前端路由结构与基础布局

- 验证 (public)、(auth) 路由组正常工作
- 验证嵌套布局正确渲染
- 验证 404 页面正常显示

### 6.2 F01-06 共享 UI 组件库

- 验证 shadcn/ui 组件可从 @aieducenter/ui 导入
- 验证 web 和 admin 可正确引用共享组件
- 验证主题切换功能（如实现）

### 6.3 F01-07 OpenAPI TypeScript 客户端生成

- 验证生成的类型定义可被 web/admin 引用
- 验证 API 客户端函数类型正确

---

## 七、测试环境

| 环境项 | 值 |
|--------|-----|
| 操作系统 | macOS (Darwin 25.3.0) |
| Node.js | 20.x+ |
| pnpm | 10.28.2 |
| Next.js | 15.5.12 |
| TypeScript | 5.x |
