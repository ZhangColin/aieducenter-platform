-- ========================================================================
-- Add audit columns (created_by, updated_by) to tenants table
-- Required by Auditable base class
-- ========================================================================

ALTER TABLE tenants
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);
