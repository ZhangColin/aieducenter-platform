package com.aieducenter.admin.infrastructure.persistence;

import jakarta.persistence.*;

/**
 * 角色-权限关联实体。
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_role_permissions")
@IdClass(AdminRolePermissionId.class)
public class AdminRolePermission {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "permission_id")
    private Long permissionId;

    public AdminRolePermission() {
    }

    public AdminRolePermission(Long roleId, Long permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getPermissionId() {
        return permissionId;
    }
}

/**
 * 角色-权限关联 ID 类。
 */
record AdminRolePermissionId(Long roleId, Long permissionId) implements java.io.Serializable {
}
