CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE employees
    ADD COLUMN hire_date DATE,
    ADD COLUMN employment_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN exit_date DATE;

-- SYNTHETIC BACKFILL: no real hire dates exist for employees seeded before
-- this migration, so every pre-existing row gets a fake hire date randomly
-- spread between ~1 month and ~3 years ago. This is fabricated data for the
-- attrition-prediction feature pipeline (Days 6-8), not a real historical
-- record - never treat hire_date on rows backfilled here as authoritative.
UPDATE employees
SET hire_date = COALESCE(hire_date, now() - (floor(random() * 1000 + 30) || ' days')::interval)
WHERE hire_date IS NULL;
