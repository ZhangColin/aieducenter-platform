package com.aieducenter.admin.application.dto.response;

import java.util.List;

/**
 * 菜单 Response。
 *
 * @since 0.1.0
 */
public record MenuResponse(
        Long id,
        String name,
        String path,
        String icon,
        Long parentId,
        Integer sortOrder,
        List<MenuResponse> children
) {
    public static MenuResponse from(com.aieducenter.admin.domain.aggregate.AdminMenu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPath(),
                menu.getIcon(),
                menu.getParentId(),
                menu.getSortOrder(),
                menu.getChildren().stream().map(MenuResponse::from).toList()
        );
    }

    /**
     * 从所有菜单中构建树形结构的 Response。
     * 用于 findTree() 方法返回的菜单列表，其中每个根菜单已经包含了其子菜单。
     */
    public static MenuResponse fromTree(com.aieducenter.admin.domain.aggregate.AdminMenu menu, List<com.aieducenter.admin.domain.aggregate.AdminMenu> allMenus) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPath(),
                menu.getIcon(),
                menu.getParentId(),
                menu.getSortOrder(),
                menu.getChildren().stream().map(child -> fromTree(child, allMenus)).toList()
        );
    }
}
