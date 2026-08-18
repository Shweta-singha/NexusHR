CREATE TABLE leave_types
(
    id                    BIGSERIAL PRIMARY KEY,
    name                  VARCHAR(50)  UNIQUE NOT NULL,
    max_days_per_year     INT          NOT NULL,
    carry_forward_allowed BOOLEAN      NOT NULL
);

INSERT INTO leave_types (name, max_days_per_year, carry_forward_allowed)
VALUES
    ('CASUAL',   12, false),
    ('SICK',     12, false),
    ('EARNED',   24, true),
    ('COMP_OFF', 10, false);
