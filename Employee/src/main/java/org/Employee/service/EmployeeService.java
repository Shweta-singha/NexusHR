package org.Employee.service;

import java.util.List;
import org.Employee.dto.EmployeeDto;
import org.Employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    List<Employee> getAllEmployees();

    Page<Employee> getAllEmployees(Pageable pageable);

    Employee createEmployee(Employee employee);

    Employee getEmployeeById(Long id);

    EmployeeDto toDto(Employee employee);

    Employee updateEmployee(Long id, Employee employee);

    void deleteEmployee(Long id);
}
