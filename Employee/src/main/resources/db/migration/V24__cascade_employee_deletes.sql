-- Employee-referencing foreign keys had no ON DELETE clause (default NO ACTION),
-- so EmployeeController.deleteEmployee() failed with a raw constraint violation
-- for any employee with attendance/leave/payroll history - i.e. effectively
-- every real employee. Make these cascade so admin employee deletion actually
-- works as intended.

ALTER TABLE attendance DROP CONSTRAINT fk_attendance_employee;
ALTER TABLE attendance ADD CONSTRAINT fk_attendance_employee
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE;

ALTER TABLE leave_balances DROP CONSTRAINT fk_leave_balance_employee;
ALTER TABLE leave_balances ADD CONSTRAINT fk_leave_balance_employee
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE;

ALTER TABLE employee_leaves DROP CONSTRAINT fk_el_employee;
ALTER TABLE employee_leaves ADD CONSTRAINT fk_el_employee
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE;

ALTER TABLE password_reset_tokens DROP CONSTRAINT fk_password_token_employee;
ALTER TABLE password_reset_tokens ADD CONSTRAINT fk_password_token_employee
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE;

ALTER TABLE salary_structures DROP CONSTRAINT fk_salary_employee;
ALTER TABLE salary_structures ADD CONSTRAINT fk_salary_employee
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE;

ALTER TABLE payroll_records DROP CONSTRAINT fk_payroll_employee;
ALTER TABLE payroll_records ADD CONSTRAINT fk_payroll_employee
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE;

-- payroll_audit references payroll_records, which will itself be cascade-deleted
-- above - without this, that cascade would fail on payroll_audit's own constraint.
ALTER TABLE payroll_audit DROP CONSTRAINT payroll_audit_payroll_id_fkey;
ALTER TABLE payroll_audit ADD CONSTRAINT payroll_audit_payroll_id_fkey
    FOREIGN KEY (payroll_id) REFERENCES payroll_records(id) ON DELETE CASCADE;
