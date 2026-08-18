package org.Employee.dto;

import java.time.LocalDateTime;

public class EmployeeDto {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String promotedBy;
    private LocalDateTime promotedAt;

    public EmployeeDto() {}

    public EmployeeDto(Long id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPromotedBy() { return promotedBy; }
    public void setPromotedBy(String promotedBy) { this.promotedBy = promotedBy; }
    public LocalDateTime getPromotedAt() { return promotedAt; }
    public void setPromotedAt(LocalDateTime promotedAt) { this.promotedAt = promotedAt; }
}
