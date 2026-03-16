package com.aieducenter.controller;

import com.cartisan.web.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查 Controller
 *
 * <p><b>注意：</b>这是一个测试用的临时 Controller，用于 F01-08 前后端联调验证。
 * 后续 Epic 完成后可以删除。</p>
 *
 * <p>提供 {@code GET /api/health} 端点，返回系统健康状态。</p>
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查端点
     *
     * <p>无需鉴权，返回系统当前状态。</p>
     *
     * @return 包装在 ApiResponse 中的健康检查响应
     */
    @GetMapping("/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.ok(HealthResponse.of());
    }
}
