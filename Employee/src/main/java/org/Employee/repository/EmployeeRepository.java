package org.Employee.repository;

import java.util.List;
import java.util.Optional;

import org.Employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsername(String username);

    Optional<Employee> findByEmployeeId(Long employeeId);

    List<Employee> findAllByEmployeeIdIn(List<Long> employeeIds);

    boolean existsByDepartmentId(Long departmentId);

    @Query("""
        SELECT COUNT(e)
        FROM Employee e
        WHERE e.role.name = :roleName
        """)
    long countByRoleName(@Param("roleName") String roleName);

}