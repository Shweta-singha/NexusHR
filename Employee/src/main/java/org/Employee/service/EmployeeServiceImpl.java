package org.Employee.service;

import java.io.Serializable;
import java.util.List;
import org.Employee.dto.EmployeeDto;
import org.Employee.entity.Employee;
import org.Employee.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService, Serializable {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Override
    @Cacheable(cacheNames = "employees")
    public List<Employee> getAllEmployees() {
        logger.info("Fetching all employees from PostgreSQL");
        return employeeRepository.findAll();
    }

    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        logger.info("Fetching employees page={} size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return employeeRepository.findAll(pageable);
    }

    @Override
    @CacheEvict(cacheNames = "employees", allEntries = true)
    public Employee createEmployee(Employee employee) {
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return employeeRepository.save(employee);
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Override
    public EmployeeDto toDto(Employee employee) {
        return new EmployeeDto(
                employee.getEmployeeId(),
                employee.getUsername(),
                employee.getEmail(),
                employee.getRole()
        );
    }

    @Override
    @CacheEvict(cacheNames = "employees", allEntries = true)
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employee.setUsername(updatedEmployee.getUsername());
        employee.setEmail(updatedEmployee.getEmail());
        employee.setPassword(passwordEncoder.encode(updatedEmployee.getPassword()));
        employee.setRole(updatedEmployee.getRole());
        return employeeRepository.save(employee);
    }

    @Override
    @CacheEvict(cacheNames = "employees", allEntries = true)
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employeeRepository.delete(employee);
    }
}
