package org.Employee.batch.config;

import org.Employee.batch.processor.EmployeeLoggingProcessor;
import org.Employee.batch.processor.PayrollProcessor;
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
            Step payrollStep) {

        return new JobBuilder(
                "payrollJob",
                jobRepository
        )
                .start(payrollStep)
                .build();
    }
}
