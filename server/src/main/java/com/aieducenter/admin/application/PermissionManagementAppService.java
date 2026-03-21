package com.aieducenter.admin.application;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.admin.domain.entity.AdminPermission;
import com.aieducenter.admin.domain.repository.AdminPermissionRepository;
import com.aieducenter.admin.domain.repository.AdminMenuRepository;
import com.aieducenter.admin.domain.error.AdminError;
import com.aieducenter.admin.application.dto.query.PermissionDto;
import com.cartisan.core.exception.DomainException;

/**
 * 权限管理应用服务。
 */
@Service
public class PermissionManagementAppService {

    private final AdminPermissionRepository permissionRepository;
    private final AdminMenuRepository menuRepository;

    public PermissionManagementAppService(AdminPermissionRepository permissionRepository,
                                          AdminMenuRepository menuRepository) {
        this.permissionRepository = permissionRepository;
        this.menuRepository = menuRepository;
    }

    /**
     * 获取所有权限列表。
     */
    public List<AdminPermission> findAll() {
        return permissionRepository.findAll();
    }

    /**
     * 根据 ID 获取权限详情。
     */
    public AdminPermission findById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new DomainException(AdminError.PERMISSION_NOT_FOUND));
    }

    /**
     * 创建权限。
     */
    @Transactional
    public AdminPermission create(String name, String code, Long menuId, Integer sortOrder) {
        // 检查 code 唯一性
        if (permissionRepository.findByCode(code).isPresent()) {
            throw new DomainException(AdminError.PERMISSION_CODE_ALREADY_EXISTS);
        }

        // 验证菜单存在
        if (menuId != null) {
            menuRepository.findById(menuId)
                    .orElseThrow(() -> new DomainException(AdminError.MENU_NOT_FOUND));
        }

        AdminPermission permission = new AdminPermission(name, code, menuId, sortOrder);
        return permissionRepository.save(permission);
    }

    /**
     * 创建权限并返回 ID。
     */
    @Transactional
    public Long createAndReturnId(String name, String code, Long menuId, Integer sortOrder) {
        return create(name, code, menuId, sortOrder).getId();
    }

    /**
     * 修改权限。
     */
    @Transactional
    public AdminPermission update(Long id, String name, String code, Long menuId, Integer sortOrder) {
        AdminPermission permission = findById(id);

        // 如果修改 code，检查唯一性
        if (!Objects.equals(permission.getCode(), code)) {
            permissionRepository.findByCode(code).ifPresent(existing -> {
                throw new DomainException(AdminError.PERMISSION_CODE_ALREADY_EXISTS);
            });
        }

        // 验证菜单存在
        if (menuId != null) {
            menuRepository.findById(menuId)
                    .orElseThrow(() -> new DomainException(AdminError.MENU_NOT_FOUND));
        }

        permission.setName(name);
        permission.setCode(code);
        permission.setMenuId(menuId);
        permission.setSortOrder(sortOrder);
        return permissionRepository.save(permission);
    }

    /**
     * 删除权限。
     */
    @Transactional
    public void delete(Long id) {
        // 验证存在
        AdminPermission permission = findById(id);
        permissionRepository.delete(permission);
    }

    // ========== DTO 返回方法 ==========

    /**
     * 获取所有权限列表（DTO）。
     */
    public List<PermissionDto> findAllAsDto() {
        List<AdminPermission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(PermissionDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取权限详情（DTO）。
     */
    public PermissionDto findByIdAsDto(Long id) {
        AdminPermission permission = findById(id);
        return PermissionDto.from(permission);
    }
}
