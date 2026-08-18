package org.Employee.batch.processor;

import lombok.RequiredArgsConstructor;
import org.Employee.entity.Employee;
import org.Employee.entity.PayrollRecord;
import org.Employee.service.PayrollGenerationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class PayrollProcessor implements ItemProcessor<Employee, PayrollRecord> {

    private static final Logger log = LoggerFactory.getLogger(PayrollProcessor.class);

    private final PayrollGenerationHelper payrollGenerationHelper;

    @Value("#{jobParameters['payrollMonth']}")
    private String payrollMonth;

    @Override
    public PayrollRecord process(Employee employee) {
        log.debug("Processing employee ID: {}, payroll month: {}", employee.getEmployeeId(), payrollMonth);
        return payrollGenerationHelper.buildDraftIfEligible(employee, payrollMonth).orElse(null);
    }
}
