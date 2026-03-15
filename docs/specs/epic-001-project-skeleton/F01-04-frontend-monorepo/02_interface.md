# Feature: F01-04 前端 Monorepo 搭建 — 接口契约

> 版本：v1.0 | 日期：2026-03-16
> 状态：Phase 2 完成

---

## 一、包结构接口

### 1.1 Workspace 配置

**文件位置**：`pnpm-workspace.yaml`

```yaml
packages:
  - 'web'
  - 'admin'
  - 'packages/*'
```

**职责**：定义 pnpm workspace 的包成员，支持 `pnpm --filter` 和 `workspace:*` 协议。

---

### 1.2 包导出接口（Exports）

**packages/ui/package.json**：

| 字段 | 值 | 说明 |
|------|-----|------|
| `name` | `@aieducenter/ui` | 包名 |
| `exports["."]` | `./src/index.ts` | 默认导出入口 |
| `type` | `module` | ES 模块 |

**packages/api-client/package.json**：同上，name 为 `@aieducenter/api-client`

**packages/shared/package.json**：同上，name 为 `@aieducenter/shared`

**职责**：定义包的入口点，支持 TypeScript paths 和 runtime 解析。

---

### 1.3 包依赖接口

**web/package.json & admin/package.json** 依赖声明：

```json
{
  "dependencies": {
    "@aieducenter/ui": "workspace:*",
    "@aieducenter/api-client": "workspace:*",
    "@aieducenter/shared": "workspace:*"
  }
}
```

**约束**：
- 使用 `workspace:*` 协议，由 pnpm 自动解析为本地链接
- 发布时 pnpm 自动替换为实际版本号

---

## 二、TypeScript 配置接口

### 2.1 共享配置（Base Config）

**文件位置**：`tsconfig.base.json`

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `compilerOptions.target` | `ES2022` | 编译目标 |
| `compilerOptions.moduleResolution` | `bundler` | 模块解析策略 |
| `compilerOptions.isolatedModules` | `true` | 独立模块编译（Next.js 要求） |
| `compilerOptions.baseUrl` | `.` | 路径解析基准目录 |
| `compilerOptions.paths["@aieducenter/ui"]` | `["./packages/ui/src"]` | UI 包路径别名 |
| `compilerOptions.paths["@aieducenter/api-client"]` | `["./packages/api-client/src"]` | API 客户端路径别名 |
| `compilerOptions.paths["@aieducenter/shared"]` | `["./packages/shared/src"]` | 共享包路径别名 |

**职责**：统一 TypeScript 配置，提供 paths 别名供各包继承。

---

### 2.2 应用配置（Web/Admin）

**文件位置**：`web/tsconfig.json` / `admin/tsconfig.json`

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `extends` | `../tsconfig.base.json` | 继承根目录共享配置 |
| `compilerOptions.jsx` | `preserve` | JSX 交由 Next.js 处理 |
| `compilerOptions.plugins[0].name` | `next` | Next.js TypeScript 插件 |
| `include` | `["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"]` | 包含文件范围 |
| `exclude` | `["node_modules"]` | 排除目录 |

**职责**：应用层 TypeScript 配置，继承 base 并添加 Next.js 特定设置。

---

### 2.3 共享包配置

**文件位置**：`packages/*/tsconfig.json`

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `extends` | `../../tsconfig.base.json` | 继承根目录共享配置 |
| `compilerOptions.composite` | `true` | 支持项目引用（可选） |
| `include` | `["src/**/*.ts", "src/**/*.tsx"]` | 包含源码范围 |

**职责**：共享包 TypeScript 配置，继承 base 并设置源码范围。

---

## 三、Next.js 配置接口

### 3.1 应用配置

**文件位置**：`web/next.config.ts` / `admin/next.config.ts`

```ts
// 伪代码
interface NextConfig {
  reactStrictMode: true
  transpilePackages: [
    '@aieducenter/ui',
    '@aieducenter/api-client',
    '@aieducenter/shared'
  ]
}
```

**字段说明**：

| 字段 | 值 | 说明 |
|------|-----|------|
| `reactStrictMode` | `true` | React 严格模式 |
| `transpilePackages` | `["@aieducenter/ui", "@aieducenter/api-client", "@aieducenter/shared"]` | 转译 workspace 包源码 |

**职责**：配置 Next.js 转译行为，支持从 workspace 包导入源码。

---

### 3.2 开发服务器端口

**配置方式**：通过命令行参数

| 应用 | 端口 | package.json scripts |
|------|------|---------------------|
| web | 3000 | `"dev": "next dev -p 3000"` |
| admin | 3001 | `"dev": "next dev -p 3001"` |

**职责**：避免两个应用同时开发时端口冲突。

---

## 四、根 Scripts 接口

### 4.1 统一脚本命令

**文件位置**：`package.json`（根目录）

| Script | 命令 | 说明 |
|--------|------|------|
| `dev` | `pnpm --filter web dev & pnpm --filter admin dev` | 同时启动两个应用（macOS/Linux） |
| `dev:web` | `pnpm --filter web dev` | 仅启动 web |
| `dev:admin` | `pnpm --filter admin dev` | 仅启动 admin |
| `build` | `pnpm --filter web --filter admin build` | 构建两个应用 |
| `build:web` | `pnpm --filter web build` | 仅构建 web |
| `build:admin` | `pnpm --filter admin build` | 仅构建 admin |
| `clean` | `rm -rf node_modules **/node_modules **/.next` | 清理构建产物 |
| `typecheck` | `pnpm -r typecheck` | 所有包类型检查 |

