package org.Employee.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String name;

    private Long parentDepartmentId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getParentDepartmentId() { return parentDepartmentId; }
    public void setParentDepartmentId(Long parentDepartmentId) { this.parentDepartmentId = parentDepartmentId; }
}
