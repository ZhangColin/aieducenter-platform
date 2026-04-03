# Gradle 到 Maven 迁移实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 将 aieducenter-platform 后端从 Gradle 迁移到 Maven，与 cartisan-boot 框架保持一致

**架构:** 直接继承 cartisan-boot 父 POM (0.1.0-SNAPSHOT)，复用其依赖管理和插件配置，创建项目专属 pom.xml 配置业务依赖

**技术栈:** Maven 3.x, Java 21, Spring Boot 3.4.0, Pitest 1.19.0

---

## 文件结构

**创建文件:**
- `server/pom.xml` - Maven 项目配置文件

**修改文件:**
- `scripts/validate/backend.sh` - 更新构建命令为 Maven
- `scripts/test/run.sh` - 更新测试命令为 Maven
- `scripts/local-start.sh` - 更新启动命令为 Maven
- `CLAUDE.md` - 更新技术栈和常用命令

**删除文件:**
- `server/build.gradle.kts`
- `server/settings.gradle.kts`
- `server/gradlew`
- `server/gradlew.bat`
- `server/gradle/`
- `server/.gradle/`

---

## Task 1: 创建 Maven POM 配置文件

**Files:**
- Create: `server/pom.xml`

- [ ] **Step 1: 创建 server/pom.xml 文件**

