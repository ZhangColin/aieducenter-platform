package com.aieducenter.admin.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.admin.domain.entity.AdminMenu;
import com.aieducenter.admin.domain.repository.AdminMenuRepository;
import com.aieducenter.admin.domain.error.AdminError;
import com.aieducenter.admin.application.dto.query.MenuDto;
import com.cartisan.core.exception.DomainException;

/**
 * 菜单管理应用服务。
 */
@Service
public class MenuManagementAppService {

    private final AdminMenuRepository menuRepository;

    public MenuManagementAppService(AdminMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    /**
     * 获取菜单树（3层）。
     */
    public List<AdminMenu> findTree() {
        List<AdminMenu> allMenus = menuRepository.findAll();

        // 组装树形结构
        Map<Long, AdminMenu> menuMap = new HashMap<>();
        List<AdminMenu> rootMenus = new ArrayList<>();

        for (AdminMenu menu : allMenus) {
            menuMap.put(menu.getId(), menu);
        }

        for (AdminMenu menu : allMenus) {
            if (menu.getParentId() == null) {
                rootMenus.add(menu);
            } else {
                AdminMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    parent.addChild(menu);
                }
            }
        }

        return rootMenus;
    }

    /**
     * 根据 ID 获取菜单详情。
     */
    public AdminMenu findById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new DomainException(AdminError.MENU_NOT_FOUND));
    }

    /**
     * 创建菜单。
     */
    @Transactional
    public AdminMenu create(String name, String path, String icon, Long parentId, Integer sortOrder) {
        // 验证父菜单
        if (parentId != null) {
            findById(parentId);

            // 检查层级（最多3级）- 通过计算父菜单的层级
            int parentDepth = calculateDepth(parentId);
            if (parentDepth >= AdminMenu.MAX_DEPTH - 1) {
                throw new DomainException(AdminError.MENU_DEPTH_EXCEEDED);
            }
        }

        AdminMenu menu = new AdminMenu(name, path, icon, parentId, sortOrder);
        return menuRepository.save(menu);
    }

    /**
     * 创建菜单并返回 ID。
     */
    @Transactional
    public Long createAndReturnId(String name, String path, String icon, Long parentId, Integer sortOrder) {
        return create(name, path, icon, parentId, sortOrder).getId();
    }

    /**
     * 修改菜单。
     */
    @Transactional
    public AdminMenu update(Long id, String name, String path, String icon, Long parentId, Integer sortOrder) {
        AdminMenu menu = findById(id);

        // 如果修改父菜单，验证新父菜单
        if (parentId != null && !parentId.equals(menu.getParentId())) {
            // 不允许将菜单设置为自己的父级
            if (parentId.equals(id)) {
                throw new DomainException(AdminError.MENU_INVALID_PARENT);
            }

            AdminMenu parent = findById(parentId);

            // 检查层级
            int parentDepth = calculateDepth(parent.getId());
            if (parentDepth >= AdminMenu.MAX_DEPTH - 1) {
                throw new DomainException(AdminError.MENU_DEPTH_EXCEEDED);
            }

            // 不允许将菜单设置为自己的后代
            if (isDescendant(menu.getId(), parentId)) {
                throw new DomainException(AdminError.MENU_INVALID_PARENT);
            }
        }

        menu.setName(name);
        menu.setPath(path);
        menu.setIcon(icon);
        menu.setParentId(parentId);
        menu.setSortOrder(sortOrder);
        return menuRepository.save(menu);
    }

    /**
     * 删除菜单。
     */
    @Transactional
    public void delete(Long id) {
        AdminMenu menu = findById(id);

        // 有子菜单的不能删除
        if (hasChildren(id)) {
            throw new DomainException(AdminError.MENU_HAS_CHILDREN);
        }

        menuRepository.delete(menu);
    }

    /**
     * 检查菜单是否有子菜单。
     */
    private boolean hasChildren(Long menuId) {
        return menuRepository.existsByParentId(menuId);
    }

    /**
     * 计算菜单的深度（层级）。
     */
    private int calculateDepth(Long menuId) {
        int depth = 0;
        Long currentId = menuId;
        while (currentId != null && depth < AdminMenu.MAX_DEPTH) {
            var menu = menuRepository.findById(currentId);
            if (menu.isEmpty()) {
                break;
            }
            currentId = menu.get().getParentId();
            depth++;
        }
        return depth;
    }

    /**
     * 检查 targetId 是否是 sourceId 的后代节点。
     */
    private boolean isDescendant(Long sourceId, Long targetId) {
        List<AdminMenu> allMenus = menuRepository.findAll();
        return isDescendantRecursive(allMenus, sourceId, targetId);
    }

    private boolean isDescendantRecursive(List<AdminMenu> menus, Long currentId, Long targetId) {
        for (AdminMenu menu : menus) {
            if (targetId.equals(menu.getParentId())) {
                if (menu.getId().equals(currentId)) {
                    return true;
                }
                if (isDescendantRecursive(menus, currentId, menu.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    // ========== DTO 返回方法 ==========

    /**
     * 获取菜单树（DTO）。
     */
    public List<MenuDto> findTreeAsDto() {
        List<AdminMenu> menus = findTree();
        return menus.stream()
                .map(menu -> MenuDto.fromTree(menu, menus))
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取菜单详情（DTO）。
     */
    public MenuDto findByIdAsDto(Long id) {
        AdminMenu menu = findById(id);
        return MenuDto.from(menu);
    }
}
