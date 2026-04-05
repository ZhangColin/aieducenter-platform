package com.aieducenter.account.application.dto.response;

import java.time.LocalDateTime;

/**
 * 用户响应 DTO。
 *
 * <p>用于返回用户详细信息，未来可用于用户信息查询 API。</p>
 */
public record UserResponse(
    Long id,
    String username,
    String nickname,
    String email,
    String phoneNumber,
    String avatar,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
