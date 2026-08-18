-- V8: Remove the USER role — EMPLOYEE is now the standard non-management role
-- (USER was a legacy/duplicate role; the project now standardizes on EMPLOYEE)

-- 1. Reassign any employees currently on the USER role to EMPLOYEE
UPDATE employees e
SET role_id = (SELECT id FROM roles WHERE name = 'EMPLOYEE')
WHERE e.role_id = (SELECT id FROM roles WHERE name = 'USER');

-- 2. Drop the USER role row now that nothing references it
DELETE FROM roles WHERE name = 'USER';
