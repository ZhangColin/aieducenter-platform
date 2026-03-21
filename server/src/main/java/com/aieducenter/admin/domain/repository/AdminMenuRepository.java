package com.aieducenter.admin.domain.repository;

import java.util.List;
import java.util.Optional;

import com.aieducenter.admin.domain.entity.AdminMenu;
import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;

/**
 * 菜单仓储接口。
 *
 * @since 0.1.0
 */
@Port(PortType.REPOSITORY)
public interface AdminMenuRepository {

    Optional<AdminMenu> findById(Long id);

    List<AdminMenu> findAll();

    List<AdminMenu> findByParentId(Long parentId);

    List<AdminMenu> findByRoleId(Long roleId);

    /**
     * 查询所有菜单并组装成树形结构。
     */
    List<AdminMenu> findTree();

    AdminMenu save(AdminMenu menu);

    void delete(AdminMenu menu);

    /**
     * 检查菜单是否有子菜单。
     */
    boolean hasChildren(Long menuId);
}
