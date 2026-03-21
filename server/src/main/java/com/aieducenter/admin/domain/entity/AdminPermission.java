package com.aieducenter.admin.domain.entity;

import com.cartisan.data.jpa.domain.SoftDeletable;

import jakarta.persistence.*;

/**
 * AdminPermission 实体。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>封装权限状态</li>
 *   <li>关联到所属菜单</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_permissions")
public class AdminPermission extends SoftDeletable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "menu_id")
    private Long menuId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 创建权限。
     */
    public AdminPermission(String name, String code, Long menuId, Integer sortOrder) {
        this.name = name;
        this.code = code;
        this.menuId = menuId;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    /**
     * JPA 默认构造函数。
     */
    protected AdminPermission() {
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

    public Long getMenuId() {
        return menuId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    // ========== Setter ==========

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
