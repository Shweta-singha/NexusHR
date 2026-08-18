package org.Employee.service;

import org.Employee.dto.PayrollCalculationResponse;
import org.Employee.entity.SalaryStructure;
import org.springframework.stereotype.Component;

@Component
public class PayrollCalculator {

    public PayrollCalculationResponse calculate(SalaryStructure salary) {
        double gross = salary.getCtc();
        double pf    = salary.getBasicPay() * 0.12;
        double esi   = gross * 0.0075;
        double tax   = gross > 50000 ? gross * 0.05 : 0;
        double net   = gross - pf - esi - tax;

        return PayrollCalculationResponse.builder()
                .grossSalary(gross)
                .pfDeduction(pf)
                .esiDeduction(esi)
                .taxDeduction(tax)
                .netSalary(net)
                .build();
    }
}
