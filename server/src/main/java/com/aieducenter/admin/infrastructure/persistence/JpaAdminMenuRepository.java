package com.aieducenter.admin.infrastructure.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.aieducenter.admin.domain.entity.AdminMenu;
import com.aieducenter.admin.domain.repository.AdminMenuRepository;
import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Adapter(PortType.REPOSITORY)
public class JpaAdminMenuRepository implements AdminMenuRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<AdminMenu> findById(Long id) {
        return em.createQuery("SELECT m FROM AdminMenu m WHERE m.id = :id AND m.deleted = false", AdminMenu.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<AdminMenu> findAll() {
        return em.createQuery("SELECT m FROM AdminMenu m WHERE m.deleted = false ORDER BY m.sortOrder", AdminMenu.class)
                .getResultList();
    }

    @Override
    public List<AdminMenu> findByParentId(Long parentId) {
        return em.createQuery("SELECT m FROM AdminMenu m WHERE m.parentId = :parentId AND m.deleted = false ORDER BY m.sortOrder", AdminMenu.class)
                .setParameter("parentId", parentId)
                .getResultList();
    }

    @Override
    public List<AdminMenu> findByRoleId(Long roleId) {
        return em.createQuery(
                "SELECT m FROM AdminMenu m " +
                "INNER JOIN AdminRoleMenu rm ON m.id = rm.menuId " +
                "WHERE rm.roleId = :roleId AND m.deleted = false " +
                "ORDER BY m.sortOrder", AdminMenu.class)
                .setParameter("roleId", roleId)
                .getResultList();
    }

    @Override
    public List<AdminMenu> findTree() {
        List<AdminMenu> allMenus = em.createQuery(
                "SELECT m FROM AdminMenu m WHERE m.deleted = false ORDER BY m.parentId, m.sortOrder", AdminMenu.class)
                .getResultList();

        // 组装树形结构
        Map<Long, AdminMenu> menuMap = new HashMap<>();
        List<AdminMenu> rootMenus = new ArrayList<>();

        for (AdminMenu menu : allMenus) {
            menuMap.put(menu.getId(), menu);
        }

        for (AdminMenu menu : allMenus) {
            if (menu.getParentId() == null) {
                rootMenus.add(menu);
            } else {
                AdminMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    parent.addChild(menu);
                }
            }
        }

        return rootMenus;
    }

    @Override
    public AdminMenu save(AdminMenu menu) {
        if (menu.getId() == null) {
            em.persist(menu);
            return menu;
        }
        return em.merge(menu);
    }

    @Override
    public void delete(AdminMenu menu) {
        em.remove(em.contains(menu) ? menu : em.merge(menu));
    }

    @Override
    public boolean hasChildren(Long menuId) {
        Long count = em.createQuery("SELECT COUNT(m) FROM AdminMenu m WHERE m.parentId = :menuId AND m.deleted = false", Long.class)
                .setParameter("menuId", menuId)
                .getSingleResult();
        return count > 0;
    }
}
