# Feature: 前后端联调验证 — 测试规格与归档

> 版本：v1.0 | 日期：2026-03-16
> 状态：已完成

---

## 一、测试策略

### 1.1 测试分层

| 测试层 | 覆盖内容 | 工具 |
|--------|---------|------|
| **单元测试** | HealthResponse 数据结构、HealthController 逻辑 | JUnit 5 + AssertJ |
| **端到端验证** | 完整的前后端通信链路 | verify-integration.sh 脚本 |

### 1.2 测试覆盖的验收标准

| AC | 测试方式 | 位置 |
|----|---------|------|
| AC1: 后端健康检查端点 | 单元测试 + 集成验证脚本 | HealthControllerTest.java |
| AC2: 前端首页展示健康状态 | 手动验证（运行 dev.sh） | - |
| AC3: 联调启动脚本 | 手动验证 | scripts/dev.sh |
| AC4: 自动化验证脚本 | 自动化脚本 | scripts/verify-integration.sh |

---

## 二、测试用例清单

### 2.1 后端单元测试

**文件**: `server/src/test/java/com/aieducenter/controller/HealthResponseTest.java`

| 用例 ID | 描述 | 验证点 |
|---------|------|--------|
| HR-001 | 创建 HealthResponse 包含 status 和 timestamp | 字段正确赋值 |
| HR-002 | 使用工厂方法创建 | status="ok", timestamp 含 "T" |

**文件**: `server/src/test/java/com/aieducenter/controller/HealthControllerTest.java`

| 用例 ID | 描述 | 验证点 |
|---------|------|--------|
| HC-001 | 调用 health() 返回正确响应 | code=200, message="Success", data.status="ok" |

### 2.2 集成验证脚本

**文件**: `scripts/verify-integration.sh`

| 验证项 | 检查内容 |
|--------|---------|
| 服务可用性 | 后端在 30 秒内启动 |
| JSON 格式 | 响应是有效 JSON |
| 响应码 | code = 200 |
| 状态字段 | data.status = "ok" |
| 时间戳 | data.timestamp 存在 |

---

## 三、测试执行记录

### 3.1 单元测试结果

```bash
./gradlew test --tests "*Health*"
```

**结果**: BUILD SUCCESSFUL
- HealthResponseTest: 2/2 passed
- HealthControllerTest: 1/1 passed

### 3.2 端到端验证结果

```bash
./scripts/verify-integration.sh
```

**结果**: ✅ PASS
- 响应格式正确
- code = 200
- status = ok
- timestamp 存在

---

## 四、已知限制与技术债务

### 4.1 临时代码标识

以下文件标记为测试用临时代码，后续 Epic 完成后可删除：

- `server/src/main/java/com/aieducenter/controller/HealthController.java`
- `server/src/main/java/com/aieducenter/controller/HealthResponse.java`
- `server/src/test/java/com/aieducenter/controller/HealthControllerTest.java`
- `server/src/test/java/com/aieducenter/controller/HealthResponseTest.java`
- `web/src/components/health-check-card.tsx`

### 4.2 未实现项

| 项目 | 原因 | 计划 |
|------|------|------|
| 使用 @aieducenter/api-client | schema.ts 为占位实现 | F01-07 完成后迁移 |
| MockMvc 集成测试 | 当前阶段优先级较低 | 后续添加 |

### 4.3 架构偏离

HealthController 放置在 `com.aieducenter.controller` 根包，而非限界上下文内。这是临时测试代码的有意偏离，后续业务代码应遵循 DDD 结构。

---

## 五、代码审查反馈

### 5.1 审查发现

- **1 Critical**: 测试断言与 ApiResponse 实际返回不匹配（已修复）
- **5 Important**: 脚本权限、api-client 使用、DDD 结构、集成测试、环境文档
- **3 Minor**: 时间戳验证、硬编码配置等

### 5.2 处理决策

| 问题 | 决策 | 原因 |
|------|------|------|
| api-client 使用 | 保持原生 fetch | schema.ts 为占位实现 |
| DDD 结构位置 | 接受偏离 | 临时测试代码 |
| 集成测试 | 延后 | 当前阶段优先级 |
| .env.example | 已添加 | 改善开发者体验 |

---

## 六、使用手册

### 6.1 启动联调环境

```bash
# 方式 1: 使用一键脚本
./scripts/dev.sh

# 方式 2: 分别启动
./gradlew bootRun &     # 后端
pnpm dev                 # 前端
```

访问 `http://localhost:3000` 查看健康检查卡片。

### 6.2 运行验证脚本

```bash
./scripts/verify-integration.sh
```

### 6.3 访问 API 文档

```
http://localhost:8080/swagger-ui.html
```

---

## 七、完成检查

- [x] 所有 AC 验证通过
- [x] 单元测试通过 (3/3)
- [x] 集成验证脚本通过
- [x] 前端类型检查通过
- [x] 代码审查已完成
- [x] 文档已归档

---

**Feature F01-08 状态**: ✅ 已完成
