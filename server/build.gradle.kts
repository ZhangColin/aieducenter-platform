plugins {
    java
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.aieducenter"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starter
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core:10.18.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.18.0")

    // PostgreSQL Driver
    runtimeOnly("org.postgresql:postgresql:42.7.4")

    // cartisan-core 和 cartisan-web
    implementation("com.cartisan:cartisan-core:0.1.0-SNAPSHOT")
    implementation("com.cartisan:cartisan-web:0.1.0-SNAPSHOT")

    // SpringDoc OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    testImplementation("com.cartisan:cartisan-test:0.1.0-SNAPSHOT")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
