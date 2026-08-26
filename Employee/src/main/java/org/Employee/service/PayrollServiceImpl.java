package org.Employee.service;
import lombok.RequiredArgsConstructor;
import org.Employee.audit.Auditable;
import org.Employee.dto.GenerateMonthlyPayrollRequest;
import org.Employee.dto.GenerateMonthlyPayrollResponse;
import org.Employee.dto.GeneratePayrollRequest;
import org.Employee.dto.GeneratePayrollResponse;
import org.Employee.dto.PayrollCalculationResponse;
import org.Employee.entity.Employee;
import org.Employee.entity.PayrollAudit;
import org.Employee.entity.PayrollRecord;
import org.Employee.entity.SalaryStructure;
import org.Employee.enums.PayrollStatus;
import org.Employee.exception.DuplicatePayrollException;
import org.Employee.exception.ResourceNotFoundException;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.PayrollAuditRepository;
import org.Employee.repository.PayrollRepository;
import org.Employee.repository.SalaryStructureRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final EmployeeRepository employeeRepository;
    private final SalaryStructureRepository salaryRepository;
    private final PayrollRepository payrollRepository;
    private final PayrollCalculator payrollCalculator;
    private final PayrollGenerationHelper payrollGenerationHelper;
    private final PayrollAuditRepository payrollAuditRepository;

    @Override
    @Auditable(entityType = "PAYROLL", action = "GENERATE")
    public GeneratePayrollResponse generatePayroll(GeneratePayrollRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        SalaryStructure salary = salaryRepository.findByEmployeeEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found"));

        payrollRepository.findByEmployeeEmployeeIdAndPayrollMonth(
                request.getEmployeeId(), request.getPayrollMonth())
                .ifPresent(p -> { throw new DuplicatePayrollException(
                        "Payroll already generated for " + request.getPayrollMonth()); });

        PayrollCalculationResponse calculation = payrollCalculator.calculate(salary);

        PayrollRecord payroll = PayrollRecord.builder()
                .employee(employee)
                .payrollMonth(request.getPayrollMonth())
                .grossSalary(calculation.getGrossSalary())
                .pfDeduction(calculation.getPfDeduction())
                .esiDeduction(calculation.getEsiDeduction())
                .taxDeduction(calculation.getTaxDeduction())
                .netSalary(calculation.getNetSalary())
                .status(PayrollStatus.DRAFT)
                .build();

        PayrollRecord saved = payrollRepository.save(payroll);
        logPayrollAction(saved.getId(), "GENERATED");
        return map(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public GeneratePayrollResponse getPayroll(Long payrollId) {
        return map(findById(payrollId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeneratePayrollResponse> getPayrollHistory(Long employeeId) {
        return payrollRepository
                .findByEmployeeEmployeeIdOrderByPayrollMonthDesc(employeeId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeneratePayrollResponse> getPayrollByMonth(String payrollMonth) {
        return payrollRepository
                .findByPayrollMonthOrderByEmployeeEmployeeId(payrollMonth)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Auditable(entityType = "PAYROLL", action = "APPROVE")
    public GeneratePayrollResponse approvePayroll(Long payrollId) {
        PayrollRecord payroll = findById(payrollId);
        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT payroll can be approved");
        }
        payroll.setStatus(PayrollStatus.APPROVED);
        PayrollRecord saved = payrollRepository.save(payroll);
        logPayrollAction(saved.getId(), "APPROVED");
        return map(saved);
    }

    @Override
    @Auditable(entityType = "PAYROLL", action = "LOCK")
    public GeneratePayrollResponse lockPayroll(Long payrollId) {
        PayrollRecord payroll = findById(payrollId);
        if (payroll.getStatus() != PayrollStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED payroll can be locked");
        }
        payroll.setStatus(PayrollStatus.LOCKED);
        PayrollRecord saved = payrollRepository.save(payroll);
        logPayrollAction(saved.getId(), "LOCKED");
        return map(saved);
    }

    @Override
    @Auditable(entityType = "PAYROLL", action = "MARK_PAID")
    public GeneratePayrollResponse markPayrollPaid(Long payrollId) {
        PayrollRecord payroll = findById(payrollId);
        if (payroll.getStatus() != PayrollStatus.LOCKED) {
            throw new IllegalStateException("Only LOCKED payroll can be marked as PAID");
        }
        payroll.setStatus(PayrollStatus.PAID);
        PayrollRecord saved = payrollRepository.save(payroll);
        logPayrollAction(saved.getId(), "PAID");
        return map(saved);
    }

    @Override
    @Auditable(entityType = "PAYROLL", action = "GENERATE_MONTHLY")
    public GenerateMonthlyPayrollResponse generateMonthlyPayroll(GenerateMonthlyPayrollRequest request) {
        List<Employee> employees = employeeRepository.findAll();
        int generated = 0;
        int skipped = 0;

        for (Employee employee : employees) {
            Optional<PayrollRecord> payroll =
                    payrollGenerationHelper.buildDraftIfEligible(employee, request.getPayrollMonth());

            if (payroll.isPresent()) {
                generated++;
            } else {
                skipped++;
            }
        }

        return GenerateMonthlyPayrollResponse.builder()
                .payrollMonth(request.getPayrollMonth())
                .totalEmployees(employees.size())
                .generated(generated)
                .skipped(skipped)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePfEsiChallan(String payrollMonth) {
        List<PayrollRecord> records = payrollRepository.findByPayrollMonthOrderByEmployeeEmployeeId(payrollMonth);

        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("PF-ESI Challan " + payrollMonth);

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Employee Code");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Gross");
            header.createCell(3).setCellValue("PF");
            header.createCell(4).setCellValue("ESI");
            header.createCell(5).setCellValue("Employer PF");
            header.createCell(6).setCellValue("Employer ESI");

            int rowNum = 1;
            for (PayrollRecord record : records) {
                Long employeeId = record.getEmployee().getEmployeeId();
                double basicPay = salaryRepository.findByEmployeeEmployeeId(employeeId)
                        .map(SalaryStructure::getBasicPay)
                        .orElse(0.0);

                // Employer-side PF/ESI aren't persisted anywhere - PayrollRecord
                // only tracks the employee's own deductions. Computed here using
                // the same statutory rates PayrollCalculator already uses for the
                // employee side (12% of basic for PF; employer ESI is the
                // standard 3.25% of gross, vs the employee's 0.75%).
                double employerPf = basicPay * 0.12;
                double employerEsi = record.getGrossSalary() * 0.0325;

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(employeeId);
                row.createCell(1).setCellValue(record.getEmployee().getUsername());
                row.createCell(2).setCellValue(record.getGrossSalary());
                row.createCell(3).setCellValue(record.getPfDeduction());
                row.createCell(4).setCellValue(record.getEsiDeduction());
                row.createCell(5).setCellValue(employerPf);
                row.createCell(6).setCellValue(employerEsi);
            }

            for (int col = 0; col <= 6; col++) {
                sheet.autoSizeColumn(col);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            workbook.close();

            return output.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PF/ESI challan", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void logPayrollAction(Long payrollId, String action) {
        String performedBy = "SYSTEM";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            performedBy = authentication.getName();
        }

        PayrollAudit audit = new PayrollAudit();
        audit.setPayrollId(payrollId);
        audit.setAction(action);
        audit.setPerformedBy(performedBy);
        audit.setPerformedAt(LocalDateTime.now());
        payrollAuditRepository.save(audit);
    }

    private PayrollRecord findById(Long payrollId) {
        return payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found"));
    }

    private GeneratePayrollResponse map(PayrollRecord payroll) {
        return GeneratePayrollResponse.builder()
                .payrollId(payroll.getId())
                .employeeId(payroll.getEmployee().getEmployeeId())
                .employeeName(payroll.getEmployee().getUsername())
                .payrollMonth(payroll.getPayrollMonth())
                .grossSalary(payroll.getGrossSalary())
                .pfDeduction(payroll.getPfDeduction())
                .esiDeduction(payroll.getEsiDeduction())
                .taxDeduction(payroll.getTaxDeduction())
                .netSalary(payroll.getNetSalary())
                .status(payroll.getStatus().name())
                .build();
    }
}
