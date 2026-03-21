package com.aieducenter.admin.application.dto.query;

import java.util.List;

/**
 * 角色 DTO。
 *
 * @since 0.1.0
 */
public record RoleDto(
        Long id,
        String name,
        String code,
        String description,
        Integer sortOrder,
        List<Long> menuIds,
        List<Long> permissionIds
) {
    public static RoleDto from(com.aieducenter.admin.domain.entity.AdminRole role) {
        return new RoleDto(
                role.getId(),
                role.getName(),
                role.getCode(),
                role.getDescription(),
                role.getSortOrder(),
                List.copyOf(role.getMenuIds()),
                List.copyOf(role.getPermissionIds())
        );
    }
}
