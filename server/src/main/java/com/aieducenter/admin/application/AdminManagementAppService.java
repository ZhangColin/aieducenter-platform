package com.aieducenter.admin.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.admin.application.dto.command.AssignRolesCommand;
import com.aieducenter.admin.application.dto.command.CreateAdminCommand;
import com.aieducenter.admin.application.dto.command.UpdateAdminCommand;
import com.aieducenter.admin.application.dto.response.AdminUserResponse;
import com.aieducenter.admin.domain.aggregate.AdminUser;
import com.aieducenter.admin.domain.aggregate.AdminRole;
import com.aieducenter.admin.domain.error.AdminMessage;
import com.aieducenter.admin.domain.repository.AdminRoleRepository;
import com.aieducenter.admin.domain.repository.AdminUserRepository;

import com.cartisan.core.exception.ApplicationException;
import com.cartisan.core.util.Assertions;

import com.cartisan.web.response.PageResponse;

/**
 * 管理员管理应用服务。
 *
 * @since 0.1.0
 */
@Service
public class AdminManagementAppService {

    private final AdminUserRepository adminUserRepository;
    private final AdminRoleRepository adminRoleRepository;

    public AdminManagementAppService(
            AdminUserRepository adminUserRepository,
            AdminRoleRepository adminRoleRepository) {
        this.adminUserRepository = adminUserRepository;
        this.adminRoleRepository = adminRoleRepository;
    }

    /**
     * 查询管理员列表（分页）。
     */
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> findAll(int page, int size) {
        // 参数校验
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 20;
        } else if (size > 100) {
            size = 100;
        }

        long total = adminUserRepository.count();
        long offset = (long) (page - 1) * size;

        List<AdminUserResponse> items = adminUserRepository.findAll().stream()
                .skip(offset)
                .limit(size)
                .map(AdminUserResponse::from)
                .toList();

        return new PageResponse<>(items, total, page, size);
    }

    /**
     * 查询管理员详情。
     */
    @Transactional(readOnly = true)
    public AdminUserResponse findById(Long id) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        return AdminUserResponse.from(adminUser);
    }

    /**
     * 创建管理员。
     */
    @Transactional
    public Long create(CreateAdminCommand command) {
        Assertions.require(
                !adminUserRepository.existsByUsername(command.username()),
                AdminMessage.USERNAME_ALREADY_EXISTS
        );

        AdminUser adminUser = new AdminUser(command.username(), command.password(), command.nickname());
        if (command.email() != null) {
            adminUser.updateEmail(command.email());
        }
        if (command.phone() != null) {
            adminUser.updatePhone(command.phone());
        }

        AdminUser saved = adminUserRepository.save(adminUser);
        return saved.getId();
    }

    /**
     * 更新管理员。
     */
    @Transactional
    public void update(Long id, UpdateAdminCommand command) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        if (command.nickname() != null) {
            adminUser.updateNickname(command.nickname());
        }
        if (command.email() != null) {
            adminUser.updateEmail(command.email());
        }
        if (command.phone() != null) {
            adminUser.updatePhone(command.phone());
        }
        if (command.avatar() != null) {
            adminUser.updateAvatar(command.avatar());
        }

        adminUserRepository.save(adminUser);
    }

    /**
     * 删除管理员。
     */
    @Transactional
    public void delete(Long id) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        adminUser.checkCanBeDeleted();
        adminUserRepository.delete(adminUser);
    }

    /**
     * 修改管理员状态。
     */
    @Transactional
    public void updateStatus(Long id, String status) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        if ("ACTIVE".equals(status)) {
            adminUser.enable();
        } else if ("DISABLED".equals(status)) {
            adminUser.disable();
        }

        adminUserRepository.save(adminUser);
    }

    /**
     * 为管理员分配角色。
     */
    @Transactional
    public void assignRoles(Long id, AssignRolesCommand command) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        // 验证所有角色 ID 存在
        for (Long roleId : command.roleIds()) {
            adminRoleRepository.findById(roleId)
                    .orElseThrow(() -> new ApplicationException(AdminMessage.ROLE_NOT_FOUND));
        }

        // 清除现有角色关联
        adminUser.clearRoles();

        // 添加新角色关联
        for (Long roleId : command.roleIds()) {
            adminUser.addRole(roleId);
        }

        adminUserRepository.save(adminUser);
    }
}
