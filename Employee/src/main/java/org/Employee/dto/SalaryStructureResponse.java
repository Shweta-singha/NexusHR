package org.Employee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryStructureResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Double basicPay;
    private Double hra;
    private Double specialAllowance;
    private Double conveyanceAllowance;
    private Double medicalAllowance;
    private Double bonus;
    private Double ctc;
}
