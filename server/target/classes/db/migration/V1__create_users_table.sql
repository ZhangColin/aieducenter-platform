-- ========================================================================
-- Epic 002: User 与登录
-- Feature: F02-01 User 聚合根与领域模型
-- Version: V1
-- ========================================================================

-- 创建用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(20) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(512),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 创建索引
CREATE INDEX idx_users_username ON users(username) WHERE deleted = FALSE;
CREATE INDEX idx_users_email ON users(email) WHERE deleted = FALSE AND email IS NOT NULL;
CREATE INDEX idx_users_phone ON users(phone_number) WHERE deleted = FALSE AND phone_number IS NOT NULL;

-- 注释
COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户ID（TSID）';
COMMENT ON COLUMN users.username IS '用户名（必填，登录凭证）';
COMMENT ON COLUMN users.email IS '邮箱（可选，登录凭证）';
COMMENT ON COLUMN users.phone_number IS '手机号（可选，登录凭证）';
COMMENT ON COLUMN users.password IS '密码（BCrypt hash）';
COMMENT ON COLUMN users.nickname IS '昵称（显示名称）';
COMMENT ON COLUMN users.avatar IS '头像URL';
COMMENT ON COLUMN users.deleted IS '软删除标记';
