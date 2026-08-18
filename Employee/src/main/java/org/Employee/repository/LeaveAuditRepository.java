package org.Employee.repository;

import org.Employee.entity.LeaveAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveAuditRepository extends JpaRepository<LeaveAudit, Long> {
}
