package com.aieducenter.admin.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aieducenter.admin.domain.entity.AdminRole;
import com.aieducenter.admin.domain.repository.AdminRoleRepository;
import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * AdminRoleRepository 的 Spring Data JPA 实现。
 *
 * @since 0.1.0
 */
@Repository
@Adapter(PortType.REPOSITORY)
public interface SpringDataJpaAdminRoleRepository extends BaseRepository<AdminRole, Long>, AdminRoleRepository {

    @Override
    @Query("SELECT r FROM AdminRole r WHERE r.deleted = false ORDER BY r.sortOrder")
    List<AdminRole> findAll();

    @Override
    @Query("SELECT CASE WHEN COUNT(aur) > 0 THEN true ELSE false END " +
           "FROM AdminUserRole aur WHERE aur.roleId = :roleId")
    boolean isUsedByAnyAdmin(@Param("roleId") Long roleId);
}
