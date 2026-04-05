package com.aieducenter.verification.domain.model;

import com.cartisan.core.domain.BaseEnum;

/**
 * 验证码类型。
 *
 * @since 0.1.0
 */
public enum VerificationType implements BaseEnum<VerificationType> {
    /**
     * 邮箱验证码。
     */
    EMAIL(1, "邮箱"),

    /**
     * 短信验证码（预留）。
     */
    SMS(2, "短信"),

    /**
     * 图形验证码。
     */
    CAPTCHA(3, "图形验证码");

    private final Integer code;
    private final String name;

    VerificationType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
