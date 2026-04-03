# Gradle 到 Maven 迁移设计文档

**日期**: 2026-04-03
**状态**: 设计阶段
**作者**: Claude Sonnet

## 概述

将 aieducenter-platform 后端项目从 Gradle Kotlin DSL 迁移到 Maven，与 cartisan-boot 框架的构建系统保持一致。

## 背景

- cartisan-boot 框架已完成从 Gradle 到 Maven 的迁移
- 当前项目使用 Gradle Kotlin DSL，与框架不一致
- 需要统一构建系统，简化依赖管理和插件配置

## 设计目标

1. **构建系统一致性**: 与 cartisan-boot 框架使用相同的 Maven 父 POM
2. **完全保留 Pitest 配置**: 变异测试的目标类、阈值、参数等配置完整迁移
3. **彻底删除 Gradle**: 不保留 Gradle 文件，完全切换到 Maven
4. **更新所有脚本**: 项目中的所有脚本更新为 Maven 命令
5. **更新文档**: CLAUDE.md 中的命令说明更新为 Maven

## 技术方案

### 1. Maven POM 配置

**父 POM 继承**:
```xml
<parent>
    <groupId>com.cartisan</groupId>
    <artifactId>cartisan-boot</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</parent>
```

**项目坐标**:
- groupId: `com.aieducenter`
- artifactId: `aieducenter-server`
- version: `1.0.0-SNAPSHOT`
- packaging: `jar`

**依赖声明** (与 Gradle 配置一致):
- Spring Boot Starter: web, actuator, data-jpa, data-redis
- cartisan 模块: core, web, data-jpa, security, test
- PostgreSQL Driver: 42.7.4
- Spring Security Crypto
- Hutool-all
- MapStruct + Lombok
- SpringDoc OpenAPI
- Druid 数据源
- Flyway + Flyway PostgreSQL

**Pitest 插件配置** (完全保留 Gradle 配置):
```xml
<configuration>
    <junit5PluginVersion>1.2.3</junit5PluginVersion>
    <targetClasses>
        <param>com.aieducenter.verification.domain.*</param>
        <param>com.aieducenter.verification.application.*</param>
        <param>com.aieducenter.verification.web.*</param>
        <param>com.aieducenter.verification.infrastructure.*</param>
    </targetClasses>
    <targetTests>
        <param>com.aieducenter.verification.*</param>
        <param>com.aieducenter.account.config.Authentication*</param>
    </targetTests>
    <mutators>DEFAULTS</mutators>
    <outputFormats>HTML,XML</outputFormats>
    <mutationThreshold>70</mutationThreshold>
    <jvmArgs>
        <arg>--enable-preview</arg>
    </jvmArgs>
    <threads>2</threads>
    <timeoutFactor>2.0</timeoutFactor>
    <timeoutConstInMillis>10000</timeoutConstInMillis>
</configuration>
```

**Maven 仓库配置**:
```xml
<repositories>
    <repository>
        <id>local-maven-repository</id>
        <url>file://${user.home}/.m2/repository</url>
    </repository>
    <repository>
        <id>central</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>
```

### 2. Gradle 文件清理

**删除以下文件**:
- `server/build.gradle.kts`
- `server/settings.gradle.kts`
- `server/gradlew`
- `server/gradlew.bat`
- `server/gradle/` 目录
- `server/.gradle/` 目录
- `server/build/` 目录（会被 Maven 重新生成）

### 3. 脚本迁移

**命令映射**:

| 原命令 | 新命令 | 脚本文件 |
|--------|--------|----------|
| `./gradlew clean --no-daemon --quiet` | `mvn -q clean` | scripts/validate/backend.sh:12 |
| `./gradlew compileJava --no-daemon --quiet` | `mvn -q compile` | scripts/validate/backend.sh:15 |
| `./gradlew test --no-daemon` | `mvn test` | scripts/validate/backend.sh:18 |
| `./gradlew tasks --all` (检查 archUnitTest) | 删除此检查 | scripts/validate/backend.sh:21 |
| `./gradlew archUnitTest --no-daemon --quiet` | 删除（整合到 test） | scripts/validate/backend.sh:23 |
| `./gradlew test --no-daemon` | `mvn test` | scripts/test/run.sh:66 |
| `./gradlew bootRun` | `mvn spring-boot:run` | scripts/local-start.sh:103 |

