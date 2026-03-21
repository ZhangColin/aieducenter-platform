package com.aieducenter.admin.application;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.admin.application.dto.command.AdminLoginCommand;
import com.aieducenter.admin.application.dto.query.AdminDto;
import com.aieducenter.admin.application.dto.query.LoginResult;
import com.aieducenter.admin.application.dto.query.MenuDto;
import com.aieducenter.admin.application.dto.query.PermissionDto;
import com.aieducenter.admin.application.dto.query.RoleDto;
import com.aieducenter.admin.domain.aggregate.Admin;
import com.aieducenter.admin.domain.error.AdminError;
import com.aieducenter.admin.domain.repository.AdminUserRepository;
import com.aieducenter.admin.domain.service.AdminPermissionService;

import cn.dev33.satoken.stp.StpLogic;
import com.cartisan.core.exception.ApplicationException;

/**
 * 管理员认证应用服务。
 *
 * @since 0.1.0
 */
@Service
public class AdminAuthAppService {

    private static final long DEFAULT_TIMEOUT = 28800;      // 8 小时
    private static final long REMEMBER_TIMEOUT = 604800;     // 7 天
    private static final String ADMIN_LOGIN_TYPE = "admin";

    private final AdminUserRepository adminUserRepository;
    private final AdminPermissionService adminPermissionService;

    public AdminAuthAppService(
            AdminUserRepository adminUserRepository,
            AdminPermissionService adminPermissionService) {
        this.adminUserRepository = adminUserRepository;
        this.adminPermissionService = adminPermissionService;
    }

    /**
     * 获取管理员专用的 StpLogic 实例。
     */
    private StpLogic adminStpLogic() {
        return new StpLogic(ADMIN_LOGIN_TYPE);
    }

    /**
     * 管理员登录。
     */
    @Transactional(readOnly = true)
    public LoginResult login(AdminLoginCommand command) {
        // 查询管理员
        Admin admin = adminUserRepository.findByUsername(command.username())
                .orElseThrow(() -> new ApplicationException(AdminError.LOGIN_FAILED));

        // 校验状态
        if (!admin.isActive()) {
            throw new ApplicationException(AdminError.ADMIN_DISABLED);
        }

        // 校验密码
        if (!admin.matchesPassword(command.password())) {
            throw new ApplicationException(AdminError.LOGIN_FAILED);
        }

        // 登录 Sa-Token（使用 admin loginType 隔离会话）
        long timeout = command.rememberMe() ? REMEMBER_TIMEOUT : DEFAULT_TIMEOUT;
        adminStpLogic().login(admin.getId(), timeout);

        // 获取 Token 信息
        String token = adminStpLogic().getTokenValue();
        Instant expireTime = Instant.now().plusSeconds(timeout);

        // 获取角色、菜单、权限
        List<String> roleCodes = adminPermissionService.getRoles(admin.getId());
        List<String> permissionCodes = adminPermissionService.getPermissions(admin.getId());
        List<MenuDto> menus = adminPermissionService.getMenus(admin.getId())
                .stream()
                .map(MenuDto::from)
                .toList();

        // 构建 DTO
        AdminDto adminDto = AdminDto.from(admin, List.of());
        List<RoleDto> roles = roleCodes.stream()
                .map(code -> new RoleDto(null, null, code, null, null, null, null))
                .toList();

        return new LoginResult(token, expireTime, adminDto, roles, menus, permissionCodes);
    }

    /**
     * 管理员登出。
     */
    public void logout() {
        adminStpLogic().logout();
    }

    /**
     * 获取当前管理员信息。
     */
    @Transactional(readOnly = true)
    public LoginResult getCurrentAdmin() {
        Long adminId = adminStpLogic().getLoginIdAsLong();

        Admin admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ApplicationException(AdminError.ADMIN_NOT_FOUND));

        String token = adminStpLogic().getTokenValue();
        Instant expireTime = Instant.ofEpochSecond(adminStpLogic().getTokenTimeout());

        List<String> roleCodes = adminPermissionService.getRoles(admin.getId());
        List<String> permissionCodes = adminPermissionService.getPermissions(admin.getId());
        List<MenuDto> menus = adminPermissionService.getMenus(admin.getId())
                .stream()
                .map(MenuDto::from)
                .toList();

        AdminDto adminDto = AdminDto.from(admin, List.of());
        List<RoleDto> roles = roleCodes.stream()
                .map(code -> new RoleDto(null, null, code, null, null, null, null))
                .toList();

        return new LoginResult(token, expireTime, adminDto, roles, menus, permissionCodes);
    }

    /**
     * 修改当前管理员密码。
     */
    @Transactional
    public void updatePassword(String oldPassword, String newPassword) {
        Long adminId = adminStpLogic().getLoginIdAsLong();
        Admin admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ApplicationException(AdminError.ADMIN_NOT_FOUND));

        admin.updatePassword(oldPassword, newPassword);
        adminUserRepository.save(admin);
    }
}
