package com.aieducenter.controller;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HealthResponse 测试
 *
 * <p>测试健康检查响应的数据结构
 */
class HealthResponseTest {

    @Test
    void shouldCreateHealthResponseWithStatusAndTimestamp() {
        // Given: 准备创建 HealthResponse
        String expectedStatus = "ok";
        String expectedTimestamp = Instant.now().toString();

        // When: 创建 HealthResponse
        HealthResponse response = new HealthResponse(expectedStatus, expectedTimestamp);

        // Then: 验证字段正确
        assertThat(response.status()).isEqualTo(expectedStatus);
        assertThat(response.timestamp()).isEqualTo(expectedTimestamp);
    }

    @Test
    void shouldCreateHealthResponseUsingFactoryMethod() {
        // When: 使用工厂方法创建
        HealthResponse response = HealthResponse.of();

        // Then: 验证 status 为 ok，timestamp 为有效 ISO 8601 格式
        assertThat(response.status()).isEqualTo("ok");
        assertThat(response.timestamp()).isNotEmpty();
        // ISO 8601 格式应该包含 'T'
        assertThat(response.timestamp()).contains("T");
    }
}
