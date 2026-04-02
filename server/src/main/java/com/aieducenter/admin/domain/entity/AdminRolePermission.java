package com.aieducenter.admin.domain.entity;

import java.util.Objects;

import jakarta.persistence.*;
import lombok.Getter;

import com.cartisan.core.domain.DomainEntity;
import com.cartisan.data.jpa.domain.AuditableSoftDeletable;
import com.cartisan.data.jpa.id.TsidGenerator;

/**
 * 角色-权限关联实体。
 *
 * <p>属于 AdminRole 聚合，无回引用到 AdminRole。</p>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "sys_admin_role_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"role_id", "permission_code"})
})
@Getter
public class AdminRolePermission extends AuditableSoftDeletable implements DomainEntity<AdminRolePermission, Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "permission_code", nullable = false, length = 255)
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
