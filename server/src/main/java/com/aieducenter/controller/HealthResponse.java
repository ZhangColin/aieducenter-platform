package com.aieducenter.controller;

import java.time.Instant;

/**
 * 健康检查响应
 *
 * <p><b>注意：</b>这是一个测试用的临时类，用于 F01-08 前后端联调验证。
 * 后续 Epic 完成后可以删除。</p>
 *
 * @param status   状态标识，固定值 "ok"
 * @param timestamp ISO 8601 格式的时间戳
 */
public record HealthResponse(
        String status,
        String timestamp
) {

    /**
     * 创建当前时间的健康检查响应
     *
     * @return status 为 "ok"，timestamp 为当前时间的响应
     */
    public static HealthResponse of() {
        return new HealthResponse("ok", Instant.now().toString());
    }
}
