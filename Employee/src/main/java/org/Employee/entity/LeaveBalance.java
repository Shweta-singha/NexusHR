package org.Employee.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "leave_balances")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(name = "casual_balance")
    private Integer casualBalance;

    @Column(name = "sick_balance")
    private Integer sickBalance;

    @Column(name = "earned_balance")
    private BigDecimal earnedBalance;

    @Column(name = "comp_off_balance")
    private Integer compOffBalance;

    @Version
    private Long version;

    public LeaveBalance() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Integer getCasualBalance() { return casualBalance; }
    public void setCasualBalance(Integer casualBalance) { this.casualBalance = casualBalance; }

    public Integer getSickBalance() { return sickBalance; }
    public void setSickBalance(Integer sickBalance) { this.sickBalance = sickBalance; }

    public BigDecimal getEarnedBalance() { return earnedBalance; }
    public void setEarnedBalance(BigDecimal earnedBalance) { this.earnedBalance = earnedBalance; }

    public Integer getCompOffBalance() { return compOffBalance; }
    public void setCompOffBalance(Integer compOffBalance) { this.compOffBalance = compOffBalance; }

    public Long getVersion() { return version; }
}
