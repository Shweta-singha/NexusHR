package org.Employee.repository;

import org.Employee.entity.PasswordAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordAuditRepository extends JpaRepository<PasswordAudit, Long> {
}
