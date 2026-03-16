# Epic 1: 项目骨架

> 版本：v1.0 | 日期：2026-03-15
> 状态：待实施
> 预估总复杂度：M（中等）

---

## 一、背景

搭建项目基础架构，使前后端能够独立运行并完成联调验证，为后续 Epic 提供可运行的技术底座。

这是项目的第一个 Epic，完成后开发团队应具备：
- 可编译运行的后端 Spring Boot 项目
- 可编译运行的前端 Next.js Monorepo（web + admin + packages）
- 前后端可通信验证的全链路
- 基础的 CI/CD 检查

---

## 二、范围

| 类别 | 包含 | 不包含 |
|------|------|--------|
| **后端** | Spring Boot 项目搭建、引入 cartisan-boot、多 Context 包结构、健康检查接口、OpenAPI 配置 | 具体业务逻辑、生产环境部署配置 |
| **前端** | Next.js Monorepo（web + admin + packages/ui）、路由结构、shadcn/ui 集成 | 业务页面实现、生产优化 |
| **集成** | OpenAPI 生成配置、前后端联调验证 | 完整的错误处理、鉴权机制 |
| **工程** | CI/CD 基础配置、代码质量检查 | 完整的监控和日志系统 |

---

## 三、Feature 清单

### F01-01: 后端项目骨架搭建（复杂度：S，无依赖）

创建 Spring Boot 项目，配置 Gradle Composite Build 引入 cartisan-boot，建立 8 个限界上下文的标准包结构。

**验收标准：**
- `server/build.gradle.kts` 配置 `includeBuild("../cartisan-boot")`
- 创建 account/tenant/gateway/conversation/billing/agent/creative/admin 八个 Context
- 每个 Context 包含 domain/application/controller/infrastructure 四层
- 项目可编译通过，依赖 cartisan-boot 成功解析
- 启动日志正常，Spring Boot 应用运行

---

### F01-02: 后端健康检查与 Actuator（复杂度：S，依赖 F01-01）

配置 Spring Boot Actuator，提供健康检查端点，用于 CI/CD 和运维监控。

**验收标准：**
- 引入 cartisan-boot 的 Actuator 依赖
- 配置 `/actuator/health` 端点（仅暴露 health）
- 健康检查返回 `{"status":"UP"}`
- 响应时间 < 100ms

---

### F01-03: OpenAPI 文档配置（复杂度：S，依赖 F01-01）

配置 SpringDoc 生成 OpenAPI 3.1 规范，为前端自动生成 API 客户端做准备。

**验收标准：**
- 引入 SpringDoc 依赖
- 配置 OpenAPI 元信息（标题、版本、描述）
- 访问 `/swagger-ui.html` 可查看文档
- 输出 OpenAPI JSON 到 `/api-docs` 端点
- 配置 Bearer Token 认证方式（占位，后续 Epic 实现）

---

### F01-04: 前端 Monorepo 搭建（复杂度：M，无依赖）

使用 pnpm workspace 创建 Monorepo，配置 web、admin、packages 三个工作空间。

**验收标准：**
- `pnpm-workspace.yaml` 配置正确
- `packages/` 下创建 ui、api-client、shared 三个包
- `web/` 和 `admin/` 可独立运行（`pnpm --filter web dev`）
- 所有包可编译通过（`pnpm -r build`）
- TypeScript 配置统一（使用 paths 别名）

---

### F01-05: 前端路由结构与基础布局（复杂度：M，依赖 F01-04）

在 web 和 admin 中创建基础路由结构和布局组件。

**验收标准：**
- web 应用路由结构：`(public)`、`(auth)`、`chat`、`agents`、`creative`、`account`、`manage`
- admin 应用路由结构：`tenants`、`models`、`pricing`、`finance`、`analytics`、`audit`
- 每个应用包含根布局（Layout）和基础导航
- 404 页面
- 所有路由可访问（无报错）

---

### F01-06: 共享 UI 组件库（复杂度：M，依赖 F01-04）

创建基于 shadcn/ui 的共享组件库，供 web 和 admin 复用。

**验收标准：**
- 从 shadcn/ui 初始化项目
- 复制基础组件：Button、Input、Label、Card、Dialog、Table
- 组件从 `@aieducenter/ui` 导出
- web 和 admin 可引用共享组件
- 组件支持主题切换（darkMode，可选）

---

### F01-07: OpenAPI TypeScript 客户端生成（复杂度：M，依赖 F01-03, F01-04）

配置从后端 OpenAPI 规范自动生成 TypeScript 类型定义和 API 客户端。

**验收标准：**
- 配置 `openapi-typescript` 工具
- 生成脚本 `pnpm gen:api-client` 可执行
- 生成的类型导出到 `packages/api-client`
- 类型包含所有后端端点的 Request/Response 结构
- 前端可 `import { api } from '@aieducenter/api-client'` 调用

---

### F01-08: 前后端联调验证（复杂度：M，依赖 F01-02, F01-05, F01-07）✅

