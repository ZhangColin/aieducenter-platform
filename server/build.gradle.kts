plugins {
    java
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("info.solidsoft.pitest") version "1.15.0"
}

group = "com.aieducenter"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenLocal()  // 优先使用本地 Maven 仓库（cartisan-boot）
    mavenCentral()
}

dependencies {
    // BOM 平台导入 - 统一管理依赖版本
    compileOnly(platform("com.cartisan:cartisan-dependencies:0.1.0-SNAPSHOT"))
    annotationProcessor(platform("com.cartisan:cartisan-dependencies:0.1.0-SNAPSHOT"))
    implementation(platform("com.cartisan:cartisan-dependencies:0.1.0-SNAPSHOT"))

    // Spring Boot Starter
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql:10.18.0")

    // PostgreSQL Driver（数据库驱动，业务自己管理版本）
    runtimeOnly("org.postgresql:postgresql:42.7.4")

    // cartisan 模块
    implementation("com.cartisan:cartisan-core:0.1.0-SNAPSHOT")
    implementation("com.cartisan:cartisan-web:0.1.0-SNAPSHOT")
    implementation("com.cartisan:cartisan-data-jpa:0.1.0-SNAPSHOT")
    implementation("com.cartisan:cartisan-security:0.1.0-SNAPSHOT")

    // Spring Security Crypto
    implementation("org.springframework.security:spring-security-crypto")

<<<<<<< HEAD
    // Hutool（使用 hutool-all，版本由 BOM 管理）
    implementation("cn.hutool:hutool-all")

    // MapStruct
    implementation("org.mapstruct:mapstruct")
    annotationProcessor("org.mapstruct:mapstruct-processor")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding")
=======
    // Hutool (for validation)
    implementation("cn.hutool:hutool-core:5.8.29")
    implementation("cn.hutool:hutool-captcha:5.8.29")
>>>>>>> 1ded2fb (deps: add hutool-captcha dependency)

    // SpringDoc OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    // Druid 数据源监控
    runtimeOnly("com.alibaba:druid-spring-boot-3-starter")

    testImplementation("com.cartisan:cartisan-test:0.1.0-SNAPSHOT")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}

tasks.bootRun {
    jvmArgs("--enable-preview")
}

pitest {
    junit5PluginVersion = "1.2.3"
    targetClasses = setOf(
        "com.aieducenter.verification.domain.*",
        "com.aieducenter.verification.application.*",
        "com.aieducenter.verification.web.*",
        "com.aieducenter.verification.infrastructure.*"
    )
    targetTests = setOf(
        "com.aieducenter.verification.*",
        "com.aieducenter.account.config.Authentication*"
    )
    mutators = setOf("DEFAULTS")
    outputFormats = setOf("HTML", "XML")
    mutationThreshold = 70
    jvmArgs = listOf("--enable-preview")
    threads = 2
    timeoutFactor = BigDecimal("2.0")
    timeoutConstInMillis = 10000
}

tasks.bootRun {
    jvmArgs("--enable-preview")
}
