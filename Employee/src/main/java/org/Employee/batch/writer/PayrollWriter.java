package org.Employee.batch.writer;

import lombok.RequiredArgsConstructor;
import org.Employee.entity.PayrollRecord;
import org.Employee.repository.PayrollRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayrollWriter implements ItemWriter<PayrollRecord> {

    private static final Logger log = LoggerFactory.getLogger(PayrollWriter.class);

    private final PayrollRepository payrollRepository;

    @Override
    public void write(Chunk<? extends PayrollRecord> chunk) {

        for (PayrollRecord payroll : chunk.getItems()) {

            log.info("Writing payroll for employee ID: {} | Month: {}",
                    payroll.getEmployee().getEmployeeId(), payroll.getPayrollMonth());

            payrollRepository.save(payroll);
        }
    }
}
