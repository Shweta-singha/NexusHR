package org.Employee.batch.processor;

import lombok.RequiredArgsConstructor;
import org.Employee.entity.Employee;
import org.Employee.entity.PayrollRecord;
import org.Employee.entity.SalaryStructure;
import org.Employee.enums.PayrollStatus;
import org.Employee.repository.PayrollRepository;
import org.Employee.repository.SalaryStructureRepository;
import org.Employee.service.PayrollCalculator;
import org.Employee.dto.PayrollCalculationResponse;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class PayrollProcessor implements ItemProcessor<Employee, PayrollRecord> {

    private final SalaryStructureRepository salaryStructureRepository;
    private final PayrollRepository payrollRepository;
    private final PayrollCalculator payrollCalculator;

    @Value("#{jobParameters['payrollMonth']}")
    private String payrollMonth;

    @Override
    public PayrollRecord process(Employee employee) {

        System.out.println(
                "Processing employee ID: "
                        + employee.getEmployeeId()
                        + ", payroll month: "
                        + payrollMonth
        );

        // ---------------------------------------------------------
        // 1. Check whether payroll already exists
        // ---------------------------------------------------------

        boolean payrollExists =
                payrollRepository
                        .findByEmployeeEmployeeIdAndPayrollMonth(
                                employee.getEmployeeId(),
                                payrollMonth
                        )
                        .isPresent();

        if (payrollExists) {

            System.out.println(
                    "Skipping employee "
                            + employee.getEmployeeId()
                            + " - payroll already exists for "
                            + payrollMonth
            );

            return null;
        }

        // ---------------------------------------------------------
        // 2. Find salary structure
        // ---------------------------------------------------------

        SalaryStructure salaryStructure =
                salaryStructureRepository
                        .findByEmployeeEmployeeId(
                                employee.getEmployeeId()
                        )
                        .orElse(null);

        if (salaryStructure == null) {

            System.out.println(
                    "Skipping employee "
                            + employee.getEmployeeId()
                            + " - salary structure not found"
            );

            return null;
        }

        // ---------------------------------------------------------
        // 3. Calculate payroll
        // ---------------------------------------------------------

        PayrollCalculationResponse calculation =
                payrollCalculator.calculate(salaryStructure);

        // ---------------------------------------------------------
        // 4. Build PayrollRecord
        // ---------------------------------------------------------

        PayrollRecord payrollRecord =
                PayrollRecord.builder()
                        .employee(employee)
                        .payrollMonth(payrollMonth)
                        .grossSalary(calculation.getGrossSalary())
                        .pfDeduction(calculation.getPfDeduction())
                        .esiDeduction(calculation.getEsiDeduction())
                        .taxDeduction(calculation.getTaxDeduction())
                        .netSalary(calculation.getNetSalary())
                        .status(PayrollStatus.DRAFT)
                        .build();

        System.out.println(
                "Payroll calculated for employee "
                        + employee.getEmployeeId()
                        + " | Gross: "
                        + calculation.getGrossSalary()
                        + " | Net: "
                        + calculation.getNetSalary()
        );

        return payrollRecord;
    }
}
