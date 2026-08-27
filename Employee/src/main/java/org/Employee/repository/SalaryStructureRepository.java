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
}
