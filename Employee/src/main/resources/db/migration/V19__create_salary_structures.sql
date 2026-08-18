CREATE TABLE salary_structures
(
    id                   BIGSERIAL PRIMARY KEY,
    employee_id          BIGINT            NOT NULL UNIQUE,
    basic_pay            DOUBLE PRECISION  NOT NULL,
    hra                  DOUBLE PRECISION  NOT NULL,
    special_allowance    DOUBLE PRECISION  NOT NULL,
    conveyance_allowance DOUBLE PRECISION  NOT NULL,
    medical_allowance    DOUBLE PRECISION  NOT NULL,
    bonus                DOUBLE PRECISION  NOT NULL,
    ctc                  DOUBLE PRECISION  NOT NULL,

    CONSTRAINT fk_salary_employee
        FOREIGN KEY (employee_id)
            REFERENCES employees (employee_id)
);
