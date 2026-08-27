package org.Employee.batch;

import org.Employee.AbstractIntegrationTest;
import org.Employee.entity.Employee;
import org.Employee.entity.PayrollRecord;
import org.Employee.entity.Role;
import org.Employee.entity.SalaryStructure;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.PayrollAuditRepository;
import org.Employee.repository.PayrollRepository;
import org.Employee.repository.RoleRepository;
import org.Employee.repository.SalaryStructureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PayrollBatchIntegrationTest extends AbstractIntegrationTest {

    // Uniquely scopes this test's assertions so results are correct
    // regardless of what other employees/payroll data other integration
    // test classes leave behind in the shared Testcontainers database.
    private static final String TEST_PAYROLL_MONTH = "9999-01";

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SalaryStructureRepository salaryStructureRepository;
    @Autowired private PayrollRepository payrollRepository;
    @Autowired private PayrollAuditRepository payrollAuditRepository;

    @Autowired
    @Qualifier("payrollJob")
    private Job payrollJob;

    @Autowired
    private JobLauncher jobLauncher;

    private JobLauncherTestUtils jobLauncherTestUtils;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJob(payrollJob);
        jobLauncherTestUtils.setJobLauncher(jobLauncher);

        Role role = roleRepository.findByName("EMPLOYEE").orElseThrow();

        Employee employee = new Employee();
        employee.setUsername("payroll-batch-test-employee");
        employee.setEmail("payroll-batch-test@example.com");
        employee.setPassword("xxxxxx");
        employee.setRole(role);
        employee = employeeRepository.save(employee);

        SalaryStructure salary = SalaryStructure.builder()
                .employee(employee)
                .basicPay(30000.0)
                .hra(10000.0)
                .specialAllowance(5000.0)
                .conveyanceAllowance(2000.0)
                .medicalAllowance(1500.0)
                .bonus(1500.0)
                .ctc(50000.0)
                .build();
        salaryStructureRepository.save(salary);
    }

    @Test
    void payrollJob_completesAndGeneratesExactlyOnePayrollRecordAndAuditRow() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addString("payrollMonth", TEST_PAYROLL_MONTH)
                        .addLong("uniqueRun", System.nanoTime())
                        .toJobParameters());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        List<PayrollRecord> records = payrollRepository
                .findByPayrollMonthOrderByEmployeeEmployeeId(TEST_PAYROLL_MONTH);
        assertEquals(1, records.size());
        assertEquals("DRAFT", records.get(0).getStatus().name());
        assertEquals("payroll-batch-test-employee", records.get(0).getEmployee().getUsername());

        long auditCount = payrollAuditRepository.findAll().stream()
                .filter(a -> a.getPayrollId().equals(records.get(0).getId()) && "GENERATED".equals(a.getAction()))
                .count();
        assertEquals(1, auditCount);
    }
}
