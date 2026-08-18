package org.Employee.dto;

import java.util.List;

public class BulkReassignmentResponse {

    private int totalEmployeesMoved;
    private Long targetDepartmentId;
    private List<Long> employeeIds;

    public BulkReassignmentResponse(int totalEmployeesMoved, Long targetDepartmentId, List<Long> employeeIds) {
        this.totalEmployeesMoved = totalEmployeesMoved;
        this.targetDepartmentId = targetDepartmentId;
        this.employeeIds = employeeIds;
    }

    public int getTotalEmployeesMoved() { return totalEmployeesMoved; }
    public Long getTargetDepartmentId() { return targetDepartmentId; }
    public List<Long> getEmployeeIds() { return employeeIds; }
}
