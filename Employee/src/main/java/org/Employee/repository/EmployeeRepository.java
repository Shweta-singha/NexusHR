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

    // Selects the id/name directly rather than navigating
    // employee.getDepartment() on an entity returned outside an open
    // session (e.g. from a batch step's JpaPagingItemReader, with
    // open-in-view=false) - Department is LAZY, so that navigation throws
    // LazyInitializationException once the loading session is gone.
    @Query("SELECT e.department.id FROM Employee e WHERE e.employeeId = :employeeId")
    Optional<Long> findDepartmentIdByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT e.department.name FROM Employee e WHERE e.employeeId = :employeeId")
    Optional<String> findDepartmentNameByEmployeeId(@Param("employeeId") Long employeeId);

}