package org.Employee.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GeneratePayrollResponse {

    private Long payrollId;
    private Long employeeId;
    private String employeeName;
    private String payrollMonth;
    private Double grossSalary;
    private Double pfDeduction;
    private Double esiDeduction;
    private Double taxDeduction;
    private Double netSalary;
    private String status;
}
