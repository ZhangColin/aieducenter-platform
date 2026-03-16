package com.aieducenter.controller;

import com.cartisan.web.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HealthController 测试
 *
 * <p>测试健康检查端点
 */
class HealthControllerTest {

    @Test
    void shouldReturnHealthCheckResponse() {
        // Given: 创建 HealthController
        HealthController controller = new HealthController();

        // When: 调用 health() 方法
        ApiResponse<HealthResponse> response = controller.health();

        // Then: 验证响应结构
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.message()).isEqualTo("Success");
        assertThat(response.data()).isNotNull();
        assertThat(response.data().status()).isEqualTo("ok");
        assertThat(response.data().timestamp()).isNotEmpty();
    }
}
