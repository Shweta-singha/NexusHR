package org.Employee.repository;

import org.Employee.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {

    Optional<SalaryStructure> findByEmployeeEmployeeId(Long employeeId);

    // Selects department id + ctc directly rather than loading full
    // SalaryStructure rows and navigating salaryStructure.getEmployee()
    // .getDepartment() - see EmployeeRepository's department queries for
    // why that navigation is unsafe outside an open session.
    @Query("SELECT s.employee.department.id, s.ctc FROM SalaryStructure s")
    List<Object[]> findAllDepartmentIdAndCtc();

    // Native SQL, not JPQL: Employee has no mapped back-reference to
    // SalaryStructure (the @OneToOne is owned by SalaryStructure), so this
    // 3-way grouping can't be expressed by navigating entity associations.
    // Starts from employees (not salary_structures) so headcount reflects
    // every employee in a department bucket, not just those with a salary
    // structure on file - SUM(ctc) is null-safe for the rest. Department is
    // explicitly bucketed to 'Unassigned' rather than left null - most of
    // this org's employees (47/~50 as of Day 12) have no department_id, and
    // the dashboard should say so rather than silently dropping them.
    @Query(value = """
            SELECT COALESCE(d.name, 'Unassigned') AS department_name,
                   COUNT(*) AS employee_count,
                   COALESCE(SUM(s.ctc), 0) AS total_ctc
            FROM employees e
            LEFT JOIN departments d ON e.department_id = d.id
            LEFT JOIN salary_structures s ON s.employee_id = e.employee_id
            GROUP BY COALESCE(d.name, 'Unassigned')
            ORDER BY total_ctc DESC
            """, nativeQuery = true)
    List<Object[]> findCostByDepartment();
}
