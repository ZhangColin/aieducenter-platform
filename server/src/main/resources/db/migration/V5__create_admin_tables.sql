-- ========================================================================
-- Platform Admin Context: 管理员、角色、菜单表
-- ========================================================================

-- 管理员表
CREATE TABLE admin_users (
    id              BIGINT PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    nickname        VARCHAR(50) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(20),
    avatar          VARCHAR(512),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    system          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- 角色表
CREATE TABLE admin_roles (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(50) NOT NULL UNIQUE,
    code            VARCHAR(50) NOT NULL UNIQUE,
    description     VARCHAR(255),
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- 菜单表
CREATE TABLE admin_menus (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    path            VARCHAR(255),
    icon            VARCHAR(50),
    parent_id       BIGINT,
    sort_order      INT NOT NULL DEFAULT 0,
    type            VARCHAR(20) NOT NULL DEFAULT 'MENU',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- 管理员-角色关联表
CREATE TABLE admin_user_roles (
    admin_id        BIGINT NOT NULL,
    role_id         BIGINT NOT NULL,
    PRIMARY KEY (admin_id, role_id)
);

-- 角色-菜单关联表
CREATE TABLE admin_role_menus (
    role_id         BIGINT NOT NULL,
    menu_id         BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 角色-权限表（存储权限快照）
CREATE TABLE admin_role_permissions (
    role_id            BIGINT NOT NULL,
    permission_code    VARCHAR(100) NOT NULL,
    permission_name    VARCHAR(255),
    PRIMARY KEY (role_id, permission_code)
);

-- 索引
CREATE INDEX idx_admin_users_username ON admin_users(username) WHERE deleted = FALSE;
CREATE INDEX idx_admin_menus_parent_id ON admin_menus(parent_id) WHERE deleted = FALSE;

-- 外键
ALTER TABLE admin_menus ADD CONSTRAINT fk_admin_menus_parent
    FOREIGN KEY (parent_id) REFERENCES admin_menus(id);

ALTER TABLE admin_user_roles ADD CONSTRAINT fk_admin_user_roles_admin
    FOREIGN KEY (admin_id) REFERENCES admin_users(id);
ALTER TABLE admin_user_roles ADD CONSTRAINT fk_admin_user_roles_role
    FOREIGN KEY (role_id) REFERENCES admin_roles(id);

ALTER TABLE admin_role_menus ADD CONSTRAINT fk_admin_role_menus_role
    FOREIGN KEY (role_id) REFERENCES admin_roles(id);
ALTER TABLE admin_role_menus ADD CONSTRAINT fk_admin_role_menus_menu
    FOREIGN KEY (menu_id) REFERENCES admin_menus(id);

ALTER TABLE admin_role_permissions ADD CONSTRAINT fk_admin_role_permissions_role
    FOREIGN KEY (role_id) REFERENCES admin_roles(id);
