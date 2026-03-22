package com.aieducenter.admin.domain.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * 角色-菜单关联实体。
 *
 * <p>属于 AdminRole 聚合，无回引用到 AdminRole。</p>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_role_menus")
public class AdminRoleMenu {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "menu_id")
    private Long menuId;

    /**
     * JPA 默认构造函数。
     */
    protected AdminRoleMenu() {
    }

    /**
     * 创建关联。
     */
    public AdminRoleMenu(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = Objects.requireNonNull(menuId, "menuId must not be null");
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminRoleMenu that = (AdminRoleMenu) o;
        return Objects.equals(roleId, that.roleId) && Objects.equals(menuId, that.menuId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, menuId);
    }
}
