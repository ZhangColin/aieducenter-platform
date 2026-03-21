package com.aieducenter.admin.domain.error;

import com.cartisan.core.exception.CodeMessage;

/**
 * 管理员模块错误码。
 *
 * <h3>错误码分类</h3>
 * <ul>
 *   <li>格式校验错误 (400): USERNAME_INVALID, PASSWORD_WEAK</li>
 *   <li>唯一性错误 (409): USERNAME_ALREADY_EXISTS</li>
 *   <li>密码错误 (400): PASSWORD_INCORRECT</li>
 *   <li>资源不存在 (404): ADMIN_NOT_FOUND, ROLE_NOT_FOUND</li>
 *   <li>登录错误 (401): LOGIN_FAILED</li>
 *   <li>业务限制 (403): ADMIN_SYSTEM_CANNOT_DELETE, ROLE_IN_USE</li>
 * </ul>
 *
 * @since 0.1.0
 */
public enum AdminError implements CodeMessage {

    // ========== 格式校验错误 (400) ==========

    /**
     * 用户名格式不正确。
     * <p>要求：3-20 位，字母开头，允许字母/数字/下划线</p>
     */
    USERNAME_INVALID(400, "ADMIN_001", "用户名格式不正确"),

    /**
     * 密码强度不足。
     * <p>要求：8-20 位，包含字母和数字</p>
     */
    PASSWORD_WEAK(400, "ADMIN_002", "密码强度不足"),

    // ========== 唯一性错误 (409) ==========

    /**
     * 用户名已存在。
     */
    USERNAME_ALREADY_EXISTS(409, "ADMIN_003", "用户名已存在"),

    /**
     * 角色编码已存在。
     */
    ROLE_CODE_ALREADY_EXISTS(409, "ADMIN_004", "角色编码已存在"),

    // ========== 密码错误 (400) ==========

    /**
     * 密码错误。
     */
    PASSWORD_INCORRECT(400, "ADMIN_005", "密码错误"),

    // ========== 资源不存在 (404) ==========

    /**
     * 管理员不存在。
     */
    ADMIN_NOT_FOUND(404, "ADMIN_006", "管理员不存在"),

    /**
     * 角色不存在。
     */
    ROLE_NOT_FOUND(404, "ADMIN_007", "角色不存在"),

    /**
     * 菜单不存在。
     */
    MENU_NOT_FOUND(404, "ADMIN_008", "菜单不存在"),

    /**
     * 权限不存在。
     */
    PERMISSION_NOT_FOUND(404, "ADMIN_009", "权限不存在"),

    // ========== 登录错误 (401) ==========

    /**
     * 登录失败。
     * <p>返回 401 而非 404 是为了防止用户枚举攻击</p>
     */
    LOGIN_FAILED(401, "ADMIN_010", "用户名或密码错误"),

    /**
     * 管理员已被禁用。
     */
    ADMIN_DISABLED(401, "ADMIN_011", "管理员已被禁用"),

    // ========== 业务限制 (403) ==========

    /**
     * 系统内置管理员不能删除。
     */
    ADMIN_SYSTEM_CANNOT_DELETE(403, "ADMIN_012", "系统内置管理员不能删除"),

    /**
     * 角色正在使用中，不能删除。
     */
    ROLE_IN_USE(403, "ADMIN_013", "角色正在使用中，不能删除"),

    /**
     * 菜单有子菜单，不能删除。
     */
    MENU_HAS_CHILDREN(403, "ADMIN_014", "菜单有子菜单，不能删除");

    private final int httpStatus;
    private final String code;
    private final String message;

    AdminError(int httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
