-- V3: Normalize role — replace VARCHAR role column with FK to roles table

-- 1. Add role_id column (nullable first so existing rows don't break)
ALTER TABLE employees ADD COLUMN IF NOT EXISTS role_id INTEGER;

-- 2. Populate role_id from existing role string (handles any leftover rows)
UPDATE employees e
SET role_id = r.id
FROM roles r
WHERE r.name = e.role;

-- 3. Default any unmatched rows to USER
UPDATE employees e
SET role_id = (SELECT id FROM roles WHERE name = 'USER')
WHERE e.role_id IS NULL;

-- 4. Add NOT NULL + FK constraint
ALTER TABLE employees ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE employees
    ADD CONSTRAINT fk_employees_role
    FOREIGN KEY (role_id) REFERENCES roles(id);

-- 5. Drop the old role string column
ALTER TABLE employees DROP COLUMN IF EXISTS role;
