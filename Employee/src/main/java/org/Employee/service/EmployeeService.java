package org.Employee.service;

import org.Employee.dto.AdminResetPasswordRequest;
import org.Employee.dto.EmployeeDto;
import org.Employee.dto.UpdateRoleRequest;
import org.Employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    List<Employee> getAllEmployees();

    Page<Employee> getAllEmployees(Pageable pageable);

    Employee createEmployee(String username, String password, String email, String roleName);

    Employee getEmployeeById(Long id);

    EmployeeDto toDto(Employee employee);

    Employee updateEmployee(Long id, String username, String password, String email, String roleName);

    void deleteEmployee(Long id);

    EmployeeDto updateEmployeeRole(Long employeeId, UpdateRoleRequest request);

    void resetEmployeePassword(Long employeeId, AdminResetPasswordRequest request);
}
