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

@Component
@RequiredArgsConstructor
@Slf4j
public class AttritionScoringScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("attritionScoringJob")
    private final Job attritionScoringJob;

    // 1 AM nightly - off-peak, and a full hour clear of payrollJob's
    // "0 0 2 1 * ?" (2 AM, 1st of the month) so the two never overlap even
    // on the one night a month both are due to run.
    @Scheduled(cron = "0 0 1 * * ?")
    public void runNightlyAttritionScoring() {
        log.info("NIGHTLY ATTRITION SCORING JOB TRIGGER STARTED");

        JobParameters parameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(attritionScoringJob, parameters);
            log.info("NIGHTLY ATTRITION SCORING JOB TRIGGER SUBMITTED");
        } catch (Exception e) {
            log.error("NIGHTLY ATTRITION SCORING JOB TRIGGER FAILED", e);
        }
    }
}
