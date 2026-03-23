package com.aieducenter.admin.application.dto.query;

import java.time.Instant;
import java.util.List;

import com.aieducenter.admin.application.dto.response.MenuResponse;
import com.aieducenter.admin.application.dto.response.RoleResponse;

/**
 * 登录结果 DTO。
 *
 * @since 0.1.0
 */
public record LoginResult(
        String token,
        Instant expireTime,
        AdminDto admin,
        List<RoleResponse> roles,
        List<MenuResponse> menus,
        List<String> permissions
) {
}
