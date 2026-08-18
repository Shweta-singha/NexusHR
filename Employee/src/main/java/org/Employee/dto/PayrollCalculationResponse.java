package org.Employee.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PayrollCalculationResponse {

    private Double grossSalary;
    private Double pfDeduction;
    private Double esiDeduction;
    private Double taxDeduction;
    private Double netSalary;
}
