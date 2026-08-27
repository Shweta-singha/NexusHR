package org.Employee.batch.config;

import org.Employee.batch.listener.PayrollJobListener;
import org.Employee.batch.processor.AttritionFeatureProcessor;
import org.Employee.batch.processor.EmployeeAttritionFeatures;
import org.Employee.batch.processor.EmployeeLoggingProcessor;
import org.Employee.batch.processor.PayrollProcessor;
import org.Employee.batch.writer.AttritionScoreWriter;
import org.Employee.batch.writer.EmployeeLoggingWriter;
import org.Employee.batch.writer.PayrollWriter;
import org.Employee.entity.Employee;
import org.Employee.entity.PayrollRecord;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    public Step employeeReaderTestStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaPagingItemReader<Employee> employeeReader,
            EmployeeLoggingProcessor processor,
            EmployeeLoggingWriter writer) {

        return new StepBuilder(
                "employeeReaderTestStep",
                jobRepository
        )
                .<Employee, Employee>chunk(
                        10,
                        transactionManager
                )
                .reader(employeeReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job employeeReaderTestJob(
            JobRepository jobRepository,
            Step employeeReaderTestStep) {

        return new JobBuilder(
                "employeeReaderTestJob",
                jobRepository
        )
                .start(employeeReaderTestStep)
                .build();
    }

    @Bean
    public Step payrollStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaPagingItemReader<Employee> employeeReader,
            PayrollProcessor payrollProcessor,
            PayrollWriter payrollWriter) {

        return new StepBuilder(
                "payrollStep",
                jobRepository
        )
                .<Employee, PayrollRecord>chunk(
                        10,
                        transactionManager
                )
                .reader(employeeReader)
                .processor(payrollProcessor)
                .writer(payrollWriter)
                .build();
    }

    @Bean
    public Job payrollJob(
            JobRepository jobRepository,
            Step payrollStep,
            PayrollJobListener payrollJobListener) {

        return new JobBuilder(
                "payrollJob",
                jobRepository
        )
                .start(payrollStep)
                .listener(payrollJobListener)
                .build();
    }

    @Bean
    public Step attritionScoringStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaPagingItemReader<Employee> employeeReader,
            AttritionFeatureProcessor attritionFeatureProcessor,
            AttritionScoreWriter attritionScoreWriter) {

        return new StepBuilder(
                "attritionScoringStep",
                jobRepository
        )
                .<Employee, EmployeeAttritionFeatures>chunk(
                        20,
                        transactionManager
                )
                .reader(employeeReader)
                .processor(attritionFeatureProcessor)
                .writer(attritionScoreWriter)
                .build();
    }

    @Bean
    public Job attritionScoringJob(
            JobRepository jobRepository,
            Step attritionScoringStep) {

        return new JobBuilder(
                "attritionScoringJob",
                jobRepository
        )
                .start(attritionScoringStep)
                .build();
    }
}
