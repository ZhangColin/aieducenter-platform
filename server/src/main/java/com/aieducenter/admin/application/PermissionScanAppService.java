package com.aieducenter.admin.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aieducenter.admin.application.dto.query.PermissionDto;
import com.cartisan.security.permission.Permission;
import com.cartisan.security.permission.PermissionScanner;

/**
 * 权限扫描应用服务。
 */
@Service
public class PermissionScanAppService {

    private final PermissionScanner permissionScanner;

    public PermissionScanAppService(PermissionScanner permissionScanner) {
        this.permissionScanner = permissionScanner;
    }

    /**
     * 扫描指定 scope 的权限。
     */
    public List<PermissionDto> scanByScope(String scope) {
        return permissionScanner.scanByScope(scope).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 扫描所有权限。
     */
    public List<PermissionDto> scanAll() {
        return permissionScanner.scanAll().stream()
                .map(this::toDto)
                .toList();
    }

    private PermissionDto toDto(Permission permission) {
        return new PermissionDto(permission.code(), permission.name());
    }
}
