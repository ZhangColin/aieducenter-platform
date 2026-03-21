package com.aieducenter.admin.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.aieducenter.admin.domain.aggregate.Admin;
import com.aieducenter.admin.domain.repository.AdminUserRepository;
import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * AdminUserRepository 的 JPA 实现类。
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>使用 EntityManager 而非 Spring Data JPA，遵循 cartisan-boot 规范</li>
 *   <li>继承关系：Admin extends SoftDeletable implements AggregateRoot</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Repository
@Adapter(PortType.REPOSITORY)
public class JpaAdminUserRepository implements AdminUserRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Admin> findById(Long id) {
        return em.createQuery("SELECT a FROM Admin a WHERE a.id = :id AND a.deleted = false", Admin.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return em.createQuery("SELECT a FROM Admin a WHERE a.username = :username AND a.deleted = false", Admin.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = em.createQuery("SELECT COUNT(a) FROM Admin a WHERE a.username = :username AND a.deleted = false", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public List<Admin> findAll() {
        return em.createQuery("SELECT a FROM Admin a WHERE a.deleted = false ORDER BY a.createdAt DESC", Admin.class)
                .getResultList();
    }

    @Override
    public Admin save(Admin admin) {
        if (admin.getId() == null) {
            em.persist(admin);
            return admin;
        }
        return em.merge(admin);
    }

    @Override
    public void delete(Admin admin) {
        admin.checkCanBeDeleted();
        em.remove(em.contains(admin) ? admin : em.merge(admin));
    }

    @Override
    public List<String> findRoleCodesByAdminId(Long adminId) {
        return em.createQuery(
                "SELECT r.code FROM AdminRole r " +
                "INNER JOIN AdminUserRole aur ON r.id = aur.roleId " +
                "WHERE aur.adminId = :adminId AND r.deleted = false", String.class)
                .setParameter("adminId", adminId)
                .getResultList();
    }

    @Override
    public List<String> findPermissionCodesByAdminId(Long adminId) {
        return em.createQuery(
                "SELECT DISTINCT p.code FROM AdminPermission p " +
                "INNER JOIN AdminRolePermission arp ON p.id = arp.permissionId " +
                "INNER JOIN AdminUserRole aur ON arp.roleId = aur.roleId " +
                "WHERE aur.adminId = :adminId AND p.deleted = false", String.class)
                .setParameter("adminId", adminId)
                .getResultList();
    }

    @Override
    public boolean hasRole(Long adminId, String roleCode) {
        Long count = em.createQuery(
                "SELECT COUNT(aur) FROM AdminUserRole aur " +
                "INNER JOIN AdminRole r ON aur.roleId = r.id " +
                "WHERE aur.adminId = :adminId AND r.code = :roleCode", Long.class)
                .setParameter("adminId", adminId)
                .setParameter("roleCode", roleCode)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public long count() {
        return em.createQuery("SELECT COUNT(a) FROM Admin a WHERE a.deleted = false", Long.class)
                .getSingleResult();
    }

    @Override
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
