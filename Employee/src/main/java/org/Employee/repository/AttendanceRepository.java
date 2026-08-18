package org.Employee.repository;
import org.Employee.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE e.employeeId = :employeeId AND a.date = :date")
    Optional<Attendance> findByEmployeeEmployeeIdAndDate(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE e.employeeId = :employeeId ORDER BY a.date DESC")
    List<Attendance> findByEmployeeEmployeeId(
            @Param("employeeId") Long employeeId);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE e.employeeId = :employeeId AND a.date BETWEEN :from AND :to ORDER BY a.date DESC")
    List<Attendance> findByEmployeeEmployeeIdAndDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE a.date BETWEEN :from AND :to ORDER BY a.date DESC")
    List<Attendance> findByDateBetween(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    long countByDate(LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date AND a.checkIn IS NOT NULL")
    long countCheckedIn(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date AND a.checkOut IS NOT NULL")
    long countCheckedOut(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date AND a.overtimeHours > 0")
    long countOvertimeEmployees(@Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE a.date = :date AND a.overtimeHours > 0")
    List<Attendance> findOvertimeEmployees(@Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e WHERE a.date = :date")
    List<Attendance> findAttendanceForReport(@Param("date") LocalDate date);
}
