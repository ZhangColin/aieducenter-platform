package com.aieducenter.admin.domain.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * 角色-权限关联实体。
 *
 * <p>属于 AdminRole 聚合，无回引用到 AdminRole。</p>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "sys_admin_role_permissions")
public class AdminRolePermission {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "permission_code")
    private String permissionCode;

    @Column(name = "permission_name")
    private String permissionName;

    /**
     * JPA 默认构造函数。
     */
    protected AdminRolePermission() {
    }

    /**
     * 创建关联。
     */
    public AdminRolePermission(Long roleId, String permissionCode, String permissionName) {
        this.roleId = roleId;
        this.permissionCode = Objects.requireNonNull(permissionCode, "permissionCode must not be null");
        this.permissionName = permissionName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public String getPermissionName() {
        return permissionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminRolePermission that = (AdminRolePermission) o;
        return Objects.equals(roleId, that.roleId) &&
               Objects.equals(permissionCode, that.permissionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionCode);
    }
}
