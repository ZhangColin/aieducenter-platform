package com.aieducenter.admin.infrastructure.persistence;

import java.util.List;

/**
 * SpringDataJpaAdminUserRepository 自定义方法接口。
 */
public interface SpringDataJpaAdminUserRepositoryCustom {

    /**
     * 分配角色给管理员。
     */
    void assignRoles(Long adminId, List<Long> roleIds);
}
