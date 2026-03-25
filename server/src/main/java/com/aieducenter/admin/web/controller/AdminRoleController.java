package com.aieducenter.admin.web.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.aieducenter.admin.application.RoleManagementAppService;
import com.aieducenter.admin.application.dto.command.AssignMenusCommand;
import com.aieducenter.admin.application.dto.command.AssignPermissionsCommand;
import com.aieducenter.admin.application.dto.command.CreateRoleCommand;
import com.aieducenter.admin.application.dto.command.UpdateRoleCommand;
import com.aieducenter.admin.application.dto.query.AdminRoleQuery;
import com.aieducenter.admin.application.dto.response.RoleResponse;
import com.cartisan.security.annotation.RequireAuth;
import com.cartisan.security.annotation.RequirePermission;
import com.cartisan.web.response.PageResponse;

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
    @Operation(summary = "查询角色列表（分页）")
    public PageResponse<RoleResponse> findAll(
            AdminRoleQuery query,
            @PageableDefault(size = 20) Pageable pageable) {
        return roleManagementAppService.findAll(query, pageable);
    }

    @GetMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:role:read",
        name = "平台管理 / 角色管理 / 查看",
        scope = "admin"
    )
    @Operation(summary = "查询角色详情")
    public RoleResponse findById(@PathVariable Long id) {
        return roleManagementAppService.findByIdAsDto(id);
    }

    @PostMapping
    @RequireAuth
    @RequirePermission(
        value = "admin:role:write",
        name = "平台管理 / 角色管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "创建角色")
    public Long create(@Valid @RequestBody CreateRoleCommand command) {
        return roleManagementAppService.createAndReturnId(command);
    }

    @PutMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:role:write",
        name = "平台管理 / 角色管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "更新角色")
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleCommand command) {
        roleManagementAppService.update(id, command);
    }

    @DeleteMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:role:write",
        name = "平台管理 / 角色管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "删除角色")
    public void delete(@PathVariable Long id) {
        roleManagementAppService.delete(id);
    }

    @PutMapping("/{id}/menus")
    @RequireAuth
    @RequirePermission(
        value = "admin:role:write",
        name = "平台管理 / 角色管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "分配菜单")
    public void assignMenus(
            @PathVariable Long id,
            @Valid @RequestBody AssignMenusCommand command) {
        roleManagementAppService.assignMenus(id, command);
    }

    @PutMapping("/{id}/permissions")
    @RequireAuth
    @RequirePermission(
        value = "admin:role:write",
        name = "平台管理 / 角色管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "分配权限")
    public void assignPermissions(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionsCommand command) {
        roleManagementAppService.assignPermissions(id, command);
    }
}
