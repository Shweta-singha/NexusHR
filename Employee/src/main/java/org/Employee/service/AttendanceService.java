package org.Employee.service;

import org.Employee.dto.AttendanceDto;
import org.Employee.dto.AttendanceResponse;
import org.Employee.dto.AttendanceSummaryResponse;
import org.Employee.dto.OvertimeReportResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    AttendanceResponse checkIn(Long employeeId);

    AttendanceResponse checkOut(Long employeeId);

    List<AttendanceDto> getMyAttendance(Long employeeId);

    List<AttendanceDto> getAttendanceByRange(Long employeeId, LocalDate from, LocalDate to);

    List<AttendanceDto> getAllAttendanceByRange(LocalDate from, LocalDate to);

    AttendanceSummaryResponse getSummary();

    List<OvertimeReportResponse> getOvertimeReport();

    byte[] generateAttendancePdf();

    byte[] generateAttendanceExcel();
}
