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

    /**
     * 保存菜单。
     */
    AdminMenu save(AdminMenu menu);

    /**
     * 根据 ID 查询菜单。
     */
    Optional<AdminMenu> findById(Long id);

    /**
     * 查询所有菜单（按排序字段排序）。
     */
    List<AdminMenu> findAll();

    /**
     * 根据父 ID 查询菜单。
     */
    List<AdminMenu> findByParentId(Long parentId);

    /**
     * 检查菜单是否有子菜单。
     */
    boolean existsByParentId(Long parentId);

    /**
     * 删除菜单。
     */
    void delete(AdminMenu menu);
}
