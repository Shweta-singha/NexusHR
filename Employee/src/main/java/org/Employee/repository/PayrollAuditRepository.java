package org.Employee.repository;

import org.Employee.entity.PayrollAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollAuditRepository extends JpaRepository<PayrollAudit, Long> {
    List<PayrollAudit> findByPayrollIdOrderByPerformedAtDesc(Long payrollId);
}
