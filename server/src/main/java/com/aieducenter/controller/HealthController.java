package com.aieducenter.controller;

import com.cartisan.web.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * 应用健康检查 Controller
 *
 * <p>提供应用级健康检查接口，验证服务是否正常运行
 * <p>注：这是临时实现，F01-02 将引入 Spring Boot Actuator
 */
@RestController
public class HealthController {

    /**
     * 健康检查接口
     *
     * @return 包含状态和时间戳的响应
     */
    @GetMapping("/api/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.ok(new HealthResponse("ok", Instant.now()));
    }

    /**
     * 健康检查响应数据
     *
     * @param status 健康状态，固定值 "ok"
     * @param timestamp 响应时间戳
     */
    public record HealthResponse(String status, Instant timestamp) {}
}
