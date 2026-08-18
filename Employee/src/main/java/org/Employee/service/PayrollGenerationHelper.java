package org.Employee.service;

import lombok.RequiredArgsConstructor;
import org.Employee.dto.PayrollCalculationResponse;
import org.Employee.entity.Employee;
import org.Employee.entity.PayrollRecord;
import org.Employee.entity.SalaryStructure;
import org.Employee.enums.PayrollStatus;
import org.Employee.repository.PayrollRepository;
import org.Employee.repository.SalaryStructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PayrollGenerationHelper {

    private static final Logger log = LoggerFactory.getLogger(PayrollGenerationHelper.class);

    private final SalaryStructureRepository salaryStructureRepository;
    private final PayrollRepository payrollRepository;
    private final PayrollCalculator payrollCalculator;

    /**
     * Builds a DRAFT PayrollRecord for the employee/month if eligible (no existing
     * payroll for that month, and a salary structure is on file); empty otherwise.
     * Does not persist - callers decide how/when to save.
     */
    public Optional<PayrollRecord> buildDraftIfEligible(Employee employee, String payrollMonth) {
        boolean payrollExists = payrollRepository
                .findByEmployeeEmployeeIdAndPayrollMonth(employee.getEmployeeId(), payrollMonth)
                .isPresent();

        if (payrollExists) {
            log.info("Skipping employee {} - payroll already exists for {}", employee.getEmployeeId(), payrollMonth);
            return Optional.empty();
        }

        SalaryStructure salaryStructure = salaryStructureRepository
                .findByEmployeeEmployeeId(employee.getEmployeeId())
                .orElse(null);

        if (salaryStructure == null) {
            log.info("Skipping employee {} - salary structure not found", employee.getEmployeeId());
            return Optional.empty();
        }

        PayrollCalculationResponse calculation = payrollCalculator.calculate(salaryStructure);

        PayrollRecord payrollRecord = PayrollRecord.builder()
                .employee(employee)
                .payrollMonth(payrollMonth)
                .grossSalary(calculation.getGrossSalary())
                .pfDeduction(calculation.getPfDeduction())
                .esiDeduction(calculation.getEsiDeduction())
                .taxDeduction(calculation.getTaxDeduction())
                .netSalary(calculation.getNetSalary())
                .status(PayrollStatus.DRAFT)
                .build();

        log.info("Payroll calculated for employee {} | Gross: {} | Net: {}",
                employee.getEmployeeId(), calculation.getGrossSalary(), calculation.getNetSalary());

        return Optional.of(payrollRecord);
    }
}
