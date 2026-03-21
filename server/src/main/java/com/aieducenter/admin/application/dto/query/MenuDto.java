package com.aieducenter.admin.application.dto.query;

import java.util.List;

/**
 * 菜单 DTO。
 *
 * @since 0.1.0
 */
public record MenuDto(
        Long id,
        String name,
        String path,
        String icon,
        Long parentId,
        Integer sortOrder,
        List<MenuDto> children
) {
    public static MenuDto from(com.aieducenter.admin.domain.entity.AdminMenu menu) {
        return new MenuDto(
                menu.getId(),
                menu.getName(),
                menu.getPath(),
                menu.getIcon(),
                menu.getParentId(),
                menu.getSortOrder(),
                menu.getChildren().stream().map(MenuDto::from).toList()
        );
    }

    /**
     * 从所有菜单中构建树形结构的 DTO。
     * 用于 findTree() 方法返回的菜单列表，其中每个根菜单已经包含了其子菜单。
     */
    public static MenuDto fromTree(com.aieducenter.admin.domain.entity.AdminMenu menu, List<com.aieducenter.admin.domain.entity.AdminMenu> allMenus) {
        return new MenuDto(
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
