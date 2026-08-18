-- =====================================================================
-- Seed salary_structures for every employee that doesn't already have
-- one. NOT a Flyway migration - lives outside db/migration so it never
-- auto-runs on startup; run it manually when you want demo data:
--
--   psql -h localhost -U postgres -d nexushr -f Employee/src/main/resources/db/seed/seed_salary_structures.sql
--
-- Safe to re-run: only inserts for employees without a salary_structures
-- row already (NOT EXISTS guard), so it's idempotent.
--
-- Base CTC is randomized within a per-role band, with a small
-- per-department multiplier applied where a department is actually set,
-- so the payroll batch demo shows realistic salary variation across
-- employees instead of one flat number repeated for all of them.
--
-- Component split mirrors the one existing seeded row's own ratios
-- (employee_id 54, role HR_MANAGER: basic 30000 / hra 15000 / special
-- 8000 / conveyance 2000 / medical 2000 / bonus 3000 on a 60000 ctc)
-- i.e. basic=50%, hra=25%, special=13.33%, conveyance=3.33%,
-- medical=3.33%, bonus=5% of ctc - so PayrollCalculator's PF
-- (basic_pay * 12%) and gross (= ctc) math stays internally consistent.
-- =====================================================================

WITH role_bands (role_name, min_ctc, max_ctc) AS (
    VALUES
        ('MANAGER',    70000, 100000),
        ('HR_MANAGER', 55000,  80000),
        ('ADMIN',      35000,  65000),
        ('EMPLOYEE',   25000,  45000)
),
target AS (
    SELECT
        e.employee_id,
        ROUND(
            (
                (
                    COALESCE(b.min_ctc, 25000)
                    + random() * (COALESCE(b.max_ctc, 120000) - COALESCE(b.min_ctc, 25000))
                )
                * CASE e.department_id
                      WHEN 11 THEN 1.10   -- DevOps
                      WHEN 12 THEN 1.10   -- Cloud
                      WHEN 13 THEN 1.10   -- Kubernetes
                      WHEN 1  THEN 1.05   -- Engineering
                      WHEN 2  THEN 1.05   -- Backend
                      WHEN 3  THEN 1.05   -- Frontend
                      WHEN 8  THEN 1.05   -- Finance
                      WHEN 9  THEN 1.05   -- Accounts
                      ELSE 1.00
                  END
            )::numeric,
            -3  -- round to the nearest 1000 for a plausible-looking salary
        ) AS ctc
    FROM employees e
    JOIN roles r ON r.id = e.role_id
    LEFT JOIN role_bands b ON b.role_name = r.name
    WHERE NOT EXISTS (
        SELECT 1 FROM salary_structures s WHERE s.employee_id = e.employee_id
    )
)
INSERT INTO salary_structures (
    employee_id, basic_pay, hra, special_allowance,
    conveyance_allowance, medical_allowance, bonus, ctc
)
SELECT
    employee_id,
    ROUND(ctc * 30 / 60, -2) AS basic_pay,
    ROUND(ctc * 15 / 60, -2) AS hra,
    ROUND(ctc *  8 / 60, -2) AS special_allowance,
    ROUND(ctc *  2 / 60, -2) AS conveyance_allowance,
    ROUND(ctc *  2 / 60, -2) AS medical_allowance,
    ROUND(ctc *  3 / 60, -2) AS bonus,
    ctc
FROM target;
