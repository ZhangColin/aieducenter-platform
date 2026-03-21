package com.aieducenter.admin.domain.repository;

import java.util.List;
import java.util.Optional;

import com.aieducenter.admin.domain.aggregate.Admin;
import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;

/**
 * 管理员仓储接口。
 *
 * @since 0.1.0
 */
@Port(PortType.REPOSITORY)
public interface AdminUserRepository {

    Optional<Admin> findById(Long id);

    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Admin> findAll();

    Admin save(Admin admin);

    void delete(Admin admin);

    /**
     * 查询管理员的角色编码列表。
     */
    List<String> findRoleCodesByAdminId(Long adminId);

    /**
     * 查询管理员的权限编码列表。
     */
    List<String> findPermissionCodesByAdminId(Long adminId);

    /**
     * 查询管理员是否拥有指定角色。
     */
    boolean hasRole(Long adminId, String roleCode);

    /**
     * 查询管理员总数。
     */
    long count();

    /**
     * 分配角色给管理员。
     */
    void assignRoles(Long adminId, List<Long> roleIds);
}
