package org.Employee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryStructureRequest {

    private Long employeeId;
    private Double basicPay;
    private Double hra;
    private Double specialAllowance;
    private Double conveyanceAllowance;
    private Double medicalAllowance;
    private Double bonus;
}
