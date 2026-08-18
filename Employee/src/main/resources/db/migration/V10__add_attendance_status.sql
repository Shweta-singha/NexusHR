-- V10: Attendance status classification (overtime_hours already exists from V4)
ALTER TABLE attendance
ADD COLUMN attendance_status VARCHAR(20);
