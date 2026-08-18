package org.Employee.service;

import org.Employee.dto.BulkReassignmentRequest;
import org.Employee.dto.BulkReassignmentResponse;
import org.Employee.dto.CreateDepartmentRequest;
import org.Employee.dto.DepartmentHierarchyResponse;
import org.Employee.dto.DepartmentResponse;
import org.Employee.dto.OrgChartResponse;

import java.util.List;

public interface DepartmentService {

    List<DepartmentHierarchyResponse> getHierarchy();

    DepartmentHierarchyResponse getSubTree(Long id);

    List<OrgChartResponse> getOrgChart();

    OrgChartResponse getDepartmentOrgChart(Long departmentId);

    DepartmentResponse create(CreateDepartmentRequest request);

    void delete(Long id);

    BulkReassignmentResponse reassignEmployees(BulkReassignmentRequest request);
}
