package org.Employee.controller;

import jakarta.validation.Valid;
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

    // CREATE — public (no auth needed)
    @PostMapping("/createEmployee")
    public ResponseEntity<EmployeeDto> createEmployee(
            @Valid @RequestBody Employee employee) {
        Employee created = employeeService.createEmployee(employee);
        return new ResponseEntity<>(employeeService.toDto(created), HttpStatus.CREATED);
    }

    // GET ALL — paginated
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
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
        return ResponseEntity.ok(employeeService.toDto(employeeService.getEmployeeById(id)));
    }

    // UPDATE
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.toDto(employeeService.updateEmployee(id, employee)));
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
