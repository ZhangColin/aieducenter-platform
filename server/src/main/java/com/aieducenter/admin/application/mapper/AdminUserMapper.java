package com.aieducenter.admin.application.mapper;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.aieducenter.admin.application.dto.response.AdminUserResponse;
import com.aieducenter.admin.domain.aggregate.AdminUser;
import com.cartisan.web.mapper.DomainMapper;

/**
 * 管理员 Mapper。
 *
 * @since 0.1.0
 */
@Mapper(componentModel = "spring")
public interface AdminUserMapper extends DomainMapper<AdminUser, AdminUserResponse> {

    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "avatar", source = "avatar")
    @Mapping(target = "status", source = "status")
    AdminUserResponse toDto(AdminUser adminUser);

    /**
     * 将 Optional&lt;String&gt; 转换为 String（null 处理）。
     */
    default String mapOptionalString(Optional<String> optional) {
        return optional.orElse(null);
    }

    /**
     * 将 AdminStatus 转换为字符串。
     */
    default String mapStatus(AdminUser.AdminStatus status) {
        return status != null ? status.name() : null;
    }
}
