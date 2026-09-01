package org.Employee.controller;

import org.Employee.dto.DepartmentCostResponse;
import org.Employee.service.AttendanceService;
import org.Employee.service.LeaveService;
import org.Employee.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final ReportService reportService;

    public ReportController(AttendanceService attendanceService, LeaveService leaveService,
                             ReportService reportService) {
        this.attendanceService = attendanceService;
        this.leaveService = leaveService;
        this.reportService = reportService;
    }

    // Same role set as AttritionController/ReviewController's reviewer
    // endpoints, for consistency across the Day 12 admin/manager screens.
    @GetMapping("/department-costs")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
    public ResponseEntity<List<DepartmentCostResponse>> getDepartmentCosts() {
        return ResponseEntity.ok(reportService.getDepartmentCosts());
    }

    @GetMapping("/attendance/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<byte[]> downloadAttendancePdf() {
        byte[] pdf = attendanceService.generateAttendancePdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=attendance-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/attendance/excel")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<byte[]> downloadAttendanceExcel() {
        byte[] excel = attendanceService.generateAttendanceExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=attendance-report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/leaves/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<byte[]> downloadLeavePdf() {
        byte[] pdf = leaveService.generateLeavePdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=leave-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/leaves/excel")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public ResponseEntity<byte[]> downloadLeaveExcel() {
        byte[] excel = leaveService.generateLeaveExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=leave-report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
