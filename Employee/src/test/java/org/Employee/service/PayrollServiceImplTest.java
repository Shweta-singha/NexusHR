package org.Employee.service;

import org.Employee.dto.GeneratePayrollRequest;
import org.Employee.dto.GeneratePayrollResponse;
import org.Employee.entity.Employee;
import org.Employee.entity.PayrollRecord;
import org.Employee.entity.SalaryStructure;
import org.Employee.enums.PayrollStatus;
import org.Employee.exception.DuplicatePayrollException;
import org.Employee.exception.ResourceNotFoundException;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.PayrollAuditRepository;
import org.Employee.repository.PayrollRepository;
import org.Employee.repository.SalaryStructureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private SalaryStructureRepository salaryRepository;
    @Mock private PayrollRepository payrollRepository;
    @Mock private PayrollAuditRepository payrollAuditRepository;

    private PayrollServiceImpl payrollService;

    @BeforeEach
    void setUp() {
        payrollService = new PayrollServiceImpl(
                employeeRepository, salaryRepository, payrollRepository,
                new PayrollCalculator(), null, payrollAuditRepository);
    }

    @Test
    void generatePayroll_throwsDuplicatePayrollException_whenAlreadyGeneratedForMonth() {
        Employee employee = employee(1L, "alice");
        GeneratePayrollRequest request = new GeneratePayrollRequest();
        request.setEmployeeId(1L);
        request.setPayrollMonth("2026-08");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRepository.findByEmployeeEmployeeId(1L))
                .thenReturn(Optional.of(SalaryStructure.builder().basicPay(20000.0).ctc(40000.0).build()));
        when(payrollRepository.findByEmployeeEmployeeIdAndPayrollMonth(1L, "2026-08"))
                .thenReturn(Optional.of(new PayrollRecord()));

        assertThrows(DuplicatePayrollException.class, () -> payrollService.generatePayroll(request));
        verify(payrollRepository, never()).save(any());
    }

    @Test
    void generatePayroll_throwsResourceNotFound_whenNoSalaryStructure() {
        Employee employee = employee(1L, "alice");
        GeneratePayrollRequest request = new GeneratePayrollRequest();
        request.setEmployeeId(1L);
        request.setPayrollMonth("2026-08");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRepository.findByEmployeeEmployeeId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollService.generatePayroll(request));
    }

    @Test
    void approvePayroll_transitionsDraftToApproved() {
        PayrollRecord record = payrollRecord(5L, PayrollStatus.DRAFT);
        when(payrollRepository.findById(5L)).thenReturn(Optional.of(record));
        when(payrollRepository.save(any(PayrollRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        GeneratePayrollResponse response = payrollService.approvePayroll(5L);

        assertEquals("APPROVED", response.getStatus());
        verify(payrollAuditRepository).save(argThat(a -> "APPROVED".equals(a.getAction()) && a.getPayrollId().equals(5L)));
    }

    @Test
    void approvePayroll_rejectsNonDraftStatus() {
        PayrollRecord record = payrollRecord(5L, PayrollStatus.APPROVED);
        when(payrollRepository.findById(5L)).thenReturn(Optional.of(record));

        assertThrows(IllegalStateException.class, () -> payrollService.approvePayroll(5L));
        verify(payrollRepository, never()).save(any());
    }

    @Test
    void lockPayroll_rejectsNonApprovedStatus() {
        PayrollRecord record = payrollRecord(5L, PayrollStatus.DRAFT);
        when(payrollRepository.findById(5L)).thenReturn(Optional.of(record));

        assertThrows(IllegalStateException.class, () -> payrollService.lockPayroll(5L));
    }

    @Test
    void markPayrollPaid_rejectsNonLockedStatus() {
        PayrollRecord record = payrollRecord(5L, PayrollStatus.APPROVED);
        when(payrollRepository.findById(5L)).thenReturn(Optional.of(record));

        assertThrows(IllegalStateException.class, () -> payrollService.markPayrollPaid(5L));
    }

    @Test
    void markPayrollPaid_transitionsLockedToPaid() {
        PayrollRecord record = payrollRecord(5L, PayrollStatus.LOCKED);
        when(payrollRepository.findById(5L)).thenReturn(Optional.of(record));
        when(payrollRepository.save(any(PayrollRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        GeneratePayrollResponse response = payrollService.markPayrollPaid(5L);

        assertEquals("PAID", response.getStatus());
    }

    private Employee employee(Long id, String username) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setUsername(username);
        return employee;
    }

    private PayrollRecord payrollRecord(Long id, PayrollStatus status) {
        return PayrollRecord.builder()
                .id(id)
                .employee(employee(id, "test-employee"))
                .payrollMonth("2026-08")
                .grossSalary(40000.0)
                .pfDeduction(2400.0)
                .esiDeduction(300.0)
                .taxDeduction(0.0)
                .netSalary(37300.0)
                .status(status)
                .build();
    }
}
