# Feature: F01-02 后端健康检查与 Actuator

> 版本：v1.0 | 日期：2026-03-15
> 来源：Epic 1 项目骨架 / 00_epic_backlog.md

---

## 一、背景

F01-01 已完成后，项目可以正常启动，但缺少标准化的健康检查端点。健康检查是 CI/CD 流水线、容器编排（K8s）、负载均衡器判断服务可用性的基础。

本 Feature 通过引入 Spring Boot Actuator，提供符合行业标准的健康检查端点。

---

## 二、目标

- 提供 `/actuator/health` 端点，返回服务健康状态
- 端点响应时间 < 100ms（满足 K8s liveness/readiness 探针要求）
- 避免泄露敏感信息，仅暴露最小必要端点

---

## 三、范围

### 包含（In Scope）

- 添加 `spring-boot-starter-actuator` 依赖
- 配置仅暴露 `health` 端点
- 配置不显示健康检查详情（show-details: never）
- 保持 Actuator 原生响应格式

### 不包含（Out of Scope）

- 不创建自定义 HealthController
- 不包装为 ApiResponse 格式（健康检查是基础设施契约，与业务 API 分离）
- 不添加数据库/Redis 等依赖检查（后续 Feature 添加）
- 不暴露其他 Actuator 端点（info、metrics、env 等）

---

## 四、验收标准（Acceptance Criteria）

- **AC1**：`curl http://localhost:8080/actuator/health` 返回 HTTP 200
- **AC2**：响应体为 `{"status":"UP"}` 或 `{"status":"DOWN"}`（Actuator 原生格式）
- **AC3**：除 `/actuator/health` 外，其他 Actuator 端点（如 `/actuator/info`）访问返回 404
- **AC4**：无自定义 HealthController 类存在
- **AC5**：响应时间 < 100ms（本地测试）

---

## 五、约束

| 维度 | 约束 |
|------|------|
| **性能** | 响应时间 < 100ms（K8s 默认探针超时时间参考） |
| **安全** | 不暴露敏感信息（如环境变量、类路径、配置详情） |
| **兼容性** | 遵循 Actuator 标准格式，确保与主流监控工具兼容 |

---

## 六、依赖

| 前置 Feature | 状态 | 说明 |
|-------------|------|------|
| F01-01 后端项目骨架 | ✅ 已完成 | 提供可运行的 Spring Boot 应用基础 |

---

## 七、参考资料

- [Spring Boot Actuator 官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- cartisan-boot-使用手册.md（无 actuator 模块，直接使用 Spring Boot 原生）
