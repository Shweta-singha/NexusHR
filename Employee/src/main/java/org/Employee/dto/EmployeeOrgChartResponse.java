package org.Employee.dto;

public class EmployeeOrgChartResponse {

    private Long employeeId;
    private String username;

    public EmployeeOrgChartResponse() {}

    public EmployeeOrgChartResponse(Long employeeId, String username) {
        this.employeeId = employeeId;
        this.username = username;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