创建一个健康检查 API，前端调用并展示结果，验证全链路打通。

**验收标准：**
- 后端提供 `GET /api/health` 返回 `{"status":"ok","timestamp":"..."}`
- 前端 web 应用首页调用该接口
- 前端展示后端响应（简单的 JSON 格式化展示）
- 联调脚本 `pnpm dev` 同时启动前后端
- 端到端验证脚本可执行

---

### F01-09: CI/CD 基础配置（复杂度：M，依赖 F01-01, F01-04）

配置 GitHub Actions 或类似 CI 系统，实现代码提交后的自动检查。

**验收标准：**
- 后端 CI：编译检查 + 单元测试运行
- 前端 CI：类型检查 + Lint 检查 + 构建
- PR 合并前 CI 必须通过
- CI 时间 < 5 分钟

---

## 四、依赖关系图

```
                    F01-01 后端骨架
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
       F01-02 Actuator  F01-03 OpenAPI   ─────────
          │              │                      │
          │              └──────────┐           │
          │                         ▼           │
          │              F01-07 API Client生成    │
          │                         │           │
          │                         ▼           │
          └────────────────→ F01-08 联调验证 ←─────┘
                                  │

          F01-04 Monorepo ──→ F01-05 路由结构
                │               │
                └───→ F01-06 UI组件库
                      │
                      └─────────────→ F01-08 联调验证
                                    │
                                    ▼
                               F01-09 CI/CD
```

**关键路径：** F01-01 → F01-02 → F01-08 → F01-09

**可并行路径：**
- 后端线：F01-01 → F01-02/F01-03 → F01-07 → F01-08
- 前端线：F01-04 → F01-05/F01-06 → F01-08

---

## 五、开发顺序推荐

### 阶段 1：并行启动（可 2 人并行）

| 顺序 | Feature | 负责角色 | 预估时间 |
|------|---------|---------|----------|
| 1 | F01-01 后端项目骨架 | 后端开发 | 0.5 天 |
| 1 | F01-04 前端 Monorepo | 前端开发 | 0.5 天 |

### 阶段 2：各自深化（继续并行）

| 顺序 | Feature | 负责角色 | 预估时间 |
|------|---------|---------|----------|
| 2 | F01-02 Actuator | 后端开发 | 0.5 小时 |
| 2 | F01-05 路由结构 | 前端开发 | 0.5 天 |
| 3 | F01-03 OpenAPI | 后端开发 | 0.5 天 |
| 3 | F01-06 UI 组件库 | 前端开发 | 0.5 天 |

### 阶段 3：集成联调（需协作）

| 顺序 | Feature | 负责角色 | 预估时间 |
|------|---------|---------|----------|
| 4 | F01-07 API Client 生成 | 全栈 | 0.5 天 |
| 5 | F01-08 前后端联调验证 | 全栈 | 0.5 天 |

### 阶段 4：工程收尾

| 顺序 | Feature | 负责角色 | 预估时间 |
|------|---------|---------|----------|
| 6 | F01-09 CI/CD 配置 | DevOps/全栈 | 0.5 天 |

**总计预估**：2-3 天（1 人）或 1.5-2 天（2 人并行）

---

## 六、复杂度评估说明

| 复杂度 | 标准 | 本 Epic Feature 分布 |
|--------|------|---------------------|
| **S** | 单人 0.5 天内完成，无外部依赖或依赖明确 | F01-01, F01-02, F01-03 |
| **M** | 单人 0.5-1 天，涉及多组件集成或新技术栈 | F01-04, F01-05, F01-06, F01-07, F01-08, F01-09 |
| **L** | 单人 1-2 天，复杂业务逻辑或多上下文协作 | 无 |
| **XL** | 跨团队协作，需 2+ 天 | 无 |

---

## 七、技术风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| cartisan-boot 版本不稳定 | 高 | 使用固定版本号，必要时 Fork 修正 |
| pnpm workspace 路径解析问题 | 中 | 优先验证 `@aieducenter/*` 包引用 |
| OpenAPI 生成工具兼容性 | 中 | 先手动验证生成结果，再脚本化 |
| CI 环境差异（本地通过、CI 失败） | 低 | 使用 Docker 统一 CI 环境 |

---

## 八、完成标志（Definition of Done）

Epic 1 完成的定义：

1. ✅ 所有 Feature 的验收标准 100% 满足
2. ✅ `pnpm dev` 一键启动前后端，可访问 `http://localhost:3000` 看到健康检查结果
3. ✅ CI 在 main 分支全绿
4. ✅ 有演示截图/视频，展示联调效果
5. ✅ 代码已合并到 main 分支

---

## 九、后续 Epic 依赖

- **Epic 2（用户与登录）** 依赖：F01-03 OpenAPI、F01-05 路由结构、F01-06 UI 组件库、F01-07 API Client
- **Epic 3（AI 对话）** 依赖：全部 Feature（特别是 F01-08 联调验证确认架构可行）
