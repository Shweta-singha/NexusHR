package org.Employee.service;

import lombok.RequiredArgsConstructor;
import org.Employee.dto.PayrollCalculationResponse;
import org.Employee.entity.Employee;
import org.Employee.entity.PayrollAudit;
import org.Employee.entity.PayrollRecord;
import org.Employee.entity.SalaryStructure;
import org.Employee.enums.PayrollStatus;
import org.Employee.repository.PayrollAuditRepository;
import org.Employee.repository.PayrollRepository;
import org.Employee.repository.SalaryStructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PayrollGenerationHelper {

    private static final Logger log = LoggerFactory.getLogger(PayrollGenerationHelper.class);

    private final SalaryStructureRepository salaryStructureRepository;
    private final PayrollRepository payrollRepository;
    private final PayrollCalculator payrollCalculator;
    private final PayrollAuditRepository payrollAuditRepository;

    /**
     * Builds, persists, and audits a DRAFT PayrollRecord for the employee/month if
     * eligible (no existing payroll for that month, and a salary structure is on
     * file); empty otherwise. Writes exactly one PayrollAudit "GENERATED" row per
     * actual insert, never on a skip - PayrollAudit.payroll_id is a NOT NULL FK,
     * so the audit row can only be written once the record has a real id, which is
     * why persistence lives here now instead of with the caller. Callers must not
     * save the returned record or write their own GENERATED audit row again.
     */
    @Transactional
    public Optional<PayrollRecord> buildDraftIfEligible(Employee employee, String payrollMonth) {
        boolean payrollExists = payrollRepository
                .findByEmployeeEmployeeIdAndPayrollMonth(employee.getEmployeeId(), payrollMonth)
                .isPresent();

        if (payrollExists) {
            log.info("Skipping employee {} - payroll already exists for {}", employee.getEmployeeId(), payrollMonth);
            return Optional.empty();
        }

        SalaryStructure salaryStructure = salaryStructureRepository
                .findByEmployeeEmployeeId(employee.getEmployeeId())
                .orElse(null);

        if (salaryStructure == null) {
            log.info("Skipping employee {} - salary structure not found", employee.getEmployeeId());
            return Optional.empty();
        }

        PayrollCalculationResponse calculation = payrollCalculator.calculate(salaryStructure);

        PayrollRecord payrollRecord = PayrollRecord.builder()
                .employee(employee)
                .payrollMonth(payrollMonth)
                .grossSalary(calculation.getGrossSalary())
                .pfDeduction(calculation.getPfDeduction())
                .esiDeduction(calculation.getEsiDeduction())
                .taxDeduction(calculation.getTaxDeduction())
                .netSalary(calculation.getNetSalary())
                .status(PayrollStatus.DRAFT)
                .build();

        PayrollRecord saved = payrollRepository.save(payrollRecord);
        logGenerated(saved.getId());

        log.info("Payroll calculated for employee {} | Gross: {} | Net: {}",
                employee.getEmployeeId(), calculation.getGrossSalary(), calculation.getNetSalary());

        return Optional.of(saved);
    }

    private void logGenerated(Long payrollId) {
        String performedBy = "SYSTEM";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            performedBy = authentication.getName();
        }

        PayrollAudit audit = new PayrollAudit();
        audit.setPayrollId(payrollId);
        audit.setAction("GENERATED");
        audit.setPerformedBy(performedBy);
        audit.setPerformedAt(LocalDateTime.now());
        payrollAuditRepository.save(audit);
    }
}
