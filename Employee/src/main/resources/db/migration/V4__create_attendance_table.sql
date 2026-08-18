-- V4: Attendance tracking table
CREATE TABLE attendance (
    id             BIGSERIAL PRIMARY KEY,
    employee_id    BIGINT    NOT NULL,
    date           DATE      NOT NULL,
    check_in       TIMESTAMP,
    check_out      TIMESTAMP,
    working_hours  DOUBLE PRECISION,
    overtime_hours DOUBLE PRECISION,

    CONSTRAINT fk_attendance_employee
        FOREIGN KEY (employee_id) REFERENCES employees(employee_id),

    -- One record per employee per day
    CONSTRAINT uq_attendance_employee_date
        UNIQUE (employee_id, date)
);

CREATE INDEX idx_attendance_employee ON attendance(employee_id);
CREATE INDEX idx_attendance_date     ON attendance(date);