package com.aieducenter.admin.domain.repository;

import java.util.List;
import java.util.Optional;

import com.aieducenter.admin.domain.aggregate.AdminRole;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * 角色仓储接口。
 *
 * @since 0.1.0
 */
public interface AdminRoleRepository extends BaseRepository<AdminRole, Long> {

    Optional<AdminRole> findByCode(String code);

    List<AdminRole> findAll();

    /**
     * 检查角色是否被管理员使用。
     */
    boolean isUsedByAnyAdmin(Long roleId);
}
