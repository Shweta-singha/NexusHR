package org.Employee.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayrollScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("payrollJob")
    private final Job payrollJob;

    @Scheduled(cron = "0 0 2 1 * ?")
    public void runMonthlyPayroll() {
        String payrollMonth = YearMonth.now().minusMonths(1).toString();

        log.info("MONTHLY PAYROLL JOB TRIGGER STARTED. payrollMonth={}", payrollMonth);

        JobParameters parameters = new JobParametersBuilder()
                .addString("payrollMonth", payrollMonth)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(payrollJob, parameters);
            log.info("MONTHLY PAYROLL JOB TRIGGER SUBMITTED. payrollMonth={}", payrollMonth);
        } catch (Exception e) {
            log.error("MONTHLY PAYROLL JOB TRIGGER FAILED. payrollMonth={}", payrollMonth, e);
        }
    }
}
