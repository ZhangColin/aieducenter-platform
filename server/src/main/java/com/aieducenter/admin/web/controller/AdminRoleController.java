package com.aieducenter.admin.web.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.aieducenter.admin.application.RoleManagementAppService;
import com.aieducenter.admin.application.dto.command.AssignMenusCommand;
import com.aieducenter.admin.application.dto.command.AssignPermissionsCommand;
import com.aieducenter.admin.application.dto.command.CreateRoleCommand;
import com.aieducenter.admin.application.dto.command.UpdateRoleCommand;
import com.aieducenter.admin.application.dto.query.RoleDto;
import com.cartisan.security.annotation.RequireAuth;
import com.cartisan.security.annotation.RequirePermission;
import com.cartisan.web.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 角色管理控制器。
 */
@RestController
@RequestMapping("/api/v1/admin/roles")
@Validated
@Tag(name = "Admin Roles", description = "角色管理")
public class AdminRoleController {

    private final RoleManagementAppService roleManagementAppService;

    public AdminRoleController(RoleManagementAppService roleManagementAppService) {
        this.roleManagementAppService = roleManagementAppService;
    }

    @GetMapping
    @RequireAuth
    @RequirePermission(
        value = "admin:role:read",
        name = "平台管理 / 角色管理 / 查看",
        scope = "admin"
    )
    @Operation(summary = "查询角色列表")
    public ApiResponse<List<RoleDto>> findAll() {
        return ApiResponse.ok(roleManagementAppService.findAllAsDto());
    }

    @GetMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:role:read",
        name = "平台管理 / 角色管理 / 查看",
        scope = "admin"
    )
    @Operation(summary = "查询角色详情")
    public ApiResponse<RoleDto> findById(@PathVariable Long id) {
        return ApiResponse.ok(roleManagementAppService.findByIdAsDto(id));
    }

    @PostMapping
    @RequireAuth
    @RequirePermission(
        value = "admin:role:write",
        name = "平台管理 / 角色管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "创建角色")
    public ApiResponse<Long> create(@Valid @RequestBody CreateRoleCommand command) {
        Long id = roleManagementAppService.createAndReturnId(
            command.name(),
            command.code(),
            command.description(),
            command.sortOrder()
        );
        return ApiResponse.ok(id);
    }

    @PutMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:role:write",
        name = "平台管理 / 角色管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "更新角色")
    public ApiResponse<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleCommand command) {
        roleManagementAppService.update(
            id,
            command.name(),
            command.code(),
            command.description(),
            command.sortOrder()
        );
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:role:write",
        name = "平台管理 / 角色管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "删除角色")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleManagementAppService.delete(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/menus")
    @RequireAuth
    @RequirePermission(
        value = "admin:role:write",
        name = "平台管理 / 角色管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "分配菜单")
    public ApiResponse<Void> assignMenus(
            @PathVariable Long id,
            @Valid @RequestBody AssignMenusCommand command) {
        roleManagementAppService.assignMenus(id, command);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/permissions")
    @RequireAuth
    @RequirePermission(
        value = "admin:role:write",
        name = "平台管理 / 角色管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "分配权限")
    public ApiResponse<Void> assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionsCommand command) {
        roleManagementAppService.assignPermissions(id, command);
        return ApiResponse.ok();
    }
}
