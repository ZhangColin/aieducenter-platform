package com.aieducenter.admin.infrastructure.persistence;

import jakarta.persistence.*;

/**
 * 角色-菜单关联实体。
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_role_menus")
@IdClass(AdminRoleMenuId.class)
public class AdminRoleMenu {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "menu_id")
    private Long menuId;

    public AdminRoleMenu() {
    }

    public AdminRoleMenu(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getMenuId() {
        return menuId;
    }
}

/**
 * 角色-菜单关联 ID 类。
 */
record AdminRoleMenuId(Long roleId, Long menuId) implements java.io.Serializable {
}
