-- ========================================================================
-- Add audit columns (created_by, updated_by) to users table
-- Required by Auditable base class
-- ========================================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);
