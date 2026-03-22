package com.aieducenter.admin.web.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.aieducenter.admin.application.MenuManagementAppService;
import com.aieducenter.admin.application.dto.command.CreateMenuCommand;
import com.aieducenter.admin.application.dto.command.UpdateMenuCommand;
import com.aieducenter.admin.application.dto.query.MenuDto;
import com.cartisan.security.annotation.RequireAuth;
import com.cartisan.security.annotation.RequirePermission;
import com.cartisan.web.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 菜单管理控制器。
 */
@RestController
@RequestMapping("/api/v1/admin/menus")
@Validated
@Tag(name = "Admin Menus", description = "菜单管理")
public class AdminMenuController {

    private final MenuManagementAppService menuManagementAppService;

    public AdminMenuController(MenuManagementAppService menuManagementAppService) {
        this.menuManagementAppService = menuManagementAppService;
    }

    @GetMapping
    @RequireAuth
    @RequirePermission(
        value = "admin:menu:read",
        name = "平台管理 / 菜单管理 / 查看",
        scope = "admin"
    )
    @Operation(summary = "查询菜单列表（树形）")
    public ApiResponse<List<MenuDto>> findTree() {
        return ApiResponse.ok(menuManagementAppService.findTreeAsDto());
    }

    @GetMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:menu:read",
        name = "平台管理 / 菜单管理 / 查看",
        scope = "admin"
    )
    @Operation(summary = "查询菜单详情")
    public ApiResponse<MenuDto> findById(@PathVariable Long id) {
        return ApiResponse.ok(menuManagementAppService.findByIdAsDto(id));
    }

    @PostMapping
    @RequireAuth
    @RequirePermission(
        value = "admin:menu:write",
        name = "平台管理 / 菜单管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "创建菜单")
    public ApiResponse<Long> create(@Valid @RequestBody CreateMenuCommand command) {
        Long id = menuManagementAppService.createAndReturnId(
            command.name(),
            command.path(),
            command.icon(),
            command.parentId(),
            command.sortOrder()
        );
        return ApiResponse.ok(id);
    }

    @PutMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:menu:write",
        name = "平台管理 / 菜单管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "更新菜单")
    public ApiResponse<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuCommand command) {
        menuManagementAppService.update(
            id,
            command.name(),
            command.path(),
            command.icon(),
            command.parentId(),
            command.sortOrder()
        );
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:menu:write",
        name = "平台管理 / 菜单管理 / 编辑",
        scope = "admin"
    )
    @Operation(summary = "删除菜单")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuManagementAppService.delete(id);
        return ApiResponse.ok();
    }
}
