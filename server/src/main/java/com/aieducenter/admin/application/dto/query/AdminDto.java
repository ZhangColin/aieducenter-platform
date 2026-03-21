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
        boolean system,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<RoleDto> roles
) {
    public static AdminDto from(com.aieducenter.admin.domain.aggregate.Admin admin, List<RoleDto> roles) {
        return new AdminDto(
                admin.getId(),
                admin.getUsername(),
                admin.getNickname(),
                admin.getEmail().orElse(null),
                admin.getPhone().orElse(null),
                admin.getAvatar().orElse(null),
                admin.getStatus().name(),
                admin.isSystem(),
                admin.getCreatedAt(),   // 继承自 Auditable
                admin.getUpdatedAt(),   // 继承自 Auditable
                roles
        );
    }
}