完整的 pom.xml 内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.cartisan</groupId>
        <artifactId>cartisan-boot</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </parent>

    <groupId>com.aieducenter</groupId>
    <artifactId>aieducenter-server</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>aieducenter-server</name>
    <description>海创元智研云平台 - 后端服务</description>

    <properties>
        <postgresql.version>42.7.4</postgresql.version>
        <pitest.junit5.plugin.version>1.2.3</pitest.junit5.plugin.version>
    </properties>

    <dependencies>
        <!-- BOM 平台导入 - 统一管理依赖版本 -->
        <dependency>
            <groupId>com.cartisan</groupId>
            <artifactId>cartisan-dependencies</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
            <version>10.18.0</version>
        </dependency>

        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>${postgresql.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- cartisan 模块 -->
        <dependency>
            <groupId>com.cartisan</groupId>
            <artifactId>cartisan-core</artifactId>
            <version>0.1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.cartisan</groupId>
            <artifactId>cartisan-web</artifactId>
            <version>0.1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.cartisan</groupId>
            <artifactId>cartisan-data-jpa</artifactId>
            <version>0.1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.cartisan</groupId>
            <artifactId>cartisan-security</artifactId>
            <version>0.1.0-SNAPSHOT</version>
        </dependency>

        <!-- Spring Security Crypto -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-crypto</artifactId>
        </dependency>

        <!-- Hutool -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>

        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok-mapstruct-binding</artifactId>
        </dependency>

        <!-- SpringDoc OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>

        <!-- Druid 数据源监控 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-3-starter</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Test Dependencies -->
        <dependency>
            <groupId>com.cartisan</groupId>
            <artifactId>cartisan-test</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Pitest Maven Plugin -->
            <plugin>
                <groupId>org.pitest</groupId>
                <artifactId>pitest-maven</artifactId>
                <version>${pitest.version}</version>
                <dependencies>
                    <dependency>
                        <groupId>org.pitest</groupId>
                        <artifactId>pitest-junit5-plugin</artifactId>
                        <version>${pitest.junit5.plugin.version}</version>
                    </dependency>
                </dependencies>
                <configuration>
                    <junit5PluginVersion>${pitest.junit5.plugin.version}</junit5PluginVersion>
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
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 验证 pom.xml 语法正确**

```bash
cd server && mvn help:effective-pom
```

Expected: 成功输出有效的 POM，无 XML 解析错误

- [ ] **Step 3: 提交创建 pom.xml**

```bash
git add server/pom.xml
git commit -m "build: add Maven POM configuration

- Inherit from cartisan-boot parent POM
- Configure all business dependencies
- Setup Pitest plugin with mutation testing

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 2: 验证 Maven 构建编译

**Files:**
- Use: `server/pom.xml`

- [ ] **Step 1: 清理并编译项目**

```bash
cd server && mvn clean compile
```

Expected: BUILD SUCCESS，编译通过，所有 Java 源文件编译成功

- [ ] **Step 2: 检查编译输出目录**

```bash
ls -la server/target/classes/com/aieducenter/
```

Expected: 存在编译后的 .class 文件

---

## Task 3: 删除 Gradle 配置文件

**Files:**
- Delete: `server/build.gradle.kts`
- Delete: `server/settings.gradle.kts`
- Delete: `server/gradlew`
- Delete: `server/gradlew.bat`
- Delete: `server/gradle/`

- [ ] **Step 1: 删除 Gradle 配置文件**

```bash
cd server && rm -f build.gradle.kts settings.gradle.kts gradlew gradlew.bat && rm -rf gradle/
```

Expected: 文件已删除，无错误

- [ ] **Step 2: 删除 Gradle 缓存目录**

```bash
cd server && rm -rf .gradle/
```

Expected: .gradle 目录已删除

- [ ] **Step 3: 提交删除 Gradle 文件**

```bash
git add server/
git commit -m "build: remove Gradle configuration files

- Delete build.gradle.kts, settings.gradle.kts
- Delete gradlew, gradlew.bat, gradle wrapper
- Delete .gradle cache directory

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 4: 更新 scripts/validate/backend.sh

**Files:**
- Modify: `scripts/validate/backend.sh`

- [ ] **Step 1: 备份原脚本**

```bash
cp scripts/validate/backend.sh scripts/validate/backend.sh.bak
```

- [ ] **Step 2: 替换 Gradle 命令为 Maven 命令**

打开 `scripts/validate/backend.sh`，进行以下修改：

**第 12 行：**
```bash
- ./gradlew clean --no-daemon --quiet
+ mvn -q clean
```

**第 15 行：**
```bash
- ./gradlew compileJava --no-daemon --quiet
+ mvn -q compile
```

**第 18 行：**
```bash
- ./gradlew test --no-daemon
+ mvn test
```

**第 21-26 行：删除整个 ArchUnit 检查逻辑块**
```bash
- if ./gradlew tasks --all 2>/dev/null | grep -q "archUnitTest"; then
-     echo "  → 运行 ArchUnit 架构检查..."
-     ./gradlew archUnitTest --no-daemon --quiet
- else
-     echo "  → (跳过 ArchUnit - 未配置)"
- fi
```

删除后，脚本应该直接进入最后的验证通过输出。

- [ ] **Step 3: 测试脚本是否正常工作**

```bash
bash scripts/validate/backend.sh
```

Expected: 脚本执行成功，输出 "✅ 后端验证通过"

- [ ] **Step 4: 删除备份文件并提交**

```bash
rm scripts/validate/backend.sh.bak
git add scripts/validate/backend.sh
git commit -m "build: update backend validation script for Maven

- Replace gradlew commands with mvn
- Remove ArchUnit task check (integrated into mvn test)
- Remove --no-daemon flag (Maven has no daemon)

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 5: 更新 scripts/test/run.sh

**Files:**
- Modify: `scripts/test/run.sh`

- [ ] **Step 1: 备份原脚本**

```bash
cp scripts/test/run.sh scripts/test/run.sh.bak
```

- [ ] **Step 2: 替换测试命令**

打开 `scripts/test/run.sh`，修改第 66 行：

```bash
- ./gradlew test --no-daemon
+ mvn test
```

- [ ] **Step 3: 提交修改**

```bash
rm scripts/test/run.sh.bak
git add scripts/test/run.sh
git commit -m "build: update test script for Maven

- Replace ./gradlew test with mvn test

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 6: 更新 scripts/local-start.sh

**Files:**
- Modify: `scripts/local-start.sh`

- [ ] **Step 1: 备份原脚本**

```bash
cp scripts/local-start.sh scripts/local-start.sh.bak
```

- [ ] **Step 2: 替换启动命令**

打开 `scripts/local-start.sh`，修改第 103 行：

```bash
- ./gradlew bootRun > "$LOG_DIR/backend.log" 2>&1 &
+ mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
```

- [ ] **Step 3: 提交修改**

```bash
rm scripts/local-start.sh.bak
git add scripts/local-start.sh
git commit -m "build: update local start script for Maven

- Replace ./gradlew bootRun with mvn spring-boot:run

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 7: 更新 CLAUDE.md 文档

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: 更新技术栈部分**

打开 `CLAUDE.md`，修改第 8 行：

```markdown
### 后端
- Java 21 / Spring Boot 3.4.x / Maven
```

- [ ] **Step 2: 更新常用命令部分**

替换"常用命令"章节（原第 30-38 行）为：

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

- [ ] **Step 3: 提交文档更新**

```bash
git add CLAUDE.md
git commit -m "docs: update build system references to Maven

- Update tech stack description
- Update common commands section
- Replace all Gradle commands with Maven equivalents

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 8: 运行单元测试验证

**Files:**
- Use: `server/pom.xml`
- Test: `server/src/test/java/`

- [ ] **Step 1: 运行完整单元测试**

```bash
cd server && mvn test
```

Expected: BUILD SUCCESS，所有测试通过

- [ ] **Step 2: 检查测试报告**

```bash
ls -la server/target/surefire-reports/
```

Expected: 存在测试报告 XML 文件

---

## Task 9: 运行全量检查验证

**Files:**
- Use: `server/pom.xml`
- Test: `server/src/test/java/`

- [ ] **Step 1: 运行全量检查**

```bash
cd server && mvn verify
```

Expected: BUILD SUCCESS，包含编译、测试、验证步骤

---

## Task 10: 验证 Pitest 变异测试

**Files:**
- Use: `server/pom.xml`

- [ ] **Step 1: 运行 Pitest 变异测试**

```bash
cd server && mvn org.pitest:pitest-maven:mutationCoverage
```

Expected: BUILD SUCCESS，生成变异测试报告

- [ ] **Step 2: 检查 Pitest 报告**

```bash
ls -la server/target/pit-reports/
```

Expected: 存在 HTML 和 XML 格式的变异测试报告

- [ ] **Step 3: 验证变异阈值**

打开 `server/target/pit-reports/*/index.html`，检查 mutation score 是否达到 70%

Expected: 变异分数 >= 70%

---

## Task 11: 验证本地启动脚本

**Files:**
- Use: `scripts/local-start.sh`

- [ ] **Step 1: 测试本地启动（需要 Redis）**

```bash
bash scripts/local-start.sh
```

Expected: 后端服务成功启动，监听 8080 端口，健康检查通过

按 Ctrl+C 停止服务

- [ ] **Step 2: 检查后端日志**

```bash
tail -n 50 logs/backend.log
```

Expected: 日志显示 Spring Boot 启动成功，无错误

---

## Task 12: 验证测试脚本

**Files:**
- Use: `scripts/test/run.sh`

- [ ] **Step 1: 运行测试脚本（需要测试环境）**

```bash
bash scripts/test/run.sh
```

Expected: 测试服务启动、数据清理、测试执行完成，输出 "✓ 测试完成！"

- [ ] **Step 2: 运行验证脚本**

```bash
bash scripts/validate/backend.sh
```

Expected: 清理、编译、测试全部通过，输出 "✅ 后端验证通过"

---

## Task 13: 清理构建缓存

**Files:**
- Use: `server/`

- [ ] **Step 1: 执行 Maven 清理**

```bash
cd server && mvn clean
```

Expected: target 目录被清空

- [ ] **Step 2: 删除旧的 Gradle build 目录（如果存在）**

```bash
cd server && rm -rf build/
```

Expected: build 目录已删除

- [ ] **Step 3: 提交任何遗漏的清理**

```bash
git status
```

如果有任何未跟踪的文件需要删除或添加到 .gitignore：

```bash
# 如有需要，更新 .gitignore
git add .gitignore
git commit -m "chore: update gitignore for Maven build artifacts"
```

---

## 验收标准

迁移成功需满足以下条件：

1. ✅ `mvn clean compile` 编译成功
2. ✅ `mvn test` 所有单元测试通过
3. ✅ `mvn verify` 全量检查通过
4. ✅ Pitest 变异测试运行成功，报告生成
5. ✅ `scripts/local-start.sh` 本地启动正常
6. ✅ `scripts/test/run.sh` 测试脚本运行正常
7. ✅ `scripts/validate/backend.sh` 验证脚本运行正常
8. ✅ 所有 Gradle 文件已删除
9. ✅ CLAUDE.md 文档已更新

## 回滚方案

如遇问题可回滚：

```bash
# 查看迁移相关的提交
git log --oneline --grep="build:"

# 回滚到迁移前的状态
git revert <commit-range>

# 或硬重置（慎用）
git reset --hard <before-migration-commit>
```

## 参考资料

- cartisan-boot 父 POM: `~/.m2/repository/com/cartisan/cartisan-boot/0.1.0-SNAPSHOT/cartisan-boot-0.1.0-SNAPSHOT.pom`
- Maven Pitest 插件: https://pitest.org/quickstart/maven/
- Spring Boot Maven 插件: https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/html/
- 设计文档: `docs/superpowers/specs/2026-04-03-maven-migration-design.md`