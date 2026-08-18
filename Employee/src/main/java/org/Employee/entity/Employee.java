package org.Employee.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "employees")
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "promoted_by")
    private String promotedBy;

    @Column(name = "promoted_at")
    private LocalDateTime promotedAt;

    public Employee() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getPromotedBy() { return promotedBy; }
    public void setPromotedBy(String promotedBy) { this.promotedBy = promotedBy; }

    public LocalDateTime getPromotedAt() { return promotedAt; }
    public void setPromotedAt(LocalDateTime promotedAt) { this.promotedAt = promotedAt; }
}
