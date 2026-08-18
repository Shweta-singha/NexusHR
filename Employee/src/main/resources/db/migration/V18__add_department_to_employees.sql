ALTER TABLE employees
    ADD COLUMN department_id BIGINT REFERENCES departments(id);
