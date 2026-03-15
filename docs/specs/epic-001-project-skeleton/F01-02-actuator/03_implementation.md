# Feature: F01-02 后端健康检查与 Actuator — 实施计划

> 版本：v1.0 | 日期：2026-03-15
> 来源：01_requirement.md + 02_interface.md

---

## 一、目标复述

本 Feature 目标是配置 Spring Boot Actuator，提供标准的健康检查端点 `/actuator/health`。

**核心要求：**
- 仅暴露 health 端点，不暴露其他敏感端点
- 返回 Actuator 原生格式 `{"status":"UP"}`
- 响应时间 < 100ms
- 无需编写任何 Java 代码

---

## 二、变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | server/build.gradle.kts | 添加 spring-boot-starter-actuator 依赖 |
| 修改 | server/src/main/resources/application.yml | 添加 management 配置 |

**无需创建：**
- 无需创建任何 Java 类（Controller、Service、Repository、DTO）

---

## 三、实施步骤

### Step 1: 添加 Actuator 依赖

**文件：** `server/build.gradle.kts`

**操作：** 在 `dependencies` 块中新增一行

```kotlin
dependencies {
    // Spring Boot Starter
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Actuator（新增）
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // cartisan-core 和 cartisan-web
    implementation("com.cartisan:cartisan-core:0.1.0-SNAPSHOT")
    implementation("com.cartisan:cartisan-web:0.1.0-SNAPSHOT")

    testImplementation("com.cartisan:cartisan-test:0.1.0-SNAPSHOT")
}
```

**验证：** 执行 `./gradlew build --dry-run` 确认依赖可解析

---

### Step 2: 配置 Actuator

**文件：** `server/src/main/resources/application.yml`

**操作：** 添加 management 配置块

```yaml
server:
  port: 8080

spring:
  application:
    name: aieducenter-platform

# Actuator 配置（新增）
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

**验证：** 检查 YAML 格式正确（无缩进错误）

---

### Step 3: 启动验证

**操作：** 启动应用并验证端点

```bash
# 1. 启动应用
./gradlew bootRun

# 2. 等待启动完成（看到 "Started AieduCenterApplication" 日志）

# 3. 检查健康端点
curl -s http://localhost:8080/actuator/health
# 预期输出: {"status":"UP"}

# 4. 验证其他端点不暴露
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/info
# 预期输出: 404

# 5. 停止应用（Ctrl+C）
```

---

### Step 4: 性能验证（可选）

**操作：** 测试响应时间是否 < 100ms

```bash
# 使用 curl 测试 10 次取平均
for i in {1..10}; do
  curl -o /dev/null -s -w "%{time_total}\n" http://localhost:8080/actuator/health
done | awk '{s+=$1; count++} END {print "平均响应时间:", s/count, "秒"}'
# 预期: < 0.1 秒
```

---

## 四、验收清单

执行以下命令验证完成度：

| 验收项 | 命令 | 预期结果 |
|--------|------|----------|
| AC1: HTTP 200 | `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health` | 200 |
| AC2: 原生格式 | `curl -s http://localhost:8080/actuator/health` | `{"status":"UP"}` |
| AC3: 无其他端点 | `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/info` | 404 |
| AC4: 无自定义 Controller | `find server/src -name "*Health*Controller.java"` | 无结果 |
| AC5: 响应时间 < 100ms | Step 4 性能测试脚本 | < 0.1 |

---

## 五、注意事项

1. **无需编写测试代码**：Actuator 由 Spring 官方测试，本 Feature 仅为配置
2. **无需更新 DECISIONS.md**：无技术方案选择争议
3. **无需更新 SKILL.md**：无新的踩坑经验

---

## 六、完成标志

所有验收项通过后，本 Feature 即完成。
