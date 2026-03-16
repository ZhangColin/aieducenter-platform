# Feature: 前后端联调验证

> 版本：v1.0 | 日期：2026-03-16
> 所属 Epic：Epic 1 - 项目骨架

---

## 一、背景

在完成 F01-01 ~ F01-07 后，前后端项目已各自可运行，但尚未验证端到端通信。

本 Feature 的目的是创建一个最小化的完整链路：前端 → HTTP 请求 → 后端 → 响应 → 前端展示，确保：
1. 前端能正确调用后端 API
2. 使用 cartisan-boot 的 `ApiResponse` 统一响应格式
3. 为后续业务开发提供联调模板

---

## 二、目标

- 验证前后端通信链路畅通
- 确认 `ApiResponse` 响应格式正确
- 提供一键启动前后端的开发脚本
- 提供自动化验证脚本

---

## 三、范围

### 包含（In Scope）

| 项目 | 说明 |
|------|------|
| **后端** | 创建临时测试用 `HealthController`，返回 `ApiResponse<HealthResponse>` |
| **前端** | web 首页添加健康检查卡片，调用后端 `/api/health` 并展示结果 |
| **脚本** | 创建联调启动脚本 `scripts/dev.sh` 和验证脚本 `scripts/verify-integration.sh` |

### 不包含（Out of Scope）

- 完整的错误处理（超时、网络错误等由浏览器默认行为处理）
- 鉴权验证（健康检查端点无需登录）
- 生产环境部署配置

---

## 四、验收标准（Acceptance Criteria）

### AC1：后端健康检查端点

- **给定**：后端服务运行在 `localhost:8080`
- **当**：调用 `GET /api/health`
- **则**：返回以下 JSON

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "ok",
    "timestamp": "2026-03-16T10:30:00Z"
  }
}
```

### AC2：前端首页展示健康状态

- **给定**：前后端均正常运行
- **当**：访问 `http://localhost:3000`
- **则**：首页显示健康检查卡片，内容为：
  - 状态图标：✅ 正常 或 ❌ 异常
  - 最后检查时间：如 "2026-03-16 10:30:00"

### AC3：联调启动脚本

- **给定**：执行 `./scripts/dev.sh`
- **则**：
  - 后端在后台启动（Spring Boot 应用运行）
  - 前端在前台启动（Next.js dev server）
  - Ctrl+C 退出时清理后端进程

### AC4：自动化验证脚本

- **给定**：执行 `./scripts/verify-integration.sh`
- **则**：
  - 自动等待后端启动完成
  - 调用 `/api/health`
  - 验证响应格式包含 `code`, `message`, `data.status`, `data.timestamp`
  - 输出 "✅ PASS" 或 "❌ FAIL"

---

## 五、约束

- **技术栈**：后端使用 cartisan-boot 的 `ApiResponse`，前端使用 `openapi-fetch`
- **端口**：后端 8080，前端 3000
- **代码标记**：HealthController 标注为测试用代码，后续可删除
