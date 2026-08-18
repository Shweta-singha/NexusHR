package org.Employee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_audit")
public class PasswordAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "action", length = 50)
    private String action;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    public Long getId() { return id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }
}
