package com.aieducenter.admin.domain.repository;

import java.util.List;
import java.util.Optional;

import com.aieducenter.admin.domain.aggregate.AdminUser;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * 管理员仓储接口。
 *
 * @since 0.1.0
 */
public interface AdminUserRepository extends BaseRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);

    boolean existsByUsername(String username);

    List<AdminUser> findAll();

    /**
     * 查询管理员是否拥有指定角色。
     */
    boolean hasRole(Long adminId, String roleCode);
}
