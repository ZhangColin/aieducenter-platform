package com.aieducenter.admin.domain.repository;

import java.util.List;
import java.util.Optional;

import com.aieducenter.admin.domain.entity.AdminPermission;
import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;

/**
 * 权限仓储接口。
 *
 * @since 0.1.0
 */
@Port(PortType.REPOSITORY)
public interface AdminPermissionRepository {

    Optional<AdminPermission> findById(Long id);

    Optional<AdminPermission> findByCode(String code);

    List<AdminPermission> findAll();

    List<AdminPermission> findByMenuId(Long menuId);

    List<AdminPermission> findByRoleId(Long roleId);

    AdminPermission save(AdminPermission permission);

    void delete(AdminPermission permission);
}
