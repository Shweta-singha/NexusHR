package org.Employee.controller;

import org.Employee.dto.AdminResetPasswordRequest;
import org.Employee.dto.EmployeeDto;
import org.Employee.dto.UpdateRoleRequest;
import org.Employee.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EmployeeService employeeService;

    public AdminController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminDashboard() {
        return ResponseEntity.ok("Welcome Admin!");
    }

    @PutMapping("/employees/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeDto updateRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {
        return employeeService.updateEmployeeRole(id, request);
    }

    @PutMapping("/employees/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> resetEmployeePassword(
            @PathVariable Long id,
            @RequestBody AdminResetPasswordRequest request) {
        employeeService.resetEmployeePassword(id, request);
        return ResponseEntity.ok("Password reset successfully");
    }
}
