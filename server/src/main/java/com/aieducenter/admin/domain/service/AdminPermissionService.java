package com.aieducenter.admin.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;

import com.aieducenter.admin.domain.aggregate.AdminMenu;
import com.aieducenter.admin.domain.aggregate.AdminRole;
import com.aieducenter.admin.domain.repository.AdminMenuRepository;
import com.aieducenter.admin.domain.repository.AdminRoleRepository;
import com.aieducenter.admin.domain.repository.AdminUserRepository;
import com.cartisan.core.stereotype.DomainService;

/**
 * 管理员权限查询服务。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>供 StpInterface 调用，返回管理员的权限和角色</li>
 *   <li>处理超级管理员的特殊逻辑</li>
 * </ul>
 *
 * @since 0.1.0
 */
@DomainService
public class AdminPermissionService {

    private final AdminUserRepository adminUserRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final AdminMenuRepository adminMenuRepository;
    private final EntityManager em;

    public AdminPermissionService(
            AdminUserRepository adminUserRepository,
            AdminRoleRepository adminRoleRepository,
            AdminMenuRepository adminMenuRepository,
            EntityManager em) {
        this.adminUserRepository = adminUserRepository;
        this.adminRoleRepository = adminRoleRepository;
        this.adminMenuRepository = adminMenuRepository;
        this.em = em;
    }

    /**
     * 获取管理员的角色 ID 列表。
     */
    public List<Long> getRoleIds(Long adminId) {
        return em.createQuery(
                "SELECT aur.roleId FROM AdminUserRole aur WHERE aur.adminId = :adminId", Long.class)
                .setParameter("adminId", adminId)
                .getResultList();
    }

    /**
     * 获取管理员的权限编码列表。
     * <p>超级管理员返回空列表，由拦截器直接放行</p>
     */
    public List<String> getPermissions(Long adminId) {
        if (hasSuperAdminRole(adminId)) {
            return List.of();
        }
        // 通过跨表查询获取权限编码
        return em.createQuery(
                "SELECT DISTINCT arp.permissionCode FROM AdminRolePermission arp " +
                "INNER JOIN AdminUserRole aur ON arp.roleId = aur.roleId " +
                "WHERE aur.adminId = :adminId", String.class)
                .setParameter("adminId", adminId)
                .getResultList();
    }

    /**
     * 获取管理员的角色编码列表。
     */
    public List<String> getRoles(Long adminId) {
        // 通过跨表查询获取角色编码
        return em.createQuery(
                "SELECT r.code FROM AdminRole r " +
                "INNER JOIN AdminUserRole aur ON r.id = aur.roleId " +
                "WHERE aur.adminId = :adminId AND r.deleted = false", String.class)
                .setParameter("adminId", adminId)
                .getResultList();
    }

    /**
     * 获取管理员的菜单列表（树形）。
     */
    public List<AdminMenu> getMenus(Long adminId) {
        if (hasSuperAdminRole(adminId)) {
            return buildMenuTree(null);  // null 表示获取所有菜单
        }

        // 通过跨表查询获取角色 ID，再查询角色
        List<Long> roleIds = em.createQuery(
                "SELECT aur.roleId FROM AdminUserRole aur WHERE aur.adminId = :adminId", Long.class)
                .setParameter("adminId", adminId)
                .getResultList();

        if (roleIds.isEmpty()) {
            return List.of();
        }

        List<AdminRole> roles = roleIds.stream()
                .map(roleId -> adminRoleRepository.findById(roleId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (roles.isEmpty()) {
            return List.of();
        }

        // 收集所有菜单 ID
        List<Long> menuIds = new ArrayList<>();
        for (AdminRole role : roles) {
            menuIds.addAll(role.getMenuIds());
        }

        if (menuIds.isEmpty()) {
            return List.of();
        }

        // 查询菜单并组装树形结构
        return buildMenuTree(menuIds);
    }

    /**
     * 构建菜单树。
     * <p>递归构建父子关系，最多支持 3 层</p>
     */
    private List<AdminMenu> buildMenuTree(List<Long> menuIds) {
        List<AdminMenu> allMenus = adminMenuRepository.findAll();
        Map<Long, AdminMenu> menuMap = new HashMap<>();
        List<AdminMenu> roots = new ArrayList<>();

        // 构建映射（如果 menuIds 为 null，则包含所有菜单）
        for (AdminMenu menu : allMenus) {
            if (menuIds == null || menuIds.contains(menu.getId())) {
                menuMap.put(menu.getId(), menu);
            }
        }

        // 建立父子关系
        for (AdminMenu menu : menuMap.values()) {
            if (menu.getParentId() == null) {
                roots.add(menu);
            } else {
                AdminMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    parent.addChild(menu);
                }
            }
        }

        return roots;
    }

    /**
     * 判断是否为超级管理员。
     */
    public boolean hasSuperAdminRole(Long adminId) {
        return adminUserRepository.hasRole(adminId, "SUPER_ADMIN");
    }
}