**职责**：提供统一的开发/构建命令入口。

---

## 五、源码结构接口

### 5.1 应用源码结构

```
web/
├── src/
│   └── app/
│       ├── layout.tsx       # 根布局（必需）
│       ├── page.tsx         # 首页（必需）
│       └── globals.css      # 全局样式（必需）
├── public/                  # 静态资源
├── next.config.ts
├── package.json
└── tsconfig.json
```

**最小可运行要求**：
- `app/layout.tsx`：根布局组件
- `app/page.tsx`：首页组件
- `app/globals.css`：全局样式（可为空）

---

### 5.2 共享包源码结构

```
packages/ui/
├── src/
│   └── index.ts            # 导出入口（空壳：export {}）
├── package.json
└── tsconfig.json
```

**最小可运行要求**：
- `src/index.ts`：存在且可被导入（内容可为 `export {}`）

---

## 六、文件清单

### 6.1 需要创建的文件

| 路径 | 说明 | 行数预估 |
|------|------|----------|
| `pnpm-workspace.yaml` | Workspace 配置 | ~5 |
| `package.json` | 根 scripts | ~25 |
| `tsconfig.base.json` | 共享 TS 配置 | ~25 |
| `web/package.json` | Web 应用依赖 | ~30 |
| `web/tsconfig.json` | Web TS 配置 | ~15 |
| `web/next.config.ts` | Next.js 配置 | ~10 |
| `web/src/app/layout.tsx` | 根布局 | ~15 |
| `web/src/app/page.tsx` | 首页 | ~10 |
| `web/src/app/globals.css` | 全局样式 | ~5 |
| `admin/package.json` | Admin 应用依赖 | ~30 |
| `admin/tsconfig.json` | Admin TS 配置 | ~15 |
| `admin/next.config.ts` | Next.js 配置 | ~10 |
| `admin/src/app/layout.tsx` | 根布局 | ~15 |
| `admin/src/app/page.tsx` | 首页 | ~10 |
| `admin/src/app/globals.css` | 全局样式 | ~5 |
| `packages/ui/package.json` | UI 包配置 | ~15 |
| `packages/ui/tsconfig.json` | UI TS 配置 | ~10 |
| `packages/ui/src/index.ts` | 导出入口 | ~1 |
| `packages/api-client/package.json` | API 包配置 | ~15 |
| `packages/api-client/tsconfig.json` | API TS 配置 | ~10 |
| `packages/api-client/src/index.ts` | 导出入口 | ~1 |
| `packages/shared/package.json` | Shared 包配置 | ~15 |
| `packages/shared/tsconfig.json` | Shared TS 配置 | ~10 |
| `packages/shared/src/index.ts` | 导出入口 | ~1 |
| `.npmrc` | pnpm 配置 | ~3 |
| `.gitignore` | Git 忽略（前端部分） | ~20 |

**总计**：约 22 个文件，~300 行配置代码

---

## 七、验收验证命令

| 验收项 | 命令 | 期望输出 |
|--------|------|----------|
| Workspace 配置 | `cat pnpm-workspace.yaml` | 包含 `packages: ["web", "admin", "packages/*"]` |
| 共享包目录 | `ls packages/` | `ui api-client shared` |
| Web 可运行 | `pnpm --filter web dev` | 3000 端口启动，无报错 |
| Admin 可运行 | `pnpm --filter admin dev` | 3001 端口启动，无报错 |
| 类型检查 | `pnpm -r typecheck` | 全部通过 |
| Web 构建 | `pnpm --filter web build` | `.next/` 目录生成 |
| Admin 构建 | `pnpm --filter admin build` | `.next/` 目录生成 |
| TS 配置继承 | `cat web/tsconfig.json` | `extends: "../tsconfig.base.json"` |
| Paths 定义 | `cat tsconfig.base.json` | 包含 `@aieducenter/*` paths |

---

## 八、技术决策记录

### 决策 1：使用 workspace:* 协议

**方案**：依赖声明使用 `"@aieducenter/ui": "workspace:*"`

**理由**：
- pnpm 官方推荐 Monorepo 实践
- 开发期自动链接，发布时自动替换版本号
- 相比 file:../ 路径更简洁，不易出错

**风险**：无明显风险

---

### 决策 2：共享包空壳占位

**方案**：packages/ui、api-client、shared 仅创建包结构，不引入 shadcn/ui

**理由**：
- F01-04 验收标准为 "Monorepo 可运行"
- shadcn/ui 集成属于 F01-06 职责
- 保持 Feature 边界清晰，符合 SOP 原子化原则

**风险**：无明显风险

---

### 决策 3：TypeScript 配置通过 tsconfig.base.json 统一管理

**方案**：根目录创建共享配置，各包 extends

**理由**：
- 符合 "TypeScript 配置统一" 验收标准
- 修改一处，全局生效
- paths 别名集中定义，不易遗漏

**风险**：无明显风险

---

### 决策 4：Next.js 配置 transpilePackages

**方案**：web/admin next.config.ts 中配置 transpilePackages

**理由**：
- workspace 包导入的是源码（.ts），需 Next.js 转译
- 不使用 transpilePackages 可能导致运行时错误
- 符合 Next.js Monorepo 最佳实践

**风险**：无明显风险

---

### 决策 5：端口通过命令行参数配置

**方案**：dev scripts 使用 `next dev -p 3000/3001`

**理由**：
- 端口由启动命令决定，一目了然
- web/admin 可使用相同的 next.config.ts
- 符合 Next.js 文档推荐做法

**风险**：Windows 上 & 符号行为不同，需注意跨平台（记录为后续优化项）
