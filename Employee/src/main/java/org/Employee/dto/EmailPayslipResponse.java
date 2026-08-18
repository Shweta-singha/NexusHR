package org.Employee.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmailPayslipResponse {

    private Long payrollId;
    private String employeeEmail;
    private String payrollMonth;
    private String message;
}
