-- ========================================================================
-- Platform Admin Context: 初始化超级管理员和基础数据
-- ========================================================================

-- 使用预留 ID 段（1-10000 为系统数据，避免与 TSID 冲突）

-- 创建超级管理员角色
INSERT INTO sys_admin_roles (id, name, code, description, sort_order) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限，无需配置', 1);

-- 创建基础菜单
INSERT INTO sys_admin_menus (id, name, path, icon, parent_id, sort_order) VALUES
(10, '用户管理', '/admin/users', 'Users', NULL, 1),
(20, '角色管理', '/admin/roles', 'Shield', NULL, 2),
(30, '菜单管理', '/admin/menus', 'Menu', NULL, 3),
(40, '权限管理', '/admin/permissions', 'Key', NULL, 4);

-- 创建系统默认超管账号
-- 用户名: admin
-- 密码: C@rt1s@n
-- 标记为 system 用户，不可删除
-- BCrypt hash for "C@rt1s@n" (cost=10)
INSERT INTO sys_admin_users (id, username, password, nickname, status, system) VALUES
(1, 'admin', '$2a$10$F8tRzv6C8wY8nJ5KQxZ3qe1dZMX5JQZXZbVKxJPqZH1MFd5jLNUL6', '超级管理员', 'ACTIVE', TRUE);

-- 分配超级管理员角色
INSERT INTO sys_admin_user_roles (admin_id, role_id) VALUES
(1, 1);
