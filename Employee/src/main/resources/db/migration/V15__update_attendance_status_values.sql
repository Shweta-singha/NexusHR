UPDATE attendance
SET attendance_status = 'PRESENT'
WHERE attendance_status IN ('FULL_DAY', 'OVERTIME');
