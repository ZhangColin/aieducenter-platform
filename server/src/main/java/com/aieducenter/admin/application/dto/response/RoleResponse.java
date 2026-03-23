package com.aieducenter.admin.application.dto.response;

import java.util.Set;

import com.aieducenter.admin.domain.aggregate.AdminRole;

/**
 * 角色 Response。
 *
 * @since 0.1.0
 */
public record RoleResponse(
        Long id,
        String name,
        String code,
        String description,
        Integer sortOrder,
        Set<Long> menuIds,
        Set<String> permissionCodes
) {

    public static RoleResponse from(AdminRole role) {
        return new RoleResponse(
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
