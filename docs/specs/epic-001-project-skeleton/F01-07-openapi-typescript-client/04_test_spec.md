# F01-07 OpenAPI TypeScript 客户端 — 测试规格

## 测试策略

### 测试层级

| 测试层 | 工具 | 覆盖内容 | 状态 |
|--------|------|----------|------|
| 单元测试 | vitest | refresh.ts 串行化刷新逻辑 | ✅ 5 tests |
| 单元测试 | vitest | client.ts 中间件行为 | ✅ 6 tests |
| 类型检查 | tsc | TypeScript 类型安全 | ✅ 无错误 |

### 测试环境

- **框架**: vitest v2.1.9
- **环境**: happy-dom (浏览器环境模拟)
- **运行命令**: `pnpm test:run`

## 测试用例清单

### refresh.ts 单元测试

文件: `packages/api-client/src/auth/refresh.test.ts`

| 测试方法 | 场景 | 验证内容 | 状态 |
|---------|------|----------|------|
| `given_valid_refresh_response_when_refresh_then_updates_store_and_returns_true` | 成功刷新 | token 被更新到 store，返回 true | ✅ |
| `given_401_response_when_refresh_then_returns_false` | 刷新失败（401） | 返回 false，token 未更新 | ✅ |
| `given_network_error_when_refresh_then_returns_false` | 网络错误 | 返回 false，错误被记录 | ✅ |
| `given_concurrent_requests_when_refresh_then_only_one_fetch_call` | 并发请求 | 只发起一次网络请求 | ✅ |
| `given_second_refresh_after_first_completes_when_refresh_then_makes_new_fetch_call` | 串行化后再次刷新 | 发起新的请求 | ✅ |

### client.ts 单元测试

文件: `packages/api-client/src/api/client.test.ts`

| 测试方法 | 场景 | 验证内容 | 状态 |
|---------|------|----------|------|
| `given_access_token_exists_when_making_request_then_includes_authorization_header` | 有 token | Authorization header 正确注入 | ✅ |
| `given_no_access_token_when_making_request_then_no_authorization_header` | 无 token | 不注入 Authorization header | ✅ |
| `given_401_response_when_not_refresh_endpoint_then_attempts_refresh` | 401 非刷新端点 | 触发 token 刷新 | ✅ |
| `given_401_response_when_refresh_endpoint_then_skips_refresh` | 401 刷新端点 | 跳过刷新（避免循环） | ✅ |
| `given_401_response_and_refresh_fails_when_in_browser_then_redirects_to_login` | 刷新失败 | 重定向到登录页 | ✅ |
| `given_401_response_and_refresh_fails_when_not_in_browser_then_no_redirect` | 非浏览器环境 | 不重定向 | ✅ |

## 覆盖的验收标准

来自 `01_requirement.md` 的 AC：

| AC | 对应测试 | 状态 |
|----|----------|------|
| AC1: 支持从 OpenAPI JSON 生成 TypeScript 类型 | 脚本配置验证 | ✅ |
| AC2: API 客户端自动注入 access token | client test #1, #2 | ✅ |
| AC3: 401 响应时自动刷新 token | client test #3, #4 | ✅ |
| AC4: 刷新失败时重定向登录 | client test #5, #6 | ✅ |
| AC5: 并发 401 时只刷新一次 | refresh test #4 | ✅ |

## 测试覆盖率

```
Test Files  2 passed (2)
Tests       11 passed (11)

覆盖率范围:
- src/auth/refresh.ts:    核心逻辑全覆盖
- src/api/client.ts:      中间件逻辑全覆盖
- src/api/types.ts:       类型定义（无需测试）
```

## 交叉审查

### 审查信息
- **审查者**: superpowers:code-reviewer 子代理
- **审查模型**: 独立于编码模型
- **审查范围**: Spec (01/02/03) + 代码变更 (5da9c74..e74d9d1)
- **审查日期**: 2026-03-16

### 审查结论
**状态**: 通过（修复后）

### 发现的问题及处理

| 严重程度 | 问题 | 处理 |
|---------|------|------|
| Critical | AuthStore 类型不一致 | ✅ 已修复（添加 clearAccessToken） |
| Critical | Refresh 缺少错误日志 | ✅ 已修复（添加 console.error） |
| Important | 硬编码端点路径 | ✅ 已修复（导出 REFRESH_ENDPOINT） |
| Important | 401 响应不重试请求 | ℹ️ 设计决策，符合实施计划 |

### 修复记录
修复提交: `e74d9d1 fix(api-client): 代码审查反馈修复`

---

## 测试运行命令

```bash
# 进入 api-client 目录
cd packages/api-client

# 运行所有测试
pnpm test:run

# 类型检查
pnpm typecheck

# 生成 OpenAPI 类型（需要后端运行或 openapi.json 存在）
pnpm gen:api

# 从后端同步 OpenAPI schema（需要后端运行）
pnpm sync:openapi
```

---

## 依赖说明

此 Feature 的部分功能依赖后续 Epic：

| 依赖 | 所属 Epic | 说明 |
|------|---------|------|
| `/api/v1/auth/refresh` 端点 | Epic 02 | 后端刷新 token 接口 |
| `@aieducenter/shared/auth-store` 完整实现 | Epic 02 | 包含用户信息、登录状态等 |

当前 `auth-store` 为临时占位实现，Epic 02 时将扩展完整功能。
