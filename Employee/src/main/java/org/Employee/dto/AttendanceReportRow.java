package org.Employee.dto;

import java.time.LocalDate;

public class AttendanceReportRow {

    private String employee;
    private LocalDate date;
    private Double workingHours;

    public AttendanceReportRow(String employee, LocalDate date, Double workingHours) {
        this.employee = employee;
        this.date = date;
        this.workingHours = workingHours;
    }

    public String getEmployee() { return employee; }
    public LocalDate getDate() { return date; }
    public Double getWorkingHours() { return workingHours; }
}
