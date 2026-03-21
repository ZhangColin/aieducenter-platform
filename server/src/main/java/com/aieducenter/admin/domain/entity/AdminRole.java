package com.aieducenter.admin.domain.entity;

import java.util.HashSet;
import java.util.Set;

import com.cartisan.data.jpa.domain.SoftDeletable;

import jakarta.persistence.*;

/**
 * AdminRole 实体。
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
public class AdminRole extends SoftDeletable {

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

    // 关联的菜单（不持久化，仅用于查询时组装）
    @Transient
    private Set<Long> menuIds = new HashSet<>();

    // 关联的权限 Code（不持久化，仅用于查询时组装）
    @Transient
    private Set<String> permissionCodes = new HashSet<>();

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
        return menuIds;
    }

    public Set<String> getPermissionCodes() {
        return permissionCodes;
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

    public void setMenuIds(Set<Long> menuIds) {
        this.menuIds = menuIds != null ? menuIds : new HashSet<>();
    }

    public void setPermissionCodes(Set<String> permissionCodes) {
        this.permissionCodes = permissionCodes != null ? permissionCodes : new HashSet<>();
    }

    // ========== 业务行为 ==========

    /**
     * 是否为超级管理员角色。
     */
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(this.code);
    }
}
