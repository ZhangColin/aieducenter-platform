# Feature: F01-02 后端健康检查与 Actuator — 接口契约

> 版本：v1.0 | 日期：2026-03-15
> 来源：01_requirement.md

---

## 一、HTTP 接口定义

### GET /actuator/health

**描述：** Spring Boot Actuator 标准健康检查端点，用于 K8s 探针、CI/CD 健康检查、负载均衡器存活检测。

**鉴权：** 无（公开访问，符合行业标准）

---

## 二、接口规范

### 2.1 请求格式

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| - | - | - | 无请求参数 |

**示例：**
```bash
curl http://localhost:8080/actuator/health
```

---

### 2.2 响应格式（成功）

#### 服务健康状态

| 字段 | 类型 | 说明 |
|------|------|------|
| status | String | 固定值 "UP" 或 "DOWN" |

**示例（服务正常）：**
```json
{"status":"UP"}
```

**示例（服务异常）：**
```json
{"status":"DOWN"}
```

#### HTTP 状态码

| 场景 | HTTP Status | 说明 |
|------|------------|------|
| 服务正常 | 200 OK | status=UP |
| 服务异常 | 503 Service Unavailable | status=DOWN |

---

### 2.3 错误码

| 场景 | HTTP Status | 说明 |
|------|------------|------|
| 端点不存在 | 404 Not Found | 访问非 health 端点（如 /actuator/info） |

---

## 三、配置规范

### 3.1 Gradle 依赖配置

**文件：** `server/build.gradle.kts`

**新增依赖：**
```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
```

**说明：**
- 无需指定版本（由 Spring Boot BOM 管理）
- 添加到 dependencies 块中

---

### 3.2 应用配置

**文件：** `server/src/main/resources/application.yml`

**新增配置：**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health      # 仅暴露 health 端点
      base-path: /actuator   # Actuator 基础路径
  endpoint:
    health:
      show-details: never    # 不显示详情，仅返回 status
```

**配置项说明：**

| 配置项 | 值 | 说明 |
|--------|---|------|
| management.endpoints.web.exposure.include | health | 仅暴露 health，不暴露 info/metrics/env 等 |
| management.endpoints.web.base-path | /actuator | 所有 Actuator 端点的基础路径 |
| management.endpoint.health.show-details | never | 生产环境安全最佳实践，避免泄露系统信息 |

**show-details 可选值对比：**

| 值 | 说明 | 推荐场景 |
|----|------|----------|
| never | 仅返回 status | **生产环境（本 Feature 选择）** |
| when-authorized | 认证用户可见详情 | 需要监控详情的受控环境 |
| always | 所有人可见详情 | 仅开发环境 |

---

## 四、技术方案说明

### 4.1 方案选择

**采用方案：** Spring Boot Actuator 原生

**对比备选方案：**

| 方案 | 优点 | 缺点 | 选择 |
|------|------|------|------|
| Actuator 原生 | 行业标准、工具兼容、零代码 | 需要配置安全 | ✅ 采用 |
| 自定义 HealthController | 完全可控、可包装 ApiResponse | 重复造轮、破坏工具兼容性 | ❌ 不采用 |
| 第三方健康检查库 | 功能丰富 | 引入额外依赖 | ❌ 不采用 |

### 4.2 关键设计决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 响应格式 | Actuator 原生 `{"status":"UP"}` | K8s、CI/CD 等工具原生支持 |
| show-details | never | 生产环境安全，避免泄露敏感信息 |
| 是否包装 ApiResponse | 否 | 健康检查是基础设施契约，与业务 API 分离 |

---

## 五、变更范围汇总

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | server/build.gradle.kts | 添加 actuator 依赖 |
| 修改 | server/src/main/resources/application.yml | 添加 management 配置 |

**无需创建：**
- 无需创建 Controller
- 无需创建 Service
- 无需创建 Repository
- 无需创建 DTO

---

## 六、验收方式

### 6.1 功能验证

```bash
# 1. 启动应用
./gradlew bootRun

# 2. 检查健康端点
curl -s http://localhost:8080/actuator/health
# 预期: {"status":"UP"}

# 3. 验证其他端点不暴露
curl -s http://localhost:8080/actuator/info
# 预期: 404
```

### 6.2 性能验证

```bash
# 响应时间测试（10 次平均）
for i in {1..10}; do
  curl -o /dev/null -s -w "%{time_total}\n" http://localhost:8080/actuator/health
done | awk '{s+=$1} END {print s/NR}'
# 预期: < 0.1 秒
```

---

## 七、后续扩展说明

本 Feature 仅配置基础健康检查。以下扩展在后续 Epic 中考虑：

| 扩展项 | 说明 | 后续 Epic |
|--------|------|-----------|
| 数据库健康检查 | 添加 DB 连接状态检查 | 数据库配置完成时 |
| Redis 健康检查 | 添加 Redis 连接状态检查 | 缓存引入时 |
| 自定义健康指标 | 业务层面的健康状态 | 业务需要时 |
| 指标收集 | Micrometer + Prometheus | 监控体系 Epic |
