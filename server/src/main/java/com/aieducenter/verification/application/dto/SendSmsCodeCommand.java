package com.aieducenter.verification.application.dto;

/**
 * 发送短信验证码命令。
 *
 * @param phone 手机号
 * @param purpose 验证码目的（REGISTER/RESET_PASSWORD/LOGIN）
 */
public record SendSmsCodeCommand(
    String phone,
    String purpose
) {}
