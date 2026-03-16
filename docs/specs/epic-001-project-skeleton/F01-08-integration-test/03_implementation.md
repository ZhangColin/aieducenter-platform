# Feature: 前后端联调验证 — 实施计划

> 版本：v1.0 | 日期：2026-03-16

---

## 一、目标复述

创建最小化的前后端联调验证链路：
- 后端提供 `/api/health` 端点，返回 `ApiResponse<HealthResponse>`
- 前端首页展示健康检查卡片
- 提供一键启动脚本和自动化验证脚本

---

## 二、变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新增 | `server/src/main/java/com/aieducenter/controller/HealthController.java` | 健康检查 Controller（测试用） |
| 新增 | `server/src/main/java/com/aieducenter/controller/HealthResponse.java` | 健康检查响应 Record（测试用） |
| 修改 | `web/src/app/page.tsx` | 首页添加健康检查卡片 |
| 新增 | `web/src/components/health-check-card.tsx` | 健康检查卡片组件 |
| 新增 | `scripts/dev.sh` | 联调启动脚本 |
| 新增 | `scripts/verify-integration.sh` | 自动化验证脚本 |

---

## 三、核心流程（伪代码）

### 后端健康检查

```
HealthController.health():
1. 创建 HealthResponse.of()
   - status = "ok"
   - timestamp = Instant.now().toString()
2. 返回 ApiResponse.ok(response)
```

### 前端健康检查

```
HealthCheckCard:
1. onMount: 调用 api.GET /api/health
2. onSuccess: 设置状态 healthy，显示时间
3. onError: 设置状态 error，显示错误
```

### 启动脚本

```
dev.sh:
1. 启动 Spring Boot (后台)
2. 启动 Next.js (前台)
3. Ctrl+C 时清理后端进程
```

---

## 四、原子任务清单

### Step 1: 后端 - 创建 HealthResponse Record

- **文件**：`server/src/main/java/com/aieducenter/controller/HealthResponse.java`
- **内容**：
  - Java Record，包含 `status` 和 `timestamp` 字段
  - 静态工厂方法 `of()` 创建实例
- **验证**：编译通过 `./gradlew compileJava`

---

### Step 2: 后端 - 创建 HealthController

- **文件**：`server/src/main/java/com/aieducenter/controller/HealthController.java`
- **内容**：
  - `@RestController` + `@RequestMapping("/api")`
  - `GET /health` 端点，返回 `ApiResponse<HealthResponse>`
  - JavaDoc 标注为测试用代码
- **验证**：编译通过

---

### Step 3: 后端 - 验证端点可访问

- **操作**：
  - 启动后端 `./gradlew bootRun`
  - 调用 `curl http://localhost:8080/api/health`
  - 验证响应格式
- **验收**：响应包含 `code: 200`, `data.status: "ok"`, `data.timestamp`

---

### Step 4: 前端 - 创建 HealthCheckCard 组件

- **文件**：`web/src/components/health-check-card.tsx`
- **内容**：
  - React 组件，状态：`loading | healthy | error`
  - `useEffect` 调用 `/api/health`
  - UI：状态图标 + 最后检查时间
- **验证**：编译通过 `pnpm --filter web typecheck`

---

### Step 5: 前端 - 首页集成 HealthCheckCard

- **文件**：`web/src/app/page.tsx`
- **内容**：导入并渲染 `<HealthCheckCard />`
- **验证**：编译通过

---

### Step 6: 脚本 - 创建 dev.sh 启动脚本

- **文件**：`scripts/dev.sh`
- **内容**：
  - 后台启动 Spring Boot
  - 前台启动 Next.js
  - trap 捕获 Ctrl+C 清理后端
  - 创建 logs 目录
- **验证**：脚本可执行 `chmod +x scripts/dev.sh`

---

### Step 7: 脚本 - 创建 verify-integration.sh 验证脚本

- **文件**：`scripts/verify-integration.sh`
- **内容**：
  - 等待后端启动（轮询）
  - 调用 `/api/health`
  - 使用 `jq` 验证 JSON 格式
  - 输出 PASS/FAIL
- **验证**：脚本可执行

---

### Step 8: 端到端验证

- **操作**：
  - 运行 `./scripts/dev.sh`
  - 访问 `http://localhost:3000`
  - 检查健康卡片显示正常
  - 运行 `./scripts/verify-integration.sh`
- **验收**：所有验证通过

---

## 五、技术要点

### 后端

- 使用 `java.time.Instant` 生成 ISO 8601 时间戳
- 使用 cartisan-boot 的 `ApiResponse.ok()` 包装响应
- 无需鉴权，不添加 `@RequireAuth`

### 前端

- 使用 `@aieducenter/api-client` 的 `api` 实例
- 无需 token，健康检查是公开端点
- 时间格式化：`new Date(timestamp).toLocaleString('zh-CN')`

### 脚本

- dev.sh 需要保存后端 PID 以便清理
- verify-integration.sh 需要处理 jq 不存在的情况
