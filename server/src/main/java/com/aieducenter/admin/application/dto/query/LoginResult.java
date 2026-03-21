package com.aieducenter.admin.application.dto.query;

import java.time.Instant;
import java.util.List;

/**
 * 登录结果 DTO。
 *
 * @since 0.1.0
 */
public record LoginResult(
        String token,
        Instant expireTime,
        AdminDto admin,
        List<RoleDto> roles,
        List<MenuDto> menus,
        List<String> permissions
) {
}
