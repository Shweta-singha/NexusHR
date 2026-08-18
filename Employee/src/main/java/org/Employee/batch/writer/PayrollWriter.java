package org.Employee.batch.writer;

import lombok.RequiredArgsConstructor;
import org.Employee.entity.PayrollRecord;
import org.Employee.repository.PayrollRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayrollWriter implements ItemWriter<PayrollRecord> {

    private final PayrollRepository payrollRepository;

    @Override
    public void write(Chunk<? extends PayrollRecord> chunk) {

        for (PayrollRecord payroll : chunk.getItems()) {

            System.out.println(
                    "Writing payroll for employee ID: "
                            + payroll.getEmployee().getEmployeeId()
                            + " | Month: "
                            + payroll.getPayrollMonth()
            );

            payrollRepository.save(payroll);
        }
    }
}
