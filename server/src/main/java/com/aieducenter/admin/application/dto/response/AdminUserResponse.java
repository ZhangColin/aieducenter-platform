package com.aieducenter.admin.application.dto.response;

import com.aieducenter.admin.domain.aggregate.AdminUser;

import java.time.LocalDateTime;

/**
 * 管理员响应 DTO。
 *
 * @since 0.1.0
 */
public record AdminUserResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String phone,
        String avatar,
        AdminUser.AdminStatus status,
        String statusName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
