package com.aieducenter.admin.application.dto.command;

import jakarta.validation.constraints.NotBlank;

/**
 * 重置密码命令（管理员操作）。
 *
 * @since 0.1.0
 */
public record ResetPasswordCommand(
        @NotBlank(message = "新密码不能为空")
        String newPassword
) {}
