package org.Employee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneratePayrollRequest {

    private Long employeeId;
    private String payrollMonth;
}
