package org.Employee.dto;

public class DepartmentResponse {

    private Long id;
    private String name;
    private Long parentDepartmentId;

    public DepartmentResponse() {}

    public DepartmentResponse(Long id, String name, Long parentDepartmentId) {
        this.id = id;
        this.name = name;
        this.parentDepartmentId = parentDepartmentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getParentDepartmentId() { return parentDepartmentId; }
    public void setParentDepartmentId(Long parentDepartmentId) { this.parentDepartmentId = parentDepartmentId; }
}
