package org.Employee.repository;

import org.Employee.entity.EmployeeLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long> {

    @Query("SELECT el FROM EmployeeLeave el JOIN FETCH el.employee e JOIN FETCH el.leaveType lt WHERE e.employeeId = :employeeId ORDER BY el.appliedAt DESC")
    List<EmployeeLeave> findByEmployeeEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT el FROM EmployeeLeave el JOIN FETCH el.employee e JOIN FETCH el.leaveType lt WHERE el.id = :id")
    Optional<EmployeeLeave> findByIdFetched(@Param("id") Long id);

    @Query("SELECT l FROM EmployeeLeave l JOIN FETCH l.employee JOIN FETCH l.leaveType ORDER BY l.id DESC")
    List<EmployeeLeave> findAllForReport();

    @Query("SELECT CASE WHEN COUNT(el) > 0 THEN true ELSE false END FROM EmployeeLeave el " +
           "WHERE el.employee.employeeId = :employeeId " +
           "AND el.status IN (org.Employee.enums.LeaveStatus.SUBMITTED, org.Employee.enums.LeaveStatus.APPROVED) " +
           "AND el.startDate <= :endDate AND el.endDate >= :startDate")
    boolean existsOverlap(@Param("employeeId") Long employeeId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);
}
