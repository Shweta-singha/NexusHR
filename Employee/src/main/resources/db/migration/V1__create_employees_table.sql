-- V1: Initial schema — employees table
CREATE TABLE IF NOT EXISTS employees (
    employee_id BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'USER'
);
