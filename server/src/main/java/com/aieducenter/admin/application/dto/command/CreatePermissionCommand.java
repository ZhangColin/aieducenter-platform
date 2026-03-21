package com.aieducenter.admin.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建权限命令。
 *
 * @since 0.1.0
 */
public record CreatePermissionCommand(

        @NotBlank(message = "权限名称不能为空")
        @Size(max = 50, message = "权限名称长度不能超过50")
        String name,

        @NotBlank(message = "权限编码不能为空")
        @Size(max = 100, message = "权限编码长度不能超过100")
        String code,

        Long menuId,

        Integer sortOrder

) {
}
