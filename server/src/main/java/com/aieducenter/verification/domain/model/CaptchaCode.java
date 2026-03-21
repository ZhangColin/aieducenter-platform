package com.aieducenter.verification.domain.model;

import java.util.Objects;

/**
 * 图形验证码值对象。
 */
public class CaptchaCode {

    private final String id;
    private final String code;

    public CaptchaCode(String id, String code) {
        this.id = Objects.requireNonNull(id, "captchaId cannot be null");
        this.code = Objects.requireNonNull(code, "code cannot be null");
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}
