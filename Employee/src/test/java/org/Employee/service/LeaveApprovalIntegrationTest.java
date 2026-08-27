package org.Employee.service;

import org.Employee.AbstractIntegrationTest;
import org.Employee.dto.LeaveApplyRequest;
import org.Employee.entity.Employee;
import org.Employee.entity.EmployeeLeave;
import org.Employee.entity.LeaveBalance;
import org.Employee.entity.Role;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.LeaveBalanceRepository;
import org.Employee.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LeaveApprovalIntegrationTest extends AbstractIntegrationTest {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private LeaveBalanceRepository leaveBalanceRepository;
    @Autowired private LeaveService leaveService;
    @Autowired private PlatformTransactionManager transactionManager;

    private Employee employee;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findByName("EMPLOYEE").orElseThrow();

        employee = new Employee();
        employee.setUsername("leave-approval-test-" + System.nanoTime());
        employee.setEmail("leave-approval-test-" + System.nanoTime() + "@example.com");
        employee.setPassword("xxxxxx");
        employee.setRole(role);
        employee = employeeRepository.save(employee);

        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(employee);
        balance.setCasualBalance(12);
        balance.setSickBalance(12);
        balance.setEarnedBalance(java.math.BigDecimal.valueOf(24));
        balance.setCompOffBalance(0);
        leaveBalanceRepository.saveAndFlush(balance);
    }

    @Test
    void submitAndApprove_decrementsCasualBalanceByLeaveDuration() {
        LeaveApplyRequest request = new LeaveApplyRequest();
        request.setLeaveTypeId(1L); // CASUAL, seeded by V5
        request.setStartDate(LocalDate.of(2027, 6, 1));
        request.setEndDate(LocalDate.of(2027, 6, 2)); // 2 days
        request.setReason("integration test");

        EmployeeLeave applied = leaveService.applyLeave(employee.getUsername(), request);
        leaveService.submitLeave(applied.getId(), employee.getUsername());
        leaveService.approveLeave(applied.getId(), "test-manager");

        LeaveBalance balance = leaveBalanceRepository.findByEmployeeEmployeeId(employee.getEmployeeId()).orElseThrow();
        assertEquals(10, balance.getCasualBalance()); // 12 - 2
    }

    @Test
    void concurrentSaves_onSameLeaveBalance_produceExactlyOneOptimisticLockFailure() throws Exception {
        Long balanceId = leaveBalanceRepository.findByEmployeeEmployeeId(employee.getEmployeeId())
                .orElseThrow().getId();

        // Forces both threads to load the SAME @Version before either commits -
        // without this, two quick sequential calls could easily just serialize
        // and never actually collide.
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Callable<Void> racer = () -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            try {
                tx.execute(status -> {
                    LeaveBalance balance = leaveBalanceRepository.findById(balanceId).orElseThrow();
                    balance.setCasualBalance(balance.getCasualBalance() - 1);
                    await(barrier);
                    return leaveBalanceRepository.saveAndFlush(balance);
                });
                successCount.incrementAndGet();
            } catch (ObjectOptimisticLockingFailureException e) {
                conflictCount.incrementAndGet();
            }
            return null;
        };

        List<Future<Void>> futures = pool.invokeAll(List.of(racer, racer));
        for (Future<Void> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertEquals(1, successCount.get(), "exactly one racing transaction should have committed");
        assertEquals(1, conflictCount.get(), "exactly one racing transaction should have hit the version conflict");
    }

    private void await(CyclicBarrier barrier) {
        try {
            barrier.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
