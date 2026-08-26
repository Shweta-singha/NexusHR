CREATE TABLE goals
(
    id          BIGSERIAL    PRIMARY KEY,
    employee_id BIGINT       NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    target_date DATE,
    status      VARCHAR(20)  NOT NULL DEFAULT 'NOT_STARTED',
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT fk_goal_employee
        FOREIGN KEY (employee_id) REFERENCES employees (employee_id) ON DELETE CASCADE
);

CREATE INDEX idx_goals_employee ON goals (employee_id);
