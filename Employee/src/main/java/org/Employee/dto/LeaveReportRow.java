package org.Employee.dto;

public class LeaveReportRow {

    private String employee;
    private String leaveType;
    private String status;

    public LeaveReportRow() {}

    public LeaveReportRow(String employee, String leaveType, String status) {
        this.employee = employee;
        this.leaveType = leaveType;
        this.status = status;
    }

    public String getEmployee() { return employee; }
    public void setEmployee(String employee) { this.employee = employee; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
