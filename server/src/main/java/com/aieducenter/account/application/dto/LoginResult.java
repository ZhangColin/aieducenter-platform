package com.aieducenter.account.application.dto;

import java.util.Objects;

/**
 * 登录结果。
 *
 * @param token JWT token
 */
public record LoginResult(
    String token
) {
    public LoginResult {
        Objects.requireNonNull(token, "token must not be null");
    }
}
