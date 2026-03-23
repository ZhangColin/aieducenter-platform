package com.aieducenter.admin.application;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.admin.application.dto.command.AdminUserLoginCommand;
import com.aieducenter.admin.application.dto.query.AdminDto;
import com.aieducenter.admin.application.dto.query.LoginResult;
import com.aieducenter.admin.application.dto.response.MenuResponse;
import com.aieducenter.admin.application.dto.response.RoleResponse;
import com.aieducenter.admin.domain.aggregate.AdminUser;
import com.aieducenter.admin.domain.repository.AdminUserRepository;
import com.cartisan.core.exception.ApplicationException;
import com.aieducenter.admin.domain.error.AdminMessage;

import cn.dev33.satoken.stp.StpLogic;

/**
 * 管理员认证应用服务。
 *
 * @since 0.1.0
 */
@Service
public class AdminAuthAppService {

    private static final long DEFAULT_TIMEOUT = 86400; // 24 小时
    private static final long REMEMBER_TIMEOUT = 604800; // 7 天

    private final AdminUserRepository adminUserRepository;
    private final AdminPermissionAppService adminPermissionAppService;
    private final StpLogic adminStpLogic;

    public AdminAuthAppService(
            AdminUserRepository adminUserRepository,
            AdminPermissionAppService adminPermissionAppService,
            StpLogic adminStpLogic) {
        this.adminUserRepository = adminUserRepository;
        this.adminPermissionAppService = adminPermissionAppService;
        this.adminStpLogic = adminStpLogic;
    }

    /**
     * 管理员登录。
     */
    @Transactional
    public LoginResult login(AdminUserLoginCommand command) {
        // 验证用户名和密码
        AdminUser adminUser = adminUserRepository.findByUsername(command.username())
                .orElseThrow(() -> new ApplicationException(AdminMessage.LOGIN_FAILED));

        if (!adminUser.isActive()) {
            throw new ApplicationException(AdminMessage.ADMIN_DISABLED);
        }

        if (!adminUser.matchesPassword(command.password())) {
            throw new ApplicationException(AdminMessage.LOGIN_FAILED);
        }

        // 登录 Sa-Token（使用 admin loginType 隔离会话）
        long timeout = command.rememberMe() ? REMEMBER_TIMEOUT : DEFAULT_TIMEOUT;
        adminStpLogic.login(adminUser.getId(), timeout);

        // 获取 Token 信息
        String token = adminStpLogic.getTokenValue();
        Instant expireTime = Instant.now().plusSeconds(timeout);

        // 获取角色、菜单、权限
        List<String> roleCodes = adminPermissionAppService.getRoleCodes(adminUser.getId());
        List<String> permissionCodes = adminPermissionAppService.getPermissions(adminUser.getId());
        List<MenuResponse> menus = adminPermissionAppService.getMenus(adminUser.getId());

        // 构建 DTO
        List<RoleResponse> roles = roleCodes.stream()
                .map(code -> new RoleResponse(null, null, code, null, null, null, null))
                .toList();
        AdminDto adminDto = AdminDto.from(adminUser, roles);

        return new LoginResult(token, expireTime, adminDto, roles, menus, permissionCodes);
    }

    /**
     * 管理员登出。
     */
    public void logout() {
        adminStpLogic.logout();
    }

    /**
     * 修改当前管理员密码。
     */
    @Transactional
    public void updatePassword(String oldPassword, String newPassword) {
        Long adminId = adminStpLogic.getLoginIdAsLong();
        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        adminUser.updatePassword(oldPassword, newPassword);
        adminUserRepository.save(adminUser);
    }

    /**
     * 获取当前管理员信息。
     */
    @Transactional(readOnly = true)
    public LoginResult getCurrentAdmin() {
        Long adminId = adminStpLogic.getLoginIdAsLong();
        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ApplicationException(AdminMessage.ADMIN_NOT_FOUND));

        // 获取角色、菜单、权限
        List<String> roleCodes = adminPermissionAppService.getRoleCodes(adminId);
        List<String> permissionCodes = adminPermissionAppService.getPermissions(adminId);
        List<MenuResponse> menus = adminPermissionAppService.getMenus(adminId);

        // 构建 DTO
        List<RoleResponse> roles = roleCodes.stream()
                .map(code -> new RoleResponse(null, null, code, null, null, null, null))
                .toList();
        AdminDto adminDto = AdminDto.from(adminUser, roles);

        // SaTokenContext 可能为 null（未登录场景）
        String token = adminStpLogic.getTokenValue();
        Instant expireTime = null;
        if (token != null) {
            long timeout = adminStpLogic.getTokenTimeout();
            if (timeout > 0) {
                expireTime = Instant.now().plusSeconds(timeout);
            }
        }

        return new LoginResult(token, expireTime, adminDto, roles, menus, permissionCodes);
    }
}
