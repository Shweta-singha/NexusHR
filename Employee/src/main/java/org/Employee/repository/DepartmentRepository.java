package org.Employee.repository;

import org.Employee.entity.Department;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Eagerly loads parentDepartment — eliminates N+1 on parent chain traversal
    @Override
    @EntityGraph(attributePaths = {"parentDepartment"})
    List<Department> findAll();

    List<Department> findByParentDepartmentIsNull();

    // Loads parentDepartment + employees in one query — used for org chart building
    @EntityGraph(attributePaths = {"parentDepartment", "employees"})
    @Query("SELECT d FROM Department d")
    List<Department> findAllWithEmployees();

    // Returns [departmentId, employeeCount] — avoids cartesian product from OneToMany in EntityGraph
    @Query("SELECT d.id, COUNT(e.employeeId) FROM Department d LEFT JOIN d.employees e GROUP BY d.id")
    List<Object[]> countEmployeesPerDepartment();
}
