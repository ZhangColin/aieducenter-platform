-- ========================================================================
-- Fix audit columns type for users and tenants tables
-- Change created_by/updated_by from VARCHAR to BIGINT to match AuditableSoftDeletable
-- ========================================================================

-- Fix users table (first drop columns, then re-add with correct type)
ALTER TABLE users DROP COLUMN IF EXISTS created_by;
ALTER TABLE users DROP COLUMN IF EXISTS updated_by;
ALTER TABLE users ADD COLUMN created_by BIGINT;
ALTER TABLE users ADD COLUMN updated_by BIGINT;

-- Fix tenants table (first drop columns, then re-add with correct type)
ALTER TABLE tenants DROP COLUMN IF EXISTS created_by;
ALTER TABLE tenants DROP COLUMN IF EXISTS updated_by;
ALTER TABLE tenants ADD COLUMN created_by BIGINT;
ALTER TABLE tenants ADD COLUMN updated_by BIGINT;
