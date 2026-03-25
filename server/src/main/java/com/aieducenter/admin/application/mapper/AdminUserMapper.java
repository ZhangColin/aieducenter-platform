package com.aieducenter.admin.application.mapper;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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

    @Override
    @Mapping(target = "statusName", source = "status", qualifiedByName = "getStatusName")
    AdminUserResponse convert(AdminUser source);

    /**
     * 将 Optional&lt;String&gt; 转换为 String（null 处理）。
     */
    default String map(Optional<String> optional) {
        return optional.orElse(null);
    }

    /**
     * 获取枚举显示名称。
     */
    @Named("getStatusName")
    default String getStatusName(AdminUser.AdminStatus status) {
        return status != null ? status.getName() : null;
    }
}
