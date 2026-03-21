package com.aieducenter.admin.application;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.admin.domain.entity.AdminRole;
import com.aieducenter.admin.domain.repository.AdminRoleRepository;
import com.aieducenter.admin.domain.repository.AdminMenuRepository;
import com.aieducenter.admin.domain.repository.AdminPermissionRepository;
import com.aieducenter.admin.domain.error.AdminError;
import com.aieducenter.admin.application.dto.command.AssignMenusCommand;
import com.aieducenter.admin.application.dto.command.AssignPermissionsCommand;
import com.cartisan.core.exception.DomainException;

/**
 * 角色管理应用服务。
 */
@Service
public class RoleManagementAppService {

    private final AdminRoleRepository roleRepository;
    private final AdminMenuRepository menuRepository;
    private final AdminPermissionRepository permissionRepository;

    public RoleManagementAppService(AdminRoleRepository roleRepository,
                                     AdminMenuRepository menuRepository,
                                     AdminPermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * 获取所有角色列表。
     */
    public List<AdminRole> findAll() {
        return roleRepository.findAll();
    }

    /**
     * 根据 ID 获取角色详情。
     */
    public AdminRole findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new DomainException(AdminError.ROLE_NOT_FOUND));
    }

    /**
     * 创建角色。
     */
    @Transactional
    public AdminRole create(String name, String code, String description, Integer sortOrder) {
        // 检查 code 唯一性
        if (roleRepository.findByCode(code).isPresent()) {
            throw new DomainException(AdminError.ROLE_CODE_ALREADY_EXISTS);
        }

        AdminRole role = new AdminRole(name, code, description, sortOrder);
        return roleRepository.save(role);
    }

    /**
     * 修改角色。
     */
    @Transactional
    public AdminRole update(Long id, String name, String code, String description, Integer sortOrder) {
        AdminRole role = findById(id);

        // 如果修改 code，检查唯一性
        if (!Objects.equals(role.getCode(), code)) {
            roleRepository.findByCode(code).ifPresent(existing -> {
                throw new DomainException(AdminError.ROLE_CODE_ALREADY_EXISTS);
            });
        }

        role.setName(name);
        role.setCode(code);
        role.setDescription(description);
        role.setSortOrder(sortOrder);
        return roleRepository.save(role);
    }

    /**
     * 删除角色。
     */
    @Transactional
    public void delete(Long id) {
        AdminRole role = findById(id);

        // 超级管理员角色不能删除
        if (role.isSuperAdmin()) {
            throw new DomainException(AdminError.SUPER_ADMIN_CANNOT_DELETE);
        }

        // 检查是否有管理员使用该角色
        if (roleRepository.isUsedByAnyAdmin(id)) {
            throw new DomainException(AdminError.ROLE_IN_USE);
        }

        roleRepository.delete(role);
    }

    /**
     * 为角色分配菜单。
     */
    @Transactional
    public void assignMenus(Long roleId, AssignMenusCommand command) {
        // 验证角色存在
        findById(roleId);

        // 验证所有菜单 ID 存在
        for (Long menuId : command.menuIds()) {
            if (menuRepository.findById(menuId).isEmpty()) {
                throw new DomainException(AdminError.MENU_NOT_FOUND);
            }
        }

        roleRepository.assignMenus(roleId, command.menuIds());
    }

    /**
     * 为角色分配权限。
     */
    @Transactional
    public void assignPermissions(Long roleId, AssignPermissionsCommand command) {
        // 验证角色存在
        findById(roleId);

        // 验证所有权限 ID 存在
        for (Long permissionId : command.permissionIds()) {
            permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new DomainException(AdminError.PERMISSION_NOT_FOUND));
        }

        roleRepository.assignPermissions(roleId, command.permissionIds());
    }
}