**脚本修改详情**:

**scripts/validate/backend.sh**:
```bash
# 第 12 行
- ./gradlew clean --no-daemon --quiet
+ mvn -q clean

# 第 15 行
- ./gradlew compileJava --no-daemon --quiet
+ mvn -q compile

# 第 18 行
- ./gradlew test --no-daemon
+ mvn test

# 第 21-26 行: 删除 ArchUnit 检查逻辑
- if ./gradlew tasks --all 2>/dev/null | grep -q "archUnitTest"; then
-     echo "  → 运行 ArchUnit 架构检查..."
-     ./gradlew archUnitTest --no-daemon --quiet
- else
-     echo "  → (跳过 ArchUnit - 未配置)"
- fi
```

**scripts/test/run.sh**:
```bash
# 第 66 行
- ./gradlew test --no-daemon
+ mvn test
```

**scripts/local-start.sh**:
```bash
# 第 103 行
- ./gradlew bootRun > "$LOG_DIR/backend.log" 2>&1 &
+ mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
```

### 4. 文档更新

**CLAUDE.md"技术栈"部分**:
```markdown
### 后端
- Java 21 / Spring Boot 3.4.x / Maven
- 基座框架：cartisan-boot（DDD、Web、Security、Data、AI、Event、Test）
```

**CLAUDE.md"常用命令"部分**:
```markdown
## 常用命令

> **注意：** 所有 Maven 命令需在 `server/` 子目录下执行。

- 编译：`cd server && mvn compile`
- 单元测试（无需 Docker）：`cd server && mvn test`
- 指定测试：`cd server && mvn test -Dtest=XxxTest`
- 全量检查（含 ArchUnit）：`cd server && mvn verify`
- 变异测试：`cd server && mvn org.pitest:pitest-maven:mutationCoverage`
- 前端开发：`pnpm dev`
```

### 5. 目录结构

**迁移后**:
```
server/
├── pom.xml                          # 新增
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
├── logs/                            # 保留
├── build/                           # 保留（Maven 输出目录）
└── target/                          # Maven 默认输出目录
```

## 实施计划

### Phase 1: 创建 Maven POM
1. 创建 `server/pom.xml`
2. 配置父 POM、项目坐标、依赖
3. 配置 Pitest 插件
4. 配置 Maven 仓库

### Phase 2: 清理 Gradle 文件
1. 删除所有 Gradle 配置文件
2. 删除 Gradle Wrapper
3. 删除 `.gradle` 缓存目录

### Phase 3: 更新脚本
1. 修改 `scripts/validate/backend.sh`
2. 修改 `scripts/test/run.sh`
3. 修改 `scripts/local-start.sh`

### Phase 4: 更新文档
1. 更新 `CLAUDE.md` 技术栈说明
2. 更新 `CLAUDE.md` 常用命令

### Phase 5: 验证测试
1. 执行 `mvn clean compile` 验证编译
2. 执行 `mvn test` 验证测试
3. 执行 `mvn verify` 验证全量检查
4. 执行 `mvn org.pitest:pitest-maven:mutationCoverage` 验证 Pitest
5. 运行 `scripts/local-start.sh` 验证本地启动
6. 运行 `scripts/test/run.sh` 验证测试脚本

## 风险评估

### 低风险
- Maven 配置与 cartisan-boot 完全一致，已验证可行
- 命令映射简单明确，无复杂逻辑

### 需要注意
1. **Pitest 配置**: 确保所有参数正确迁移
2. **ArchUnit 测试**: 确认整合到 `mvn test` 后正常执行
3. **脚本测试**: 迁移后需测试所有脚本功能

### 回滚方案
- Git 可随时回滚到迁移前状态
- 建议在独立分支进行迁移，验证后合并到 develop

## 参考资料

- cartisan-boot Maven POM: `~/.m2/repository/com/cartisan/cartisan-boot/0.1.0-SNAPSHOT/cartisan-boot-0.1.0-SNAPSHOT.pom`
- Maven Pitest 插件文档: https://pitest.org/quickstart/maven/
- Spring Boot Maven 插件文档: https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/html/