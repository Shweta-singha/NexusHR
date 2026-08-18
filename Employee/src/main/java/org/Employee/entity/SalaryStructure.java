package org.Employee.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "salary_structures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(nullable = false)
    private Double basicPay;

    @Column(nullable = false)
    private Double hra;

    @Column(nullable = false)
    private Double specialAllowance;

    @Column(nullable = false)
    private Double conveyanceAllowance;

    @Column(nullable = false)
    private Double medicalAllowance;

    @Column(nullable = false)
    private Double bonus;

    @Column(nullable = false)
    private Double ctc;
}
