package org.Employee.repository;

import org.Employee.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<PayrollRecord, Long> {

    Optional<PayrollRecord> findByEmployeeEmployeeIdAndPayrollMonth(
            Long employeeId, String payrollMonth);

    List<PayrollRecord> findByEmployeeEmployeeIdOrderByPayrollMonthDesc(Long employeeId);

    List<PayrollRecord> findByPayrollMonthOrderByEmployeeEmployeeId(String payrollMonth);
}
