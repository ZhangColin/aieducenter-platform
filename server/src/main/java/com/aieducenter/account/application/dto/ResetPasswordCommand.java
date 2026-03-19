package com.aieducenter.account.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 重置密码命令。
 *
 * @param account            用户名、邮箱或手机号
 * @param verificationCode   验证码
 * @param newPassword        新密码
 */
public record ResetPasswordCommand(
    @NotBlank String account,
    @NotBlank String verificationCode,
    @NotBlank String newPassword
) {}
