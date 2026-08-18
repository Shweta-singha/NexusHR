package org.Employee.controller;

import jakarta.validation.Valid;
import org.Employee.dto.BulkReassignmentRequest;
import org.Employee.dto.BulkReassignmentResponse;
import org.Employee.dto.CreateDepartmentRequest;
import org.Employee.dto.DepartmentHierarchyResponse;
import org.Employee.dto.DepartmentResponse;
import org.Employee.dto.OrgChartResponse;
import org.Employee.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/hierarchy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DepartmentHierarchyResponse>> getHierarchy() {
        return ResponseEntity.ok(departmentService.getHierarchy());
    }

    @GetMapping("/{id}/hierarchy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DepartmentHierarchyResponse> getSubTree(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getSubTree(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.ok(departmentService.create(request));
    }

    @GetMapping("/org-chart")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<List<OrgChartResponse>> getOrgChart() {
        return ResponseEntity.ok(departmentService.getOrgChart());
    }

    @GetMapping("/{id}/org-chart")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<OrgChartResponse> getDepartmentOrgChart(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentOrgChart(id));
    }

    @PostMapping("/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkReassignmentResponse> reassignEmployees(
            @Valid @RequestBody BulkReassignmentRequest request) {
        return ResponseEntity.ok(departmentService.reassignEmployees(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
