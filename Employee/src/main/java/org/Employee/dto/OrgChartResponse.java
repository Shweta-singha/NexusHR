package org.Employee.dto;

import java.util.ArrayList;
import java.util.List;

public class OrgChartResponse {

    private Long id;
    private String name;
    private Integer employeeCount;
    private List<EmployeeOrgChartResponse> employees = new ArrayList<>();
    private List<OrgChartResponse> children = new ArrayList<>();

    public OrgChartResponse() {}

    public OrgChartResponse(Long id, String name, Integer employeeCount,
                             List<EmployeeOrgChartResponse> employees,
                             List<OrgChartResponse> children) {
        this.id = id;
        this.name = name;
        this.employeeCount = employeeCount;
        this.employees = employees;
        this.children = children;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }

    public List<EmployeeOrgChartResponse> getEmployees() { return employees; }
    public void setEmployees(List<EmployeeOrgChartResponse> employees) { this.employees = employees; }

    public List<OrgChartResponse> getChildren() { return children; }
    public void setChildren(List<OrgChartResponse> children) { this.children = children; }
}
