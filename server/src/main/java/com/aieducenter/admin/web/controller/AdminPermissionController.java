package com.aieducenter.admin.web.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.aieducenter.admin.application.PermissionManagementAppService;
import com.aieducenter.admin.application.dto.command.CreatePermissionCommand;
import com.aieducenter.admin.application.dto.query.PermissionDto;
import com.cartisan.security.annotation.RequireAuth;
import com.cartisan.security.annotation.RequirePermission;
import com.cartisan.web.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 权限管理控制器。
 */
@RestController
@RequestMapping("/api/v1/admin/permissions")
@Validated
@Tag(name = "Admin Permissions", description = "权限管理")
public class AdminPermissionController {

    private final PermissionManagementAppService permissionManagementAppService;

    public AdminPermissionController(PermissionManagementAppService permissionManagementAppService) {
        this.permissionManagementAppService = permissionManagementAppService;
    }

    @GetMapping
    @RequireAuth
    @RequirePermission("admin:permission:read")
    @Operation(summary = "查询权限列表")
    public ApiResponse<List<PermissionDto>> findAll() {
        return ApiResponse.ok(permissionManagementAppService.findAllAsDto());
    }

    @GetMapping("/{id}")
    @RequireAuth
    @RequirePermission("admin:permission:read")
    @Operation(summary = "查询权限详情")
    public ApiResponse<PermissionDto> findById(@PathVariable Long id) {
        return ApiResponse.ok(permissionManagementAppService.findByIdAsDto(id));
    }

    @PostMapping
    @RequireAuth
    @RequirePermission("admin:permission:write")
    @Operation(summary = "创建权限")
    public ApiResponse<Long> create(@Valid @RequestBody CreatePermissionCommand command) {
        Long id = permissionManagementAppService.createAndReturnId(
            command.name(),
            command.code(),
            command.menuId(),
            command.sortOrder()
        );
        return ApiResponse.ok(id);
    }

    @DeleteMapping("/{id}")
    @RequireAuth
    @RequirePermission("admin:permission:write")
    @Operation(summary = "删除权限")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionManagementAppService.delete(id);
        return ApiResponse.ok();
    }
}
