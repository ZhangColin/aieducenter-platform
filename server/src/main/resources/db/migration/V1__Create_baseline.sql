-- Flyway 初始基线脚本
-- 创建 Flyway 迁移历史表（如果尚未创建）

-- 这个脚本用于建立 Flyway 的基线版本
-- 后续的迁移脚本将基于此版本递增

-- 注意：首次运行时，Flyway 会自动创建 schema_migrations 表
-- 此文件保留用于未来可能需要的手动 schema 初始化
