-- ========================================================================
-- 添加菜单类型字段
-- ========================================================================

-- 添加 type 字段，默认值为 'MENU'
ALTER TABLE admin_menus ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'MENU';

-- 更新现有记录的默认值
UPDATE admin_menus SET type = 'MENU' WHERE type IS NULL;
