package com.aieducenter.account.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 短信验证码登录命令。
 *
 * @param phone 手机号
 * @param code  6 位短信验证码
 */
public record LoginBySmsCommand(
    @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$") String phone,
    @NotBlank @Size(min = 6, max = 6) String code
) {}
