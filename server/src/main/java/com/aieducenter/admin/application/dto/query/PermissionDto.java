package com.aieducenter.admin.application.dto.query;

/**
 * 权限 DTO。
 *
 * @since 0.1.0
 */
public record PermissionDto(
        Long id,
        String name,
        String code,
        Long menuId,
        Integer sortOrder
) {
    public static PermissionDto from(com.aieducenter.admin.domain.entity.AdminPermission permission) {
        return new PermissionDto(
                permission.getId(),
                permission.getName(),
                permission.getCode(),
                permission.getMenuId(),
                permission.getSortOrder()
        );
    }
}
