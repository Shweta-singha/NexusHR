package org.Employee.dto;

public class OvertimeReportResponse {

    private String employee;
    private Double hours;

    public OvertimeReportResponse() {}

    public OvertimeReportResponse(String employee, Double hours) {
        this.employee = employee;
        this.hours = hours;
    }

    public String getEmployee() { return employee; }
    public void setEmployee(String employee) { this.employee = employee; }

    public Double getHours() { return hours; }
    public void setHours(Double hours) { this.hours = hours; }
}
