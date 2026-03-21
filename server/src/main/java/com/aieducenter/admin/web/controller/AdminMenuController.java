package com.aieducenter.admin.web.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.aieducenter.admin.application.MenuManagementAppService;
import com.aieducenter.admin.application.dto.command.CreateMenuCommand;
import com.aieducenter.admin.application.dto.command.UpdateMenuCommand;
import com.aieducenter.admin.application.dto.query.MenuDto;
import com.aieducenter.admin.domain.entity.AdminMenu;
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
    @Operation(summary = "查询菜单列表（树形）")
    public ApiResponse<List<MenuDto>> findTree() {
        List<AdminMenu> menus = menuManagementAppService.findTree();
        List<MenuDto> dtos = menus.stream()
            .map(menu -> MenuDto.from(menu))
            .collect(Collectors.toList());
        return ApiResponse.ok(dtos);
    }

    @GetMapping("/{id}")
    @RequireAuth
    @RequirePermission("admin:menu:read")
    @Operation(summary = "查询菜单详情")
    public ApiResponse<MenuDto> findById(@PathVariable Long id) {
        AdminMenu menu = menuManagementAppService.findById(id);
        return ApiResponse.ok(MenuDto.from(menu));
    }

    @PostMapping
    @RequireAuth
    @RequirePermission("admin:menu:write")
    @Operation(summary = "创建菜单")
    public ApiResponse<Long> create(@Valid @RequestBody CreateMenuCommand command) {
        AdminMenu menu = menuManagementAppService.create(
            command.name(),
            command.path(),
            command.icon(),
            command.parentId(),
            command.sortOrder()
        );
        return ApiResponse.ok(menu.getId());
    }

    @PutMapping("/{id}")
    @RequireAuth
    @RequirePermission("admin:menu:write")
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
    @RequirePermission("admin:menu:write")
    @Operation(summary = "删除菜单")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuManagementAppService.delete(id);
        return ApiResponse.ok();
    }
}
