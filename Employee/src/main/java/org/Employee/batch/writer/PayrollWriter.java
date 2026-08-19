package org.Employee.batch.writer;

import org.Employee.entity.PayrollRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class PayrollWriter implements ItemWriter<PayrollRecord> {

    private static final Logger log = LoggerFactory.getLogger(PayrollWriter.class);

    @Override
    public void write(Chunk<? extends PayrollRecord> chunk) {

        // Records arrive here already persisted (and GENERATED-audited) by
        // PayrollGenerationHelper.buildDraftIfEligible() in PayrollProcessor -
        // saving again here would just be a redundant no-op update.
        for (PayrollRecord payroll : chunk.getItems()) {
            log.info("Payroll written for employee ID: {} | Month: {}",
                    payroll.getEmployee().getEmployeeId(), payroll.getPayrollMonth());
        }
    }
}
