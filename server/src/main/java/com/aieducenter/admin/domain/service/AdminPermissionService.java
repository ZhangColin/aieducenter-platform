package com.aieducenter.admin.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aieducenter.admin.domain.entity.AdminMenu;
import com.aieducenter.admin.domain.entity.AdminRole;
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

    public AdminPermissionService(
            AdminUserRepository adminUserRepository,
            AdminRoleRepository adminRoleRepository,
            AdminMenuRepository adminMenuRepository) {
        this.adminUserRepository = adminUserRepository;
        this.adminRoleRepository = adminRoleRepository;
        this.adminMenuRepository = adminMenuRepository;
    }

    /**
     * 获取管理员的权限编码列表。
     * <p>超级管理员返回空列表，由拦截器直接放行</p>
     */
    public List<String> getPermissions(Long adminId) {
        if (hasSuperAdminRole(adminId)) {
            return List.of();
        }
        return adminUserRepository.findPermissionCodesByAdminId(adminId);
    }

    /**
     * 获取管理员的角色编码列表。
     */
    public List<String> getRoles(Long adminId) {
        return adminUserRepository.findRoleCodesByAdminId(adminId);
    }

    /**
     * 获取管理员的菜单列表（树形）。
     */
    public List<AdminMenu> getMenus(Long adminId) {
        if (hasSuperAdminRole(adminId)) {
            return adminMenuRepository.findTree();
        }

        List<AdminRole> roles = adminRoleRepository.findByAdminId(adminId);
        if (roles.isEmpty()) {
            return List.of();
        }

        // 收集所有菜单 ID
        List<Long> menuIds = new ArrayList<>();
        for (AdminRole role : roles) {
            for (AdminMenu menu : adminMenuRepository.findByRoleId(role.getId())) {
                menuIds.add(menu.getId());
            }
        }

        // 查询菜单并组装树形结构
        return buildMenuTree(menuIds);
    }

    /**
     * 判断是否为超级管理员。
     */
    public boolean hasSuperAdminRole(Long adminId) {
        return adminUserRepository.hasRole(adminId, "SUPER_ADMIN");
    }

    /**
     * 构建菜单树。
     * <p>递归构建父子关系，最多支持 3 层</p>
     */
    private List<AdminMenu> buildMenuTree(List<Long> menuIds) {
        List<AdminMenu> allMenus = adminMenuRepository.findAll();
        Map<Long, AdminMenu> menuMap = new HashMap<>();
        List<AdminMenu> roots = new ArrayList<>();

        // 构建映射
        for (AdminMenu menu : allMenus) {
            if (menuIds.contains(menu.getId())) {
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
}
