-- V9: Audit trail for role promotions/changes
ALTER TABLE employees
ADD COLUMN promoted_by VARCHAR(255);

ALTER TABLE employees
ADD COLUMN promoted_at TIMESTAMP;
