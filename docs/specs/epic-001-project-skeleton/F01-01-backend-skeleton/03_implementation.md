# Feature: 后端项目骨架搭建 — 实施计划

> Epic: Epic 1 - 项目骨架
> Feature: F01-01
> 版本：v1.0 | 日期：2026-03-15

---

## 一、目标复述

创建 Spring Boot 项目骨架，通过 Composite Build 引用本地 cartisan-boot 框架，建立 8 个限界上下文的标准四层目录结构，实现健康检查接口，使项目可编译运行。

## 二、变更范围

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新建 | `server/settings.gradle.kts` | Gradle 设置，配置 Composite Build |
| 新建 | `server/build.gradle.kts` | Gradle 构建配置 |
| 新建 | `server/src/main/java/com/aieducenter/AieduCenterApplication.java` | 主启动类 |
| 新建 | `server/src/main/java/com/aieducenter/controller/HealthController.java` | 健康检查 Controller |
| 新建 | `server/src/main/resources/application.yml` | 应用配置 |
| 新建 | `server/src/main/resources/logback-spring.xml` | 日志配置 |
| 新建 | `server/gradle/wrapper/*` | Gradle Wrapper 文件 |
| 新建 | 8 个 Context 目录及四层子目录 | 空目录，为后续开发预留 |

## 三、核心流程（伪代码）

1. **初始化 Gradle 项目**
   - 创建 `server/` 目录
   - 生成/配置 Gradle Wrapper
   - 编写 `settings.gradle.kts`（配置 `includeBuild("../../cartisan-boot")`）
   - 编写 `build.gradle.kts`（配置依赖、Java 21）

2. **创建目录结构**
   - 创建 `src/main/java/com/aieducenter/`
   - 创建 8 个 Context 目录
   - 每个 Context 创建 4 层子目录
   - 创建 `controller/` 目录（应用级）

3. **编写主启动类**
   - 创建 `AieduCenterApplication.java`
   - 添加 `@SpringBootApplication` 注解
   - 实现 `main()` 方法

4. **编写健康检查 Controller**
   - 创建 `HealthController.java`
   - 添加 `@RestController` 注解
   - 实现 `health()` 方法，返回 `ApiResponse<HealthResponse>`

5. **编写配置文件**
   - 创建 `application.yml`（端口、应用名）
   - 创建 `logback-spring.xml`（日志格式）

6. **验证**
   - 编译：`./gradlew build`
   - 启动：`./gradlew bootRun`
   - 测试：`curl http://localhost:8080/api/health`

## 四、原子任务清单

### Step 1: 创建 Gradle Wrapper

- **文件：** `server/gradle/wrapper/gradle-wrapper.properties`, `gradlew`, `gradlew.bat`
- **内容：** Gradle 8.x 配置
- **验证：** `./gradlew --version` 可执行

### Step 2: 编写 settings.gradle.kts

- **文件：** `server/settings.gradle.kts`
- **内容：** 配置 `includeBuild("../../cartisan-boot")`
- **验证：** 文件存在且路径正确

### Step 3: 编写 build.gradle.kts

- **文件：** `server/build.gradle.kts`
- **内容：**
  - 配置 Java 21 toolchain
  - 引入 Spring Boot 3.4 插件
  - 配置依赖：cartisan-dependencies (BOM), cartisan-core, cartisan-web
  - 配置 JUnit 5 测试
- **验证：** `./gradlew dependencies` 可解析 cartisan-boot 依赖

### Step 4: 创建目录结构

- **目录：**
  - `src/main/java/com/aieducenter/`
  - `src/main/java/com/aieducenter/controller/`
  - `src/main/java/com/aieducenter/{account,tenant,gateway,conversation,billing,agent,creative,admin}/{domain,application,controller,infrastructure}/`
  - `src/main/resources/`
- **验证：** 所有目录存在

### Step 5: 编写主启动类

- **文件：** `src/main/java/com/aieducenter/AieduCenterApplication.java`
- **内容：**
  ```java
  @SpringBootApplication
  public class AieduCenterApplication {
      public static void main(String[] args) {
          SpringApplication.run(AieduCenterApplication.class, args);
      }
  }
  ```
- **验证：** `./gradlew compileJava` 成功

### Step 6: 编写 HealthController

- **文件：** `src/main/java/com/aieducenter/controller/HealthController.java`
- **内容：**
  - `@RestController` 注解
  - `@GetMapping("/api/health")` 方法
  - 返回 `ApiResponse.ok(new HealthResponse("ok", Instant.now()))`
  - `HealthResponse` record 定义
- **验证：** 编译成功

### Step 7: 编写 application.yml

- **文件：** `src/main/resources/application.yml`
- **内容：**
  ```yaml
  server:
    port: 8080
  spring:
    application:
      name: aieducenter-platform
  ```
- **验证：** 文件存在

### Step 8: 编写 logback-spring.xml

- **文件：** `src/main/resources/logback-spring.xml`
- **内容：** 控制台输出，定义日志格式和级别
- **验证：** 文件存在

### Step 9: 编译验证

- **命令：** `./gradlew build`
- **验证：** 编译成功，无错误

### Step 10: 启动验证

- **命令：** `./gradlew bootRun`
- **验证：**
  - 看到 Spring Boot 启动日志
  - 看到 "Started AieduCenterApplication"
  - 无错误日志

### Step 11: 健康检查接口验证

- **命令：** `curl http://localhost:8080/api/health`
- **验证：**
  - HTTP 200 响应
  - 响应体包含 `{"code":0,"data":{"status":"ok","timestamp":"..."}}`

---

## 五、文件清单

**配置文件（3 个）：**

| 文件 | 行数估算 | 说明 |
|------|---------|------|
| `settings.gradle.kts` | ~3 行 | 项目名 + includeBuild |
| `build.gradle.kts` | ~30 行 | 完整的 Gradle 配置 |
| `gradle/wrapper/gradle-wrapper.properties` | ~5 行 | Gradle Wrapper 配置 |

**Java 源文件（2 个）：**

| 文件 | 行数估算 | 说明 |
|------|---------|------|
| `AieduCenterApplication.java` | ~10 行 | 主启动类 |
| `HealthController.java` | ~20 行 | 健康检查 |

**配置文件（2 个）：**

| 文件 | 行数估算 | 说明 |
|------|---------|------|
| `application.yml` | ~5 行 | 应用配置 |
| `logback-spring.xml` | ~20 行 | 日志配置 |

**目录（约 41 个）：**

- 8 个 Context × 4 层 = 32 个
- controller/, resources/, gradle/ 等约 9 个

---

## 六、验收检查清单

- [ ] `server/` 目录存在
- [ ] `./gradlew --version` 可执行
- [ ] `./gradlew dependencies` 可解析 cartisan-boot 依赖
- [ ] `./gradlew build` 编译成功
- [ ] `./gradlew bootRun` 启动成功
- [ ] 日志显示 "Started AieduCenterApplication"
- [ ] `curl http://localhost:8080/api/health` 返回正确响应
- [ ] 8 个 Context 目录及四层子目录全部存在

---

## 七、风险与缓解

| 风险 | 缓解措施 |
|------|---------|
| cartisan-boot 路径错误 | 检查 `includeBuild` 相对路径是否正确 |
| Gradle 版本不兼容 | 使用 Gradle 8.x，与 cartisan-boot 一致 |
| Java 版本不匹配 | 确保 JAVA_HOME 指向 Java 21 |
| 端口 8080 被占用 | 修改 `application.yml` 中的端口配置 |
