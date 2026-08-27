-- FK references employees(employee_id), not employees(id) - employee_id is
-- this project's actual employees PK column throughout (see V1).
CREATE TABLE ai_attrition_scores
(
    id            BIGSERIAL      PRIMARY KEY,
    employee_id   BIGINT         NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    risk_score    NUMERIC(5,4)   NOT NULL,
    risk_band     VARCHAR(10)    NOT NULL,
    scored_at     TIMESTAMP      NOT NULL DEFAULT now(),
    model_version VARCHAR(20)    NOT NULL
);

-- One current score per employee - re-scoring upserts this row rather than
-- accumulating a new one every run.
CREATE UNIQUE INDEX uq_attrition_scores_employee ON ai_attrition_scores (employee_id);

CREATE INDEX idx_attrition_scores_risk_band ON ai_attrition_scores (risk_band);
