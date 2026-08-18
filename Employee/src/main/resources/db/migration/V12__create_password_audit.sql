CREATE TABLE password_audit
(
    id           BIGSERIAL    PRIMARY KEY,
    employee_id  BIGINT,
    action       VARCHAR(50),
    performed_by VARCHAR(100),
    performed_at TIMESTAMP
);

