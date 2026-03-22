package com.aieducenter.admin.domain.aggregate;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.cartisan.core.domain.AggregateRoot;
import com.cartisan.data.jpa.domain.SoftDeletable;

import jakarta.persistence.*;

/**
 * AdminRole 聚合根。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>封装角色状态</li>
 *   <li>管理角色关联的菜单和权限</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_roles")
public class AdminRole extends SoftDeletable implements AggregateRoot<AdminRole> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // 关联的菜单
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "role_id")
    private Set<com.aieducenter.admin.domain.entity.AdminRoleMenu> roleMenus = new HashSet<>();

    // 关联的权限
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "role_id")
    private Set<com.aieducenter.admin.domain.entity.AdminRolePermission> rolePermissions = new HashSet<>();

    /**
     * 创建角色。
     */
    public AdminRole(String name, String code, String description, Integer sortOrder) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    /**
     * JPA 默认构造函数。
     */
    protected AdminRole() {
    }

    /**
     * JPA 保存前生成 ID。
     */
    @PrePersist
    void prePersist() {
        if (id == null) {
            this.id = com.cartisan.data.jpa.id.TsidGenerator.newInstance().generate();
        }
    }

    // ========== Getter ==========

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Set<Long> getMenuIds() {
        return roleMenus.stream()
                .map(com.aieducenter.admin.domain.entity.AdminRoleMenu::getMenuId)
                .collect(Collectors.toSet());
    }

    public Set<String> getPermissionCodes() {
        return rolePermissions.stream()
                .map(com.aieducenter.admin.domain.entity.AdminRolePermission::getPermissionCode)
                .collect(Collectors.toSet());
    }

    public Set<com.aieducenter.admin.domain.entity.AdminRoleMenu> getRoleMenus() {
        return roleMenus;
    }

    public Set<com.aieducenter.admin.domain.entity.AdminRolePermission> getRolePermissions() {
        return rolePermissions;
    }

    // ========== Setter ==========

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    // ========== 业务行为 ==========

    /**
     * 是否为超级管理员角色。
     */
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(this.code);
    }

    /**
     * 添加菜单关联。
     */
    public void addMenu(Long menuId) {
        roleMenus.add(new com.aieducenter.admin.domain.entity.AdminRoleMenu(this.id, menuId));
    }

    /**
     * 清除所有菜单关联。
     */
    public void clearMenus() {
        roleMenus.clear();
    }

    /**
     * 添加权限关联。
     */
    public void addPermission(String permissionCode, String permissionName) {
        rolePermissions.add(new com.aieducenter.admin.domain.entity.AdminRolePermission(this.id, permissionCode, permissionName));
    }

    /**
     * 清除所有权限关联。
     */
    public void clearPermissions() {
        rolePermissions.clear();
    }
}
