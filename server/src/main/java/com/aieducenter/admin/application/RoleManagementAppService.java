package com.aieducenter.admin.application;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.admin.domain.aggregate.AdminRole;
import com.aieducenter.admin.domain.repository.AdminRoleRepository;
import com.aieducenter.admin.domain.repository.AdminMenuRepository;
import com.aieducenter.admin.domain.error.AdminMessage;
import com.aieducenter.admin.application.dto.command.AssignMenusCommand;
import com.aieducenter.admin.application.dto.command.AssignPermissionsCommand;
import com.aieducenter.admin.application.dto.response.RoleResponse;
import com.cartisan.core.exception.DomainException;

/**
 * 角色管理应用服务。
 */
@Service
public class RoleManagementAppService {

    private final AdminRoleRepository roleRepository;
    private final AdminMenuRepository menuRepository;

    public RoleManagementAppService(AdminRoleRepository roleRepository,
                                     AdminMenuRepository menuRepository) {
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
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
                .orElseThrow(() -> new DomainException(AdminMessage.ROLE_NOT_FOUND));
    }

    /**
     * 创建角色。
     */
    @Transactional
    public AdminRole create(String name, String code, String description, Integer sortOrder) {
        // 检查 code 唯一性
        if (roleRepository.findByCode(code).isPresent()) {
            throw new DomainException(AdminMessage.ROLE_CODE_ALREADY_EXISTS);
        }

        AdminRole role = new AdminRole(name, code, description, sortOrder);
        return roleRepository.save(role);
    }

    /**
     * 创建角色并返回 ID。
     */
    @Transactional
    public Long createAndReturnId(String name, String code, String description, Integer sortOrder) {
        return create(name, code, description, sortOrder).getId();
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
                throw new DomainException(AdminMessage.ROLE_CODE_ALREADY_EXISTS);
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
            throw new DomainException(AdminMessage.SUPER_ADMIN_CANNOT_DELETE);
        }

        // 检查是否有管理员使用该角色
        if (roleRepository.isUsedByAnyAdmin(id)) {
            throw new DomainException(AdminMessage.ROLE_IN_USE);
        }

        roleRepository.delete(role);
    }

    /**
     * 为角色分配菜单。
     */
    @Transactional
    public void assignMenus(Long roleId, AssignMenusCommand command) {
        // 验证角色存在
        AdminRole role = findById(roleId);

        // 验证所有菜单 ID 存在
        for (Long menuId : command.menuIds()) {
            if (menuRepository.findById(menuId).isEmpty()) {
                throw new DomainException(AdminMessage.MENU_NOT_FOUND);
            }
        }

        // 清除现有菜单并添加新菜单
        role.clearMenus();
        for (Long menuId : command.menuIds()) {
            role.addMenu(menuId);
        }
        roleRepository.save(role);
    }

    /**
     * 为角色分配权限。
     */
    @Transactional
    public void assignPermissions(Long roleId, AssignPermissionsCommand command) {
        // 验证角色存在
        AdminRole role = findById(roleId);

        // TODO: 通过 PermissionScanner 验证权限 codes 有效性
        // 暂时不验证，直接存储

        // 清除现有权限并添加新权限
        role.clearPermissions();
        for (String permissionCode : command.permissionCodes()) {
            role.addPermission(permissionCode, null);
        }
        roleRepository.save(role);
    }

    // ========== DTO 返回方法 ==========

    /**
     * 获取所有角色列表（DTO）。
     */
    public List<RoleResponse> findAllAsDto() {
        List<AdminRole> roles = roleRepository.findAll();
        return roles.stream().map(RoleResponse::from).collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取角色详情（DTO）。
     */
    public RoleResponse findByIdAsDto(Long id) {
        AdminRole role = findById(id);
        return RoleResponse.from(role);
    }
}
