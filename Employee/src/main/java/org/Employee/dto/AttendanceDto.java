package org.Employee.dto;

import org.Employee.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceDto {

    private Long id;
    private Long employeeId;
    private String username;
    private LocalDate date;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Double workingHours;
    private Double overtimeHours;
    private AttendanceStatus attendanceStatus;

    public AttendanceDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }

    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }

    public Double getWorkingHours() { return workingHours; }
    public void setWorkingHours(Double workingHours) { this.workingHours = workingHours; }

    public Double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(Double overtimeHours) { this.overtimeHours = overtimeHours; }

    public AttendanceStatus getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(AttendanceStatus attendanceStatus) { this.attendanceStatus = attendanceStatus; }
}
