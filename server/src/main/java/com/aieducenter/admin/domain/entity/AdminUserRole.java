package com.aieducenter.admin.domain.entity;

import jakarta.persistence.*;

/**
 * 管理员-角色关联实体。
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_user_roles")
@IdClass(AdminUserRoleId.class)
public class AdminUserRole {

    @Id
    @Column(name = "admin_id")
    private Long adminId;

    @Id
    @Column(name = "role_id")
    private Long roleId;

    public AdminUserRole() {
    }

    public AdminUserRole(Long adminId, Long roleId) {
        this.adminId = adminId;
        this.roleId = roleId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public Long getRoleId() {
        return roleId;
    }
}

/**
 * 管理员-角色关联 ID 类。
 */
record AdminUserRoleId(Long adminId, Long roleId) implements java.io.Serializable {
}
