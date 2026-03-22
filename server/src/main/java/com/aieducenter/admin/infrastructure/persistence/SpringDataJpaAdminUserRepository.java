package com.aieducenter.admin.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;
import com.cartisan.data.jpa.repository.BaseRepository;
import com.aieducenter.admin.domain.aggregate.Admin;
import com.aieducenter.admin.domain.repository.AdminUserRepository;

/**
 * 管理员仓储实现。
 */
@Repository
@Adapter(PortType.REPOSITORY)
public interface SpringDataJpaAdminUserRepository
        extends BaseRepository<Admin, Long>, AdminUserRepository, SpringDataJpaAdminUserRepositoryCustom {

    /**
     * 根据用户名查询管理员。
     */
    @Override
    @Query("SELECT a FROM Admin a WHERE a.username = :username AND a.deleted = false")
    Optional<Admin> findByUsername(String username);

    /**
     * 检查用户名是否存在。
     */
    @Override
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admin a WHERE a.username = :username AND a.deleted = false")
    boolean existsByUsername(String username);

    /**
     * 查询所有管理员（按创建时间倒序）。
     */
    @Override
    @Query("SELECT a FROM Admin a WHERE a.deleted = false ORDER BY a.createdAt DESC")
    List<Admin> findAll();

    /**
     * 查询管理员是否拥有指定角色。
     */
    @Override
    @Query("SELECT CASE WHEN COUNT(aur) > 0 THEN true ELSE false END " +
           "FROM AdminUserRole aur " +
           "INNER JOIN AdminRole r ON aur.roleId = r.id " +
           "WHERE aur.adminId = :adminId AND r.code = :roleCode")
    boolean hasRole(@Param("adminId") Long adminId, @Param("roleCode") String roleCode);
}
