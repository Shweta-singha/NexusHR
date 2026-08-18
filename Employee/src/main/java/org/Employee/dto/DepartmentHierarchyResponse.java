package org.Employee.dto;

import java.util.List;

public class DepartmentHierarchyResponse {

    private Long id;
    private String name;
    private Integer employeeCount;
    private List<DepartmentHierarchyResponse> children;

    public DepartmentHierarchyResponse() {}

    public DepartmentHierarchyResponse(Long id, String name, Integer employeeCount,
                                        List<DepartmentHierarchyResponse> children) {
        this.id = id;
        this.name = name;
        this.employeeCount = employeeCount;
        this.children = children;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }

    public List<DepartmentHierarchyResponse> getChildren() { return children; }
    public void setChildren(List<DepartmentHierarchyResponse> children) { this.children = children; }
}
