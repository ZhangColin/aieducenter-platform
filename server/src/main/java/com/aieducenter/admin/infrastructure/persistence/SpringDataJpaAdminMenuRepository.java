package com.aieducenter.admin.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aieducenter.admin.domain.entity.AdminMenu;
import com.aieducenter.admin.domain.repository.AdminMenuRepository;
import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * 菜单仓储实现。
 */
@Repository
@Adapter(PortType.REPOSITORY)
public interface SpringDataJpaAdminMenuRepository extends BaseRepository<AdminMenu, Long>, AdminMenuRepository {

    @Override
    @Query("SELECT m FROM AdminMenu m WHERE m.deleted = false ORDER BY m.sortOrder")
    List<AdminMenu> findAll();

    @Override
    @Query("SELECT m FROM AdminMenu m WHERE m.parentId = :parentId AND m.deleted = false ORDER BY m.sortOrder")
    List<AdminMenu> findByParentId(Long parentId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM AdminMenu m WHERE m.parentId = :parentId AND m.deleted = false")
    boolean existsByParentId(Long parentId);
}
