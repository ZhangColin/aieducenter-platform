package com.aieducenter.admin.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.aieducenter.admin.domain.entity.AdminRole;
import com.aieducenter.admin.domain.repository.AdminRoleRepository;
import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Adapter(PortType.REPOSITORY)
public class JpaAdminRoleRepository implements AdminRoleRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<AdminRole> findById(Long id) {
        return em.createQuery("SELECT r FROM AdminRole r WHERE r.id = :id AND r.deleted = false", AdminRole.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<AdminRole> findByCode(String code) {
        return em.createQuery("SELECT r FROM AdminRole r WHERE r.code = :code AND r.deleted = false", AdminRole.class)
                .setParameter("code", code)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<AdminRole> findAll() {
        return em.createQuery("SELECT r FROM AdminRole r WHERE r.deleted = false ORDER BY r.sortOrder", AdminRole.class)
                .getResultList();
    }

    @Override
    public List<AdminRole> findByAdminId(Long adminId) {
        return em.createQuery(
                "SELECT r FROM AdminRole r " +
                "INNER JOIN AdminUserRole aur ON r.id = aur.roleId " +
                "WHERE aur.adminId = :adminId AND r.deleted = false " +
                "ORDER BY r.sortOrder", AdminRole.class)
                .setParameter("adminId", adminId)
                .getResultList();
    }

    @Override
    public AdminRole save(AdminRole role) {
        if (role.getId() == null) {
            em.persist(role);
            return role;
        }
        return em.merge(role);
    }

    @Override
    public void delete(AdminRole role) {
        em.remove(em.contains(role) ? role : em.merge(role));
    }

    @Override
    public boolean isUsedByAnyAdmin(Long roleId) {
        Long count = em.createQuery("SELECT COUNT(aur) FROM AdminUserRole aur WHERE aur.roleId = :roleId", Long.class)
                .setParameter("roleId", roleId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除原有关联
        em.createQuery("DELETE FROM AdminRoleMenu rm WHERE rm.roleId = :roleId")
                .setParameter("roleId", roleId)
                .executeUpdate();

        // 添加新关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                AdminRoleMenu rm = new AdminRoleMenu(roleId, menuId);
                em.persist(rm);
            }
        }
    }

    @Override
    public void assignPermissions(Long roleId, List<String> permissionCodes) {
        // 删除原有权限
        em.createQuery("DELETE FROM AdminRolePermission rp WHERE rp.roleId = :roleId")
                .setParameter("roleId", roleId)
                .executeUpdate();

        // 添加新权限
        if (permissionCodes != null && !permissionCodes.isEmpty()) {
            // TODO: 通过 PermissionScanner 获取权限名称
            for (String permissionCode : permissionCodes) {
                AdminRolePermission rp = new AdminRolePermission(roleId, permissionCode, null);
                em.persist(rp);
            }
        }
    }
}
