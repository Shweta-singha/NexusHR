package org.Employee.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GenerateMonthlyPayrollResponse {

    private String payrollMonth;
    private int totalEmployees;
    private int generated;
    private int skipped;
}
