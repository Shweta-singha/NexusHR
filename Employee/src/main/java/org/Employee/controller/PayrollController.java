package org.Employee.controller;

import org.Employee.dto.EmailPayslipResponse;
import org.Employee.dto.GenerateMonthlyPayrollRequest;
import org.Employee.dto.GenerateMonthlyPayrollResponse;
import org.Employee.dto.GeneratePayrollRequest;
import org.Employee.dto.GeneratePayrollResponse;
import org.Employee.dto.SalaryStructureRequest;
import org.Employee.dto.SalaryStructureResponse;
import org.Employee.service.PayrollService;
import org.Employee.service.PayslipEmailService;
import org.Employee.service.PayslipService;
import org.Employee.service.SalaryStructureService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final SalaryStructureService salaryService;
    private final PayrollService payrollService;
    private final PayslipService payslipService;
    private final PayslipEmailService payslipEmailService;
    private final JobLauncher jobLauncher;
    private final Job payrollJob;

    public PayrollController(SalaryStructureService salaryService,
                             PayrollService payrollService,
                             PayslipService payslipService,
                             PayslipEmailService payslipEmailService,
                             JobLauncher jobLauncher,
                             @Qualifier("payrollJob") Job payrollJob) {
        this.salaryService = salaryService;
        this.payrollService = payrollService;
        this.payslipService = payslipService;
        this.payslipEmailService = payslipEmailService;
        this.jobLauncher = jobLauncher;
        this.payrollJob = payrollJob;
    }

    @PostMapping("/salary-structure")
    @PreAuthorize("hasRole('ADMIN')")
    public SalaryStructureResponse createSalaryStructure(
            @RequestBody SalaryStructureRequest request) {
        return salaryService.createSalaryStructure(request);
    }

    @GetMapping("/salary-structure/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public SalaryStructureResponse getSalaryStructure(
            @PathVariable Long employeeId) {
        return salaryService.getSalaryStructure(employeeId);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public GeneratePayrollResponse generatePayroll(
            @RequestBody GeneratePayrollRequest request) {
        return payrollService.generatePayroll(request);
    }

    @PostMapping("/generate/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public GenerateMonthlyPayrollResponse generateMonthlyPayroll(
            @RequestBody GenerateMonthlyPayrollRequest request) {
        return payrollService.generateMonthlyPayroll(request);
    }

    @PostMapping("/batch/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> runPayrollBatch(
            @RequestParam String payrollMonth) throws Exception {

        if (payrollMonth == null || payrollMonth.isBlank()) {
            throw new IllegalArgumentException("payrollMonth is required");
        }

        JobParameters parameters = new JobParametersBuilder()
                .addString("payrollMonth", payrollMonth)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(payrollJob, parameters);

        Map<String, Object> response = new HashMap<>();
        response.put("jobExecutionId", execution.getId());
        response.put("jobName", execution.getJobInstance().getJobName());
        response.put("status", execution.getStatus().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public GeneratePayrollResponse getPayroll(@PathVariable Long id) {
        return payrollService.getPayroll(id);
    }

    @GetMapping("/history/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public List<GeneratePayrollResponse> getPayrollHistory(@PathVariable Long employeeId) {
        return payrollService.getPayrollHistory(employeeId);
    }

    @PutMapping("/{payrollId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public GeneratePayrollResponse approve(@PathVariable Long payrollId) {
        return payrollService.approvePayroll(payrollId);
    }

    @PutMapping("/{payrollId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public GeneratePayrollResponse lock(@PathVariable Long payrollId) {
        return payrollService.lockPayroll(payrollId);
    }

    @PutMapping("/{payrollId}/paid")
    @PreAuthorize("hasRole('ADMIN')")
    public GeneratePayrollResponse markPaid(@PathVariable Long payrollId) {
        return payrollService.markPayrollPaid(payrollId);
    }

    @GetMapping("/{payrollId}/payslip")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable Long payrollId) {

        byte[] pdf = payslipService.generatePayslip(payrollId);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=payslip-" + payrollId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/{payrollId}/email")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public EmailPayslipResponse emailPayslip(@PathVariable Long payrollId) {
        return payslipEmailService.emailPayslip(payrollId);
    }
}
