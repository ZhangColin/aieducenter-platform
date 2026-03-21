package com.aieducenter.admin.web.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.aieducenter.admin.application.AdminManagementAppService;
import com.aieducenter.admin.application.dto.command.AssignRolesCommand;
import com.aieducenter.admin.application.dto.command.CreateAdminCommand;
import com.aieducenter.admin.application.dto.command.UpdateAdminCommand;
import com.aieducenter.admin.application.dto.query.AdminDto;
import com.cartisan.security.annotation.RequireAuth;
import com.cartisan.security.annotation.RequirePermission;
import com.cartisan.web.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 管理员管理控制器。
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@Validated
@Tag(name = "Admin Users", description = "管理员管理")
public class AdminUserController {

    private final AdminManagementAppService adminManagementAppService;

    public AdminUserController(AdminManagementAppService adminManagementAppService) {
        this.adminManagementAppService = adminManagementAppService;
    }

    @GetMapping
    @RequireAuth
    @RequirePermission("admin:user:read")
    @Operation(summary = "查询管理员列表（分页）")
    public ApiResponse<com.cartisan.web.response.PageResponse<AdminDto>> findAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(adminManagementAppService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @RequireAuth
    @RequirePermission("admin:user:read")
    @Operation(summary = "查询管理员详情")
    public ApiResponse<AdminDto> findById(@PathVariable Long id) {
        return ApiResponse.ok(adminManagementAppService.findById(id));
    }

    @PostMapping
    @RequireAuth
    @RequirePermission("admin:user:write")
    @Operation(summary = "创建管理员")
    public ApiResponse<Long> create(@Valid @RequestBody CreateAdminCommand command) {
        return ApiResponse.ok(adminManagementAppService.create(command));
    }

    @PutMapping("/{id}")
    @RequireAuth
    @RequirePermission("admin:user:write")
    @Operation(summary = "更新管理员")
    public ApiResponse<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminCommand command) {
        adminManagementAppService.update(id, command);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @RequireAuth
    @RequirePermission("admin:user:delete")
    @Operation(summary = "删除管理员")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminManagementAppService.delete(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/status")
    @RequireAuth
    @RequirePermission("admin:user:write")
    @Operation(summary = "修改管理员状态")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        adminManagementAppService.updateStatus(id, status);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/roles")
    @RequireAuth
    @RequirePermission("admin:user:write")
    @Operation(summary = "分配角色")
    public ApiResponse<Void> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignRolesCommand command) {
        adminManagementAppService.assignRoles(id, command);
        return ApiResponse.ok();
    }
}
