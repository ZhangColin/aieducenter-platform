package com.aieducenter.admin.application.dto.response;

import java.time.LocalDateTime;
import com.aieducenter.admin.domain.aggregate.AdminUser;

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
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminUserResponse from(AdminUser adminUser) {
        return new AdminUserResponse(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getNickname(),
                adminUser.getEmail().orElse(null),
                adminUser.getPhone().orElse(null),
                adminUser.getAvatar().orElse(null),
                adminUser.getStatus().name(),
                adminUser.getCreatedAt(),
                adminUser.getUpdatedAt()
        );
    }
}
