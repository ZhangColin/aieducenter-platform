package com.aieducenter.admin.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.cartisan.data.jpa.domain.SoftDeletable;

import jakarta.persistence.*;

/**
 * AdminMenu 实体。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>封装菜单状态</li>
 *   <li>支持树形结构（最多3级）</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_menus")
public class AdminMenu extends SoftDeletable {

    public static final int MAX_DEPTH = 3;

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "path", length = 255)
    private String path;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // 子菜单（不持久化，查询时组装）
    @Transient
    private List<AdminMenu> children = new ArrayList<>();

    /**
     * 创建菜单。
     */
    public AdminMenu(String name, String path, String icon, Long parentId, Integer sortOrder) {
        this.name = name;
        this.path = path;
        this.icon = icon;
        this.parentId = parentId;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    /**
     * JPA 默认构造函数。
     */
    protected AdminMenu() {
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

    public String getPath() {
        return path;
    }

    public String getIcon() {
        return icon;
    }

    public Long getParentId() {
        return parentId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public List<AdminMenu> getChildren() {
        return children;
    }

    // ========== Setter ==========

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setChildren(List<AdminMenu> children) {
        this.children = children != null ? children : new ArrayList<>();
    }

    // ========== 业务行为 ==========

    /**
     * 添加子菜单。
     */
    public void addChild(AdminMenu child) {
        this.children.add(child);
    }

    /**
     * 是否为根菜单。
     */
    public boolean isRoot() {
        return this.parentId == null;
    }
}
