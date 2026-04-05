package com.aieducenter.account.application.dto.command;

import jakarta.validation.constraints.NotBlank;

/**
 * 密码登录命令。
 *
 * @param account  用户名、邮箱或手机号
 * @param password 明文密码
 * @param captchaId 图形验证码ID
 * @param captchaCode 图形验证码
 */
public record LoginByPasswordCommand(
    @NotBlank String account,
    @NotBlank String password,
    @NotBlank String captchaId,
    @NotBlank String captchaCode
) {}
