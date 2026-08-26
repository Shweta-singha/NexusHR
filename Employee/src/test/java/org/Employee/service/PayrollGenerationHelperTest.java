package org.Employee.service;

import org.Employee.entity.Employee;
import org.Employee.entity.PayrollAudit;
import org.Employee.entity.PayrollRecord;
import org.Employee.entity.SalaryStructure;
import org.Employee.repository.PayrollAuditRepository;
import org.Employee.repository.PayrollRepository;
import org.Employee.repository.SalaryStructureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollGenerationHelperTest {

    @Mock private SalaryStructureRepository salaryStructureRepository;
    @Mock private PayrollRepository payrollRepository;
    @Mock private PayrollAuditRepository payrollAuditRepository;

    private final PayrollCalculator payrollCalculator = new PayrollCalculator();

    private PayrollGenerationHelper helper() {
        return new PayrollGenerationHelper(
                salaryStructureRepository, payrollRepository, payrollCalculator, payrollAuditRepository);
    }

    @Test
    void skipsEmployee_whenPayrollAlreadyExistsForMonth() {
        Employee employee = employee(1L);
        when(payrollRepository.findByEmployeeEmployeeIdAndPayrollMonth(1L, "2026-08"))
                .thenReturn(Optional.of(new PayrollRecord()));

        Optional<PayrollRecord> result = helper().buildDraftIfEligible(employee, "2026-08");

        assertTrue(result.isEmpty());
        verify(payrollRepository, never()).save(any());
        verify(payrollAuditRepository, never()).save(any());
    }

    @Test
    void skipsEmployee_whenNoSalaryStructure() {
        Employee employee = employee(1L);
        when(payrollRepository.findByEmployeeEmployeeIdAndPayrollMonth(1L, "2026-08"))
                .thenReturn(Optional.empty());
        when(salaryStructureRepository.findByEmployeeEmployeeId(1L)).thenReturn(Optional.empty());

        Optional<PayrollRecord> result = helper().buildDraftIfEligible(employee, "2026-08");

        assertTrue(result.isEmpty());
        verify(payrollRepository, never()).save(any());
    }

    @Test
    void createsDraftAndExactlyOneAuditRow_whenEligible() {
        Employee employee = employee(1L);
        SalaryStructure salary = SalaryStructure.builder().basicPay(20000.0).ctc(40000.0).build();
        PayrollRecord saved = PayrollRecord.builder().id(99L).build();

        when(payrollRepository.findByEmployeeEmployeeIdAndPayrollMonth(1L, "2026-08"))
                .thenReturn(Optional.empty());
        when(salaryStructureRepository.findByEmployeeEmployeeId(1L)).thenReturn(Optional.of(salary));
        when(payrollRepository.save(any(PayrollRecord.class))).thenReturn(saved);

        Optional<PayrollRecord> result = helper().buildDraftIfEligible(employee, "2026-08");

        assertTrue(result.isPresent());
        assertEquals(99L, result.get().getId());

        verify(payrollRepository, times(1)).save(any(PayrollRecord.class));
        verify(payrollAuditRepository, times(1)).save(argThat(
                (PayrollAudit a) -> a.getPayrollId().equals(99L) && "GENERATED".equals(a.getAction())));
    }

    private Employee employee(Long id) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setUsername("test-employee-" + id);
        return employee;
    }
}
