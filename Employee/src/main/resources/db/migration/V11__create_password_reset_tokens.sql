CREATE TABLE password_reset_tokens
(
    id          BIGSERIAL    PRIMARY KEY,
    employee_id BIGINT       NOT NULL,
    token       VARCHAR(255) UNIQUE NOT NULL,
    expiry_time TIMESTAMP    NOT NULL,
    used        BOOLEAN      DEFAULT FALSE,

    CONSTRAINT fk_password_token_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees (employee_id)
);
