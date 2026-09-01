package org.Employee.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.Employee.dto.EmployeeDto;
import org.Employee.entity.Employee;
import org.Employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // Inner request DTO — keeps role as a plain String for the API surface
    public static class EmployeeRequest {
        @NotBlank public String username;
        @NotBlank @Size(min = 6) public String password;
        @NotBlank @Email public String email;
        public String role = "EMPLOYEE";
    }

    // CREATE — ADMIN only
    @PostMapping("/createEmployee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> createEmployee(
            @Valid @RequestBody EmployeeRequest req) {
        Employee created = employeeService.createEmployee(
                req.username, req.password, req.email, req.role);
        return new ResponseEntity<>(employeeService.toDto(created), HttpStatus.CREATED);
    }

    // GET ALL — paginated. MANAGER included so the Performance review UI's
    // employee picker works for that role (ReviewController already lets
    // MANAGER submit/view reviews) - see README known-limitations for the
    // broader-access tradeoff this implies.
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeDto> result = employeeService.getAllEmployees(pageable)
                .map(employeeService::toDto);
        return ResponseEntity.ok(result);
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(
                employeeService.toDto(employeeService.getEmployeeById(id)));
    }

    // UPDATE
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest req) {
        return ResponseEntity.ok(employeeService.toDto(
                employeeService.updateEmployee(id, req.username, req.password, req.email, req.role)));
    }

    // DELETE — ADMIN only
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }

    // TEST
    @GetMapping("/test")
    public String testApi() {
        return "JWT Authentication Working Successfully";
    }
}
