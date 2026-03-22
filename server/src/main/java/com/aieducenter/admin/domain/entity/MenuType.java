package com.aieducenter.admin.domain.entity;

/**
 * 菜单类型枚举。
 *
 * @since 0.1.0
 */
public enum MenuType {
    /**
     * 普通菜单（可点击，有路由）。
     */
    MENU,

    /**
     * 分组标题（不可点击，纯展示，可带图标）。
     */
    GROUP,

    /**
     * 分隔线。
     */
    DIVIDER
}
