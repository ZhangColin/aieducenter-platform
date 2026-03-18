package com.aieducenter.account.domain.error;

import com.cartisan.core.exception.CodeMessage;

/**
 * 用户模块错误码。
 *
 * <h3>错误码分类</h3>
 * <ul>
 *   <li>格式校验错误 (400): USERNAME_INVALID, EMAIL_INVALID, PHONE_NUMBER_INVALID</li>
 *   <li>唯一性错误 (409): USERNAME_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS, PHONE_NUMBER_ALREADY_EXISTS</li>
 *   <li>密码错误 (400): PASSWORD_INCORRECT, PASSWORD_WEAK, PASSWORD_SAME_AS_OLD</li>
 *   <li>资源不存在 (404): USER_NOT_FOUND</li>
 * </ul>
 *
 * @since 0.1.0
 */
public enum UserError implements CodeMessage {

    // ========== 格式校验错误 (400) ==========

    /**
     * 用户名格式不正确。
     * <p>要求：3-20 位，字母开头，允许字母/数字/下划线</p>
     */
    USERNAME_INVALID(400, "USER_001", "用户名格式不正确"),

    /**
     * 邮箱格式不正确。
     */
    EMAIL_INVALID(400, "USER_002", "邮箱格式不正确"),

    /**
     * 手机号格式不正确。
     * <p>要求：中国大陆手机号，1 开头，第二位 3-9</p>
     */
    PHONE_NUMBER_INVALID(400, "USER_003", "手机号格式不正确"),

    // ========== 唯一性错误 (409) ==========

    /**
     * 用户名已存在。
     */
    USERNAME_ALREADY_EXISTS(409, "USER_004", "用户名已存在"),

    /**
     * 邮箱已被使用。
     */
    EMAIL_ALREADY_EXISTS(409, "USER_005", "邮箱已被使用"),

    /**
     * 手机号已被使用。
     */
    PHONE_NUMBER_ALREADY_EXISTS(409, "USER_006", "手机号已被使用"),

    // ========== 密码错误 (400) ==========

    /**
     * 密码错误。
     */
    PASSWORD_INCORRECT(400, "USER_007", "密码错误"),

    /**
     * 密码强度不足。
     * <p>要求：8-20 位，包含字母和数字</p>
     */
    PASSWORD_WEAK(400, "USER_008", "密码强度不足"),

    /**
     * 新密码不能与旧密码相同。
     */
    PASSWORD_SAME_AS_OLD(400, "USER_009", "新密码不能与旧密码相同"),

    // ========== 资源不存在 (404) ==========

    /**
     * 用户不存在。
     */
    USER_NOT_FOUND(404, "USER_010", "用户不存在");

    private final int httpStatus;
    private final String code;
    private final String message;

    UserError(int httpStatus, String code, String message) {
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
