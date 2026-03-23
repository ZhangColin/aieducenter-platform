package com.aieducenter.admin.application.dto.query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员 DTO。
 *
 * @since 0.1.0
 */
public record AdminDto(
        Long id,
        String username,
        String nickname,
        String email,
        String phone,
        String avatar,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<RoleDto> roles
) {
    public static AdminDto from(com.aieducenter.admin.domain.aggregate.AdminUser adminUser, List<RoleDto> roles) {
        return new AdminDto(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getNickname(),
                adminUser.getEmail().orElse(null),
                adminUser.getPhone().orElse(null),
                adminUser.getAvatar().orElse(null),
                adminUser.getStatus().name(),
                adminUser.getCreatedAt(),   // 继承自 Auditable
                adminUser.getUpdatedAt(),   // 继承自 Auditable
                roles
        );
    }
}
