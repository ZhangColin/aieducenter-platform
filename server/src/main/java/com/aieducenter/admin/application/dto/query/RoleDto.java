package com.aieducenter.admin.application.dto.query;

import java.util.Set;

import com.aieducenter.admin.domain.aggregate.AdminRole;

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
        Set<Long> menuIds,
        Set<String> permissionCodes
) {

    public static RoleDto from(AdminRole role) {
        return new RoleDto(
                role.getId(),
                role.getName(),
                role.getCode(),
                role.getDescription(),
                role.getSortOrder(),
                role.getMenuIds(),
                role.getPermissionCodes()
        );
    }
}
