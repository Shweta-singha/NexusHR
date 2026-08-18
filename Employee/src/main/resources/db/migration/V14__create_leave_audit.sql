CREATE TABLE leave_audit
(
    id           BIGSERIAL    PRIMARY KEY,
    leave_id     BIGINT,
    action       VARCHAR(50),
    performed_by VARCHAR(100),
    performed_at TIMESTAMP
);
