package com.aieducenter.account.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 密码登录命令。
 *
 * @param account  用户名、邮箱或手机号
 * @param password 明文密码
 */
public record LoginByPasswordCommand(
    @NotBlank String account,
    @NotBlank String password
) {}
