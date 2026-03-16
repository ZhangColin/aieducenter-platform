# Feature: F01-07 OpenAPI TypeScript 客户端生成

> **Epic**: Epic 001 - 项目骨架
> **版本**: v1.0
> **日期**: 2026-03-16

---

## 一、背景

当前项目已完成：
- F01-03：后端 OpenAPI 配置（SpringDoc，端点 `/v3/api-docs`）
- F01-04：前端 Monorepo 搭建（packages/api-client 已创建但为空）

为打通前后端类型安全，需要配置从后端 OpenAPI 规范自动生成 TypeScript 类型定义和 API 客户端，使前端可以：
1. 获得完整的后端 API 类型定义
2. 使用类型安全的方式调用后端接口
3. 自动处理认证 token 注入

---

## 二、目标

- 配置 `openapi-typescript` 工具生成类型定义
- 配置 `openapi-fetch` 创建封装好的 API 客户端
- 支持 Access Token 自动注入（从 Zustand store）
- 支持 401 时自动刷新 Refresh Token（串行化）
- 提供 `pnpm gen:api-client` 脚本可执行

---

## 三、范围

### 包含（In Scope）

1. **工具配置**
   - `openapi-typescript`：从 OpenAPI schema 生成类型
   - `openapi-fetch`：类型安全的 fetch 客户端

2. **生成方式**
   - 主方案：从静态 `openapi.json` 文件生成（适合 CI/CD）
   - 可选：从运行中后端实时生成（本地开发快捷方式）

3. **client.ts 封装**
   - 自动注入 Access Token（从 `@aieducenter/shared/auth-store`）
   - 401 响应时自动刷新 token
   - 刷新失败时跳转登录页

4. **auth/refresh.ts**
   - 并发 401 请求的刷新串行化
   - 调用后端 `/api/v1/auth/refresh` 端点
   - 更新 Zustand store 中的 token

5. **脚本**
   - `gen:api`：从 openapi.json 生成类型
   - `gen:api:live`：从运行中后端生成类型
   - `sync:openapi`：从后端同步 openapi.json

### 不包含（Out of Scope）

- 后端 `/api/v1/auth/refresh` 端点的实现（Epic 02）
- `@aieducenter/shared/auth-store` 的实现（Epic 02）
- 完整的错误处理 UI（Toast、弹窗等）
- 请求取消、重试等高级功能

---

## 四、验收标准（Acceptance Criteria）

### AC1：工具安装与配置
- `packages/api-client/package.json` 包含 `openapi-typescript` 和 `openapi-fetch` 依赖
- `gen:api` 脚本可正确生成类型到 `src/api/schema.ts`

### AC2：静态 JSON 生成方式
- `openapi.json` 文件存在于包根目录
- `gen:api` 脚本从此文件生成类型，不依赖后端运行
- `sync:openapi` 脚本可从后端同步最新的 OpenAPI JSON

### AC3：client.ts 封装
- 导出 `api` 实例，可直接使用
- 请求时自动从 Zustand store 获取并注入 Access Token
- 401 响应时触发刷新逻辑

### AC4：刷新逻辑串行化
- 多个并发 401 请求只触发一次刷新
- 刷新成功后 store 中的 token 被更新
- 刷新失败后跳转登录页

### AC5：导出与使用
- `import { api } from '@aieducenter/api-client'` 可用
- `import type { paths } from '@aieducenter/api-client/schema'` 可用
- 类型包含所有后端端点的 Request/Response 结构

### AC6：边界场景
- 刷新接口本身返回 401 时不形成死循环
- SSR 环境下不执行跳转逻辑（`typeof window !== 'undefined'`）
- 无 token 时不注入 Authorization 头

---

## 五、约束

### 技术约束
- 使用 `openapi-typescript` ^7.4.5
- 使用 `openapi-fetch` ^0.12.2
- TypeScript ^5.0.0
- 依赖 `@aieducenter/shared` workspace 包

### 兼容性
- 支持 Next.js 15 App Router（SSR/CSR）
- 支持浏览器环境（localStorage、cookie）

### 性能
- 类型生成时间 < 5 秒
- 客户端初始化时间 < 50ms

---

## 六、参考资料

- 设计文档：`docs/plans/2026-03-16-f01-07-openapi-typescript-client-design.md`
- 后端 OpenAPI 配置：`server/src/main/java/com/aieducenter/config/OpenApiConfig.java`
- cartisan-boot 使用手册：`docs/guides/cartisan-boot-使用手册.md`
