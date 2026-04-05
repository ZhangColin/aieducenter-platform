-- ============================================================
-- 修改 tenants.type 字段：VARCHAR → INT
-- 数据迁移：PERSONAL → 1
-- ============================================================

-- 0. 迁移前数据验证
SELECT COUNT(*) as total_count,
       COUNT(CASE WHEN type = 'PERSONAL' THEN 1 END) as personal_count
FROM tenants;

-- 1. 备份现有数据
CREATE TABLE tenants_backup_20260405 AS SELECT * FROM tenants;

-- 2. 添加临时整数字段
ALTER TABLE tenants ADD COLUMN type_int INT;

-- 3. 迁移数据
UPDATE tenants
SET type_int = CASE type
    WHEN 'PERSONAL' THEN 1
    ELSE 1  -- 默认值
END;

-- 4. 设置 NOT NULL 约束
ALTER TABLE tenants ALTER COLUMN type_int SET NOT NULL;

-- 5. 验证数据迁移（确保没有NULL值）
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM tenants WHERE type_int IS NULL) THEN
        RAISE EXCEPTION 'Data migration failed: NULL values found in type_int';
    END IF;
END $$;

-- 6. 删除旧字段，重命名新字段
ALTER TABLE tenants DROP COLUMN type;
ALTER TABLE tenants RENAME COLUMN type_int TO type;

-- 7. 添加注释
COMMENT ON COLUMN tenants.type IS '租户类型：1-个人';
