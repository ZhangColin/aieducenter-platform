package com.aieducenter.admin.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.aieducenter.admin.domain.entity.AdminPermission;
import com.aieducenter.admin.domain.repository.AdminPermissionRepository;
import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Adapter(PortType.REPOSITORY)
public class JpaAdminPermissionRepository implements AdminPermissionRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<AdminPermission> findById(Long id) {
        return em.createQuery("SELECT p FROM AdminPermission p WHERE p.id = :id AND p.deleted = false", AdminPermission.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<AdminPermission> findByCode(String code) {
        return em.createQuery("SELECT p FROM AdminPermission p WHERE p.code = :code AND p.deleted = false", AdminPermission.class)
                .setParameter("code", code)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<AdminPermission> findAll() {
        return em.createQuery("SELECT p FROM AdminPermission p WHERE p.deleted = false ORDER BY p.sortOrder", AdminPermission.class)
                .getResultList();
    }

    @Override
    public List<AdminPermission> findByMenuId(Long menuId) {
        return em.createQuery("SELECT p FROM AdminPermission p WHERE p.menuId = :menuId AND p.deleted = false ORDER BY p.sortOrder", AdminPermission.class)
                .setParameter("menuId", menuId)
                .getResultList();
    }

    @Override
    public List<AdminPermission> findByRoleId(Long roleId) {
        return em.createQuery(
                "SELECT p FROM AdminPermission p " +
                "INNER JOIN AdminRolePermission rp ON p.id = rp.permissionId " +
                "WHERE rp.roleId = :roleId AND p.deleted = false " +
                "ORDER BY p.sortOrder", AdminPermission.class)
                .setParameter("roleId", roleId)
                .getResultList();
    }

    @Override
    public AdminPermission save(AdminPermission permission) {
        if (permission.getId() == null) {
            em.persist(permission);
            return permission;
        }
        return em.merge(permission);
    }

    @Override
    public void delete(AdminPermission permission) {
        em.remove(em.contains(permission) ? permission : em.merge(permission));
    }
}
