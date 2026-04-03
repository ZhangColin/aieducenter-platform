-- 修改 sys_admin_users.status 列：VARCHAR(20) → INTEGER
ALTER TABLE sys_admin_users
  ALTER COLUMN status TYPE INTEGER USING (
    CASE status
      WHEN 'ACTIVE' THEN 1
      WHEN 'DISABLED' THEN 0
      ELSE 1
    END
  );

-- 修改 sys_admin_users.status 默认值
ALTER TABLE sys_admin_users
  ALTER COLUMN status SET DEFAULT 1;

-- 修改 sys_admin_menus.type 列：VARCHAR(20) → INTEGER
ALTER TABLE sys_admin_menus
  ALTER COLUMN type TYPE INTEGER USING (
    CASE type
      WHEN 'MENU' THEN 1
      WHEN 'GROUP' THEN 2
      WHEN 'DIVIDER' THEN 3
      ELSE 1
    END
  );

-- 修改 sys_admin_menus.type 默认值
ALTER TABLE sys_admin_menus
  ALTER COLUMN type SET DEFAULT 1;
