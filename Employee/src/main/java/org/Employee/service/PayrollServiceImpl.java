package org.Employee.service;
import lombok.RequiredArgsConstructor;
import org.Employee.dto.GenerateMonthlyPayrollRequest;
import org.Employee.dto.GenerateMonthlyPayrollResponse;
import org.Employee.dto.GeneratePayrollRequest;
import org.Employee.dto.GeneratePayrollResponse;
import org.Employee.dto.PayrollCalculationResponse;
import org.Employee.entity.Employee;
import org.Employee.entity.PayrollRecord;
import org.Employee.entity.SalaryStructure;
import org.Employee.enums.PayrollStatus;
import org.Employee.exception.DuplicatePayrollException;
import org.Employee.exception.ResourceNotFoundException;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.PayrollRepository;
import org.Employee.repository.SalaryStructureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

    @Override
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

        return map(payrollRepository.save(payroll));
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
    public GeneratePayrollResponse approvePayroll(Long payrollId) {
        PayrollRecord payroll = findById(payrollId);
        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT payroll can be approved");
        }
        payroll.setStatus(PayrollStatus.APPROVED);
        return map(payrollRepository.save(payroll));
    }

    @Override
    public GeneratePayrollResponse lockPayroll(Long payrollId) {
        PayrollRecord payroll = findById(payrollId);
        if (payroll.getStatus() != PayrollStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED payroll can be locked");
        }
        payroll.setStatus(PayrollStatus.LOCKED);
        return map(payrollRepository.save(payroll));
    }

    @Override
    public GeneratePayrollResponse markPayrollPaid(Long payrollId) {
        PayrollRecord payroll = findById(payrollId);
        if (payroll.getStatus() != PayrollStatus.LOCKED) {
            throw new IllegalStateException("Only LOCKED payroll can be marked as PAID");
        }
        payroll.setStatus(PayrollStatus.PAID);
        return map(payrollRepository.save(payroll));
    }

    @Override
    public GenerateMonthlyPayrollResponse generateMonthlyPayroll(GenerateMonthlyPayrollRequest request) {
        List<Employee> employees = employeeRepository.findAll();
        int generated = 0;
        int skipped = 0;

        for (Employee employee : employees) {
            Optional<PayrollRecord> payroll =
                    payrollGenerationHelper.buildDraftIfEligible(employee, request.getPayrollMonth());

            if (payroll.isPresent()) {
                payrollRepository.save(payroll.get());
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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
