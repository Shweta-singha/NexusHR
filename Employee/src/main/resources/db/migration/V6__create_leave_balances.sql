CREATE TABLE leave_balances
(
    id               BIGSERIAL PRIMARY KEY,
    employee_id      BIGINT UNIQUE NOT NULL,
    casual_balance   INT NOT NULL DEFAULT 12,
    sick_balance     INT NOT NULL DEFAULT 12,
    earned_balance   INT NOT NULL DEFAULT 24,
    comp_off_balance INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_leave_balance_employee
        FOREIGN KEY (employee_id) REFERENCES employees (employee_id)
);
