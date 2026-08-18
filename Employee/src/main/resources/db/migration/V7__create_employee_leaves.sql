CREATE TABLE employee_leaves
(
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT       NOT NULL,
    leave_type_id BIGINT       NOT NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    reason        TEXT,
    status        VARCHAR(20)  NOT NULL,
    applied_at    TIMESTAMP,
    approved_at   TIMESTAMP,
    approved_by   VARCHAR(100),

    CONSTRAINT fk_el_employee
        FOREIGN KEY (employee_id) REFERENCES employees (employee_id),

    CONSTRAINT fk_el_leave_type
        FOREIGN KEY (leave_type_id) REFERENCES leave_types (id)
);
