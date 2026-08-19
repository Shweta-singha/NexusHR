-- V21 is reserved for payroll_audit per the implementation plan; this is V22.
ALTER TABLE leave_balances ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
