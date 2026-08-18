package org.Employee.controller;
import org.Employee.dto.AttendanceDto;
import org.Employee.dto.AttendanceResponse;
import org.Employee.dto.AttendanceSummaryResponse;
import org.Employee.dto.OvertimeReportResponse;
import org.Employee.entity.Employee;
import org.Employee.repository.EmployeeRepository;
import org.Employee.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController 
{
    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;
    public AttendanceController(AttendanceService attendanceService,
                                EmployeeRepository employeeRepository) {
        this.attendanceService = attendanceService;
        this.employeeRepository = employeeRepository;
}

    // Employee checks themselves in
    @PostMapping("/check-in")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendanceResponse> checkIn(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long employeeId = resolveEmployeeId(userDetails.getUsername());
        return ResponseEntity.ok(attendanceService.checkIn(employeeId));
}

    // Employee checks themselves out
    @PostMapping("/check-out")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttendanceResponse> checkOut(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long employeeId = resolveEmployeeId(userDetails.getUsername());
        return ResponseEntity.ok(attendanceService.checkOut(employeeId));
}

    // Employee views their own attendance history
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttendanceDto>> myAttendance(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long employeeId = resolveEmployeeId(userDetails.getUsername());
        return ResponseEntity.ok(attendanceService.getMyAttendance(employeeId));
    }

    // Employee views their own attendance within a date range
    @GetMapping("/my/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttendanceDto>> myAttendanceByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long employeeId = resolveEmployeeId(userDetails.getUsername());
        return ResponseEntity.ok(attendanceService.getAttendanceByRange(employeeId, from, to));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<AttendanceSummaryResponse> getSummary() {
        return ResponseEntity.ok(attendanceService.getSummary());
    }

    @GetMapping("/overtime")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<List<OvertimeReportResponse>> getOvertimeReport() {
        return ResponseEntity.ok(attendanceService.getOvertimeReport());
    }

    // ADMIN / HR_MANAGER views all attendance within a date range
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<List<AttendanceDto>> allAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getAllAttendanceByRange(from, to));
    }

    // ADMIN / HR_MANAGER views a specific employee's attendance
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
    public ResponseEntity<List<AttendanceDto>> attendanceByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(employeeId));
    }

    private Long resolveEmployeeId(String username) {
        return employeeRepository.findByUsername(username)
                .map(Employee::getEmployeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + username));
    }
}
