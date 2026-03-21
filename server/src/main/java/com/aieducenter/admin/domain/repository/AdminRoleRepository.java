package com.aieducenter.admin.domain.repository;

import java.util.List;
import java.util.Optional;

import com.aieducenter.admin.domain.entity.AdminRole;
import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;

/**
 * 角色仓储接口。
 *
 * @since 0.1.0
 */
@Port(PortType.REPOSITORY)
public interface AdminRoleRepository {

    Optional<AdminRole> findById(Long id);

    Optional<AdminRole> findByCode(String code);

    List<AdminRole> findAll();

    List<AdminRole> findByAdminId(Long adminId);

    AdminRole save(AdminRole role);

    void delete(AdminRole role);

    /**
     * 检查角色是否被管理员使用。
     */
    boolean isUsedByAnyAdmin(Long roleId);

    /**
     * 分配菜单给角色。
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 分配权限给角色。
     *
     * @param roleId 角色 ID
     * @param permissionCodes 权限 Code 列表
     */
    void assignPermissions(Long roleId, List<String> permissionCodes);
}
