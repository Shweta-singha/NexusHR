package org.Employee.entity;

import jakarta.persistence.*;
import lombok.*;
import org.Employee.enums.PayrollStatus;

@Entity
@Table(
        name = "payroll_records",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"employee_id", "payroll_month"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private String payrollMonth;

    private Double grossSalary;

    private Double pfDeduction;

    private Double esiDeduction;

    private Double taxDeduction;

    private Double netSalary;

    @Enumerated(EnumType.STRING)
    private PayrollStatus status;
}
