package org.Employee.service;

import org.Employee.dto.PayrollCalculationResponse;
import org.Employee.entity.SalaryStructure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollCalculatorTest {

    private final PayrollCalculator calculator = new PayrollCalculator();

    @Test
    void calculatesDeductionsAndNetPay_belowTaxThreshold() {
        SalaryStructure salary = SalaryStructure.builder()
                .basicPay(20000.0)
                .ctc(40000.0)
                .build();

        PayrollCalculationResponse result = calculator.calculate(salary);

        assertEquals(40000.0, result.getGrossSalary());
        assertEquals(2400.0, result.getPfDeduction());   // 20000 * 0.12
        assertEquals(300.0, result.getEsiDeduction());   // 40000 * 0.0075
        assertEquals(0.0, result.getTaxDeduction());     // gross <= 50000
        assertEquals(37300.0, result.getNetSalary());    // 40000 - 2400 - 300 - 0
    }

    @Test
    void appliesFivePercentTax_aboveFiftyThousandGross() {
        SalaryStructure salary = SalaryStructure.builder()
                .basicPay(50000.0)
                .ctc(100000.0)
                .build();

        PayrollCalculationResponse result = calculator.calculate(salary);

        assertEquals(100000.0, result.getGrossSalary());
        assertEquals(6000.0, result.getPfDeduction());   // 50000 * 0.12
        assertEquals(750.0, result.getEsiDeduction());   // 100000 * 0.0075
        assertEquals(5000.0, result.getTaxDeduction());  // gross > 50000 -> 5%
        assertEquals(88250.0, result.getNetSalary());    // 100000 - 6000 - 750 - 5000
    }

    @Test
    void grossExactlyAtFiftyThousand_noTax() {
        SalaryStructure salary = SalaryStructure.builder()
                .basicPay(25000.0)
                .ctc(50000.0)
                .build();

        PayrollCalculationResponse result = calculator.calculate(salary);

        // The check is "gross > 50000", so exactly 50000 must not trigger tax.
        assertEquals(0.0, result.getTaxDeduction());
    }
}
