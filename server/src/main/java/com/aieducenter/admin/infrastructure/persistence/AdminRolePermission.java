package com.aieducenter.admin.infrastructure.persistence;

import jakarta.persistence.*;

/**
 * 角色-权限关联实体。
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_role_permissions")
public class AdminRolePermission {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "permission_code")
    private String permissionCode;

    @Column(name = "permission_name")
    private String permissionName;

    public AdminRolePermission() {
    }

    public AdminRolePermission(Long roleId, String permissionCode, String permissionName) {
        this.roleId = roleId;
        this.permissionCode = permissionCode;
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

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }
}
