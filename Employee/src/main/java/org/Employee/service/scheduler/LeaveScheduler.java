package org.Employee.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Employee.entity.LeaveBalance;
import org.Employee.repository.LeaveBalanceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveScheduler {

    private static final double ACCRUAL_AMOUNT = 1.5;

    private final LeaveBalanceRepository leaveBalanceRepository;

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void creditMonthlyEarnedLeaves() {
        log.info("MONTHLY ACCRUAL STARTED");

        List<LeaveBalance> balances = leaveBalanceRepository.findAll();

        for (LeaveBalance balance : balances) {
            BigDecimal current = balance.getEarnedBalance();
            if (current == null) {
                current = BigDecimal.ZERO;
            }
            BigDecimal updated = current.add(BigDecimal.valueOf(ACCRUAL_AMOUNT));
            balance.setEarnedBalance(updated);
            log.info("Employee={} Earned Leave {} -> {}",
                    balance.getEmployee().getUsername(),
                    current,
                    updated);
        }

        leaveBalanceRepository.saveAll(balances);

        log.info("MONTHLY ACCRUAL COMPLETED. Employees={}", balances.size());
    }

    @Scheduled(cron = "0 0 0 1 1 ?")
    @Transactional
    public void carryForwardEarnedLeaves() {
        log.info("YEAR-END CARRY FORWARD STARTED");

        List<LeaveBalance> balances = leaveBalanceRepository.findAll();

        for (LeaveBalance balance : balances) {
            BigDecimal earned = balance.getEarnedBalance();
            if (earned == null) {
                continue;
            }
            if (earned.compareTo(BigDecimal.TEN) > 0) {
                log.info("Employee={} Earned={} CarryForward={}",
                        balance.getEmployee().getUsername(),
                        earned,
                        BigDecimal.TEN);
                balance.setEarnedBalance(BigDecimal.TEN);
            }
        }

        leaveBalanceRepository.saveAll(balances);

        log.info("YEAR-END CARRY FORWARD COMPLETED");
    }
}
