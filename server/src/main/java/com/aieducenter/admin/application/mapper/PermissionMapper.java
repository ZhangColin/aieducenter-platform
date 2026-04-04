package com.aieducenter.admin.application.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.aieducenter.admin.application.dto.response.PermissionResponse;
import com.cartisan.security.permission.Permission;

/**
 * 权限 Mapper。
 *
 * @since 0.1.0
 */
@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse convert(Permission permission);

    List<PermissionResponse> convertList(List<Permission> permissions);
}
