package com.aieducenter.admin.application.dto.command;

import jakarta.validation.constraints.Size;

/**
 * 更新角色命令。
 *
 * @since 0.1.0
 */
public record UpdateRoleCommand(

        @Size(max = 50, message = "角色名称长度不能超过50")
        String name,

        @Size(max = 255, message = "描述长度不能超过255")
        String description,

        Integer sortOrder

) {
}
