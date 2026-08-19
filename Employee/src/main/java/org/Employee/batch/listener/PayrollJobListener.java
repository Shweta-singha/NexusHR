package org.Employee.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class PayrollJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(PayrollJobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(
                "Starting job={} parameters={} payrollMonth={} startTime={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters(),
                jobExecution.getJobParameters().getString("payrollMonth"),
                jobExecution.getStartTime()
        );
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info(
                "Job={} finished with exitStatus={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getExitStatus()
        );

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {

            long employeesProcessed = jobExecution.getStepExecutions().stream()
                    .mapToLong(StepExecution::getReadCount)
                    .sum();

            log.info(
                    "Job={} COMPLETED - employees processed={}",
                    jobExecution.getJobInstance().getJobName(),
                    employeesProcessed
            );

        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {

            log.error(
                    "Job={} FAILED - exceptions={}",
                    jobExecution.getJobInstance().getJobName(),
                    jobExecution.getAllFailureExceptions()
            );
        }
    }
}
