package com.aieducenter.admin.infrastructure.persistence;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.admin.domain.entity.AdminUserRole;

/**
 * SpringDataJpaAdminUserRepository 自定义方法实现。
 */
public class SpringDataJpaAdminUserRepositoryImpl implements SpringDataJpaAdminUserRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void assignRoles(Long adminId, List<Long> roleIds) {
        // 先删除原有角色关联
        em.createQuery("DELETE FROM AdminUserRole aur WHERE aur.adminId = :adminId")
                .setParameter("adminId", adminId)
                .executeUpdate();

        // 添加新角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                AdminUserRole aur = new AdminUserRole(adminId, roleId);
                em.persist(aur);
            }
        }
    }
}
