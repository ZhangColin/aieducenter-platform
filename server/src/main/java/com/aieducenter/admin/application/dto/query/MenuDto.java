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
}
