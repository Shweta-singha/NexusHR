package org.Employee.batch.reader;

import jakarta.persistence.EntityManagerFactory;
import org.Employee.entity.Employee;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.configuration.annotation.StepScope;

@Configuration
public class EmployeeItemReader {

    @Bean
    @StepScope
    public JpaPagingItemReader<Employee> employeeReader(
            EntityManagerFactory entityManagerFactory) {

        return new JpaPagingItemReaderBuilder<Employee>()
                .name("employeeReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT e FROM Employee e ORDER BY e.employeeId"
                )
                .pageSize(10)
                .build();
    }
}
