package org.Employee.batch.processor;

import org.Employee.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class EmployeeLoggingProcessor
        implements ItemProcessor<Employee, Employee> {

    private static final Logger log =
            LoggerFactory.getLogger(EmployeeLoggingProcessor.class);

    @Override
    public Employee process(Employee employee) {

        log.info(
                "BATCH READ -> employeeId={}, username={}",
                employee.getEmployeeId(),
                employee.getUsername()
        );

        return employee;
    }
}
