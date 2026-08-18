CREATE TABLE payroll_records
(
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT           NOT NULL,
    payroll_month VARCHAR(20)      NOT NULL,
    gross_salary  DOUBLE PRECISION NOT NULL,
    pf_deduction  DOUBLE PRECISION NOT NULL,
    esi_deduction DOUBLE PRECISION NOT NULL,
    tax_deduction DOUBLE PRECISION NOT NULL,
    net_salary    DOUBLE PRECISION NOT NULL,
    status        VARCHAR(30)      NOT NULL,

    CONSTRAINT fk_payroll_employee
        FOREIGN KEY (employee_id)
            REFERENCES employees (employee_id),

    CONSTRAINT uk_payroll_month
        UNIQUE (employee_id, payroll_month)
);
