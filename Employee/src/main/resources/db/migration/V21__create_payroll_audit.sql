CREATE TABLE payroll_audit
(
    id           BIGSERIAL     PRIMARY KEY,
    payroll_id   BIGINT        NOT NULL REFERENCES payroll_records(id),
    action       VARCHAR(50)   NOT NULL,
    performed_by VARCHAR(100),
    performed_at TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_payroll_audit_payroll_id ON payroll_audit (payroll_id);
