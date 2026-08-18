-- V2: Roles reference table — defines valid roles in the system
CREATE TABLE IF NOT EXISTS roles (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO roles (name) VALUES
    ('ADMIN'),
    ('HR_MANAGER'),
    ('MANAGER'),
    ('USER'),
    ('EMPLOYEE')
ON CONFLICT (name) DO NOTHING;
