package org.Employee.service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.Employee.dto.AdminResetPasswordRequest;
import org.Employee.dto.EmployeeDto;
import org.Employee.dto.UpdateRoleRequest;
import org.Employee.entity.Employee;
import org.Employee.entity.PasswordAudit;
import org.Employee.entity.Role;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.PasswordAuditRepository;
import org.Employee.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordAuditRepository passwordAuditRepository;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            PasswordAuditRepository passwordAuditRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordAuditRepository = passwordAuditRepository;
    }

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
    public Employee createEmployee(String username, String password, String email, String roleName) {
        Role role = resolveRole(roleName);
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPassword(passwordEncoder.encode(password));
        employee.setEmail(email);
        employee.setRole(role);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Override
    public EmployeeDto toDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto(
                employee.getEmployeeId(),
                employee.getUsername(),
                employee.getEmail(),
                employee.getRole().getName()
        );
        dto.setPromotedBy(employee.getPromotedBy());
        dto.setPromotedAt(employee.getPromotedAt());
        return dto;
    }

    @Override
    @CacheEvict(cacheNames = "employees", allEntries = true)
    public Employee updateEmployee(Long id, String username, String password,
                                   String email, String roleName) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employee.setUsername(username);
        employee.setEmail(email);
        employee.setPassword(passwordEncoder.encode(password));
        employee.setRole(resolveRole(roleName));
        return employeeRepository.save(employee);
    }

    @Override
    @CacheEvict(cacheNames = "employees", allEntries = true)
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional
    public EmployeeDto updateEmployeeRole(Long employeeId, UpdateRoleRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        if (employee.getUsername().equals(currentUsername)) {
            throw new RuntimeException("Admin cannot change own role");
        }

        long adminCount = employeeRepository.countByRoleName("ADMIN");
        if (adminCount == 1 && employee.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Cannot remove last ADMIN");
        }

        Role role = roleRepository.findByName(request.getRoleName().name())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        employee.setRole(role);
        employee.setPromotedBy(currentUsername);
        employee.setPromotedAt(LocalDateTime.now());

        Employee updated = employeeRepository.save(employee);
        return toDto(updated);
    }

    @Override
    @Transactional
    public void resetEmployeePassword(Long employeeId, AdminResetPasswordRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);

        String adminUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        logPasswordAction(employeeId, "ADMIN_RESET", adminUsername);
    }

    private void logPasswordAction(Long employeeId, String action, String performedBy) {
        PasswordAudit audit = new PasswordAudit();
        audit.setEmployeeId(employeeId);
        audit.setAction(action);
        audit.setPerformedBy(performedBy);
        audit.setPerformedAt(LocalDateTime.now());
        passwordAuditRepository.save(audit);
    }

    private Role resolveRole(String roleName) {
        String normalized = (roleName != null) ? roleName.toUpperCase() : "EMPLOYEE";
        return roleRepository.findByName(normalized)
                .orElseThrow(() -> new RuntimeException("Role not found: " + normalized));
    }
}
