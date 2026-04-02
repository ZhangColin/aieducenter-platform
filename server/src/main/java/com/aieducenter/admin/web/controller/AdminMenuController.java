package com.aieducenter.admin.web.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.aieducenter.admin.application.MenuManagementAppService;
import com.aieducenter.admin.application.dto.command.CreateMenuCommand;
import com.aieducenter.admin.application.dto.command.UpdateMenuCommand;
import com.aieducenter.admin.application.dto.response.MenuResponse;
import com.aieducenter.admin.constants.AdminScopes;
import com.cartisan.security.annotation.RequireAuth;
import com.cartisan.security.annotation.RequirePermission;

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
        scope = AdminScopes.ADMIN
    )
    @Operation(summary = "查询菜单列表（树形）")
    public List<MenuResponse> findTree() {
        return menuManagementAppService.findTree();
    }

    @GetMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:menu:read",
        name = "平台管理 / 菜单管理 / 查看",
        scope = AdminScopes.ADMIN
    )
    @Operation(summary = "查询菜单详情")
    public MenuResponse findById(@PathVariable Long id) {
        return menuManagementAppService.findById(id);
    }

    @PostMapping
    @RequireAuth
    @RequirePermission(
        value = "admin:menu:write",
        name = "平台管理 / 菜单管理 / 编辑",
        scope = AdminScopes.ADMIN
    )
    @Operation(summary = "创建菜单")
    public Long create(@Valid @RequestBody CreateMenuCommand command) {
        return menuManagementAppService.create(command);
    }

    @PutMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:menu:write",
        name = "平台管理 / 菜单管理 / 编辑",
        scope = AdminScopes.ADMIN
    )
    @Operation(summary = "更新菜单")
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuCommand command) {
        menuManagementAppService.update(id, command);
    }

    @DeleteMapping("/{id}")
    @RequireAuth
    @RequirePermission(
        value = "admin:menu:write",
        name = "平台管理 / 菜单管理 / 编辑",
        scope = AdminScopes.ADMIN
    )
    @Operation(summary = "删除菜单")
    public void delete(@PathVariable Long id) {
        menuManagementAppService.delete(id);
    }
}
