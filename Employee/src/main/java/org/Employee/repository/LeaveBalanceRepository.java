package org.Employee.repository;

import org.Employee.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    @Query("SELECT lb FROM LeaveBalance lb JOIN FETCH lb.employee e WHERE e.employeeId = :employeeId")
    Optional<LeaveBalance> findByEmployeeEmployeeId(@Param("employeeId") Long employeeId);
}
