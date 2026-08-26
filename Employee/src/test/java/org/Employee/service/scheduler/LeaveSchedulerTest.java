package org.Employee.service.scheduler;

import org.Employee.entity.Employee;
import org.Employee.entity.LeaveBalance;
import org.Employee.repository.LeaveBalanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveSchedulerTest {

    @Mock private LeaveBalanceRepository leaveBalanceRepository;

    @Test
    void creditMonthlyEarnedLeaves_addsOnePointFive_toExistingBalance() {
        LeaveBalance balance = balance(10.0);
        when(leaveBalanceRepository.findAll()).thenReturn(List.of(balance));

        new LeaveScheduler(leaveBalanceRepository).creditMonthlyEarnedLeaves();

        assertEquals(new BigDecimal("11.5"), balance.getEarnedBalance());
    }

    @Test
    void creditMonthlyEarnedLeaves_treatsNullBalanceAsZero() {
        LeaveBalance balance = balance(null);
        when(leaveBalanceRepository.findAll()).thenReturn(List.of(balance));

        new LeaveScheduler(leaveBalanceRepository).creditMonthlyEarnedLeaves();

        assertEquals(new BigDecimal("1.5"), balance.getEarnedBalance());
    }

    @Test
    void carryForwardEarnedLeaves_capsAboveTenDownToTen() {
        LeaveBalance balance = balance(24.0);
        when(leaveBalanceRepository.findAll()).thenReturn(List.of(balance));

        new LeaveScheduler(leaveBalanceRepository).carryForwardEarnedLeaves();

        assertEquals(BigDecimal.TEN, balance.getEarnedBalance());
    }

    @Test
    void carryForwardEarnedLeaves_leavesBalanceAtOrBelowTenUntouched() {
        LeaveBalance balance = balance(8.0);
        when(leaveBalanceRepository.findAll()).thenReturn(List.of(balance));

        new LeaveScheduler(leaveBalanceRepository).carryForwardEarnedLeaves();

        assertEquals(BigDecimal.valueOf(8.0), balance.getEarnedBalance());
    }

    private LeaveBalance balance(Double earned) {
        LeaveBalance balance = new LeaveBalance();
        Employee employee = new Employee();
        employee.setUsername("test-employee");
        balance.setEmployee(employee);
        balance.setEarnedBalance(earned != null ? BigDecimal.valueOf(earned) : null);
        return balance;
    }
}
