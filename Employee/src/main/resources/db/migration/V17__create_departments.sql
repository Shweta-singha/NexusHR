CREATE TABLE departments (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    parent_department_id BIGINT REFERENCES departments(id)
);

INSERT INTO departments (id, name, parent_department_id) VALUES
(1,  'Engineering',  NULL),
(2,  'Backend',      1),
(3,  'Frontend',     1),
(4,  'QA',           1),
(5,  'HR',           NULL),
(6,  'Recruitment',  5),
(7,  'Payroll',      5),
(8,  'Finance',      NULL),
(9,  'Accounts',     8),
(10, 'Audit',        8);
