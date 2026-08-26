CREATE TABLE audit_log
(
    id           BIGSERIAL    PRIMARY KEY,
    entity_type  VARCHAR(50)  NOT NULL,
    action       VARCHAR(50)  NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    details      TEXT,
    performed_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_entity_type ON audit_log (entity_type);
CREATE INDEX idx_audit_log_performed_at ON audit_log (performed_at);
