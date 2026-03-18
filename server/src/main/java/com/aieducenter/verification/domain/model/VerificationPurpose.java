package com.aieducenter.verification.domain.model;

/**
 * 验证码使用目的。
 *
 * @since 0.1.0
 */
public enum VerificationPurpose {
    /**
     * 用户注册。
     */
    REGISTER,

    /**
     * 密码重置。
     */
    RESET_PASSWORD
}
