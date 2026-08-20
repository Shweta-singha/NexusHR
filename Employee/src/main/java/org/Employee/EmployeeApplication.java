package org.Employee;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableJpaRepositories(basePackages = "org.Employee.repository")

public class EmployeeApplication 
{
    public static void main(String[] args) 
    {
        SpringApplication.run(EmployeeApplication.class, args);
    }
}
