package org.Employee.batch.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/batch/test")
@RequiredArgsConstructor
public class BatchTestController {

    private final JobLauncher jobLauncher;

    @Qualifier("employeeReaderTestJob")
    private final Job employeeReaderTestJob;

    @Qualifier("payrollJob")
    private final Job payrollJob;

    @Qualifier("attritionScoringJob")
    private final Job attritionScoringJob;

    @PostMapping("/employee-reader")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> runEmployeeReader()
            throws Exception {

        JobParameters parameters =
                new JobParametersBuilder()
                        .addLong(
                                "timestamp",
                                System.currentTimeMillis()
                        )
                        .toJobParameters();

        JobExecution execution =
                jobLauncher.run(
                        employeeReaderTestJob,
                        parameters
                );

        Map<String, Object> response = new HashMap<>();

        response.put(
                "jobExecutionId",
                execution.getId()
        );

        response.put(
                "jobName",
                execution.getJobInstance().getJobName()
        );

        response.put(
                "status",
                execution.getStatus().toString()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/payroll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> runPayroll(
            @RequestParam String payrollMonth)
            throws Exception {

        if (payrollMonth == null || payrollMonth.isBlank()) {
            throw new IllegalArgumentException("payrollMonth is required");
        }

        JobParameters parameters =
                new JobParametersBuilder()
                        .addString(
                                "payrollMonth",
                                payrollMonth
                        )
                        .toJobParameters();

        JobExecution execution =
                jobLauncher.run(
                        payrollJob,
                        parameters
                );

        Map<String, Object> response = new HashMap<>();

        response.put(
                "jobExecutionId",
                execution.getId()
        );

        response.put(
                "jobName",
                execution.getJobInstance().getJobName()
        );

        response.put(
                "status",
                execution.getStatus().toString()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/attrition-scoring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> runAttritionScoring()
            throws Exception {

        JobParameters parameters =
                new JobParametersBuilder()
                        .addLong(
                                "timestamp",
                                System.currentTimeMillis()
                        )
                        .toJobParameters();

        JobExecution execution =
                jobLauncher.run(
                        attritionScoringJob,
                        parameters
                );

        Map<String, Object> response = new HashMap<>();

        response.put(
                "jobExecutionId",
                execution.getId()
        );

        response.put(
                "jobName",
                execution.getJobInstance().getJobName()
        );

        response.put(
                "status",
                execution.getStatus().toString()
        );

        return ResponseEntity.ok(response);
    }
}
