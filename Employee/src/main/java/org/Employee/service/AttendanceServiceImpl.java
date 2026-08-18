package org.Employee.service;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.Employee.dto.AttendanceDto;
import org.Employee.dto.AttendanceEvent.EventType;
import org.Employee.dto.AttendanceResponse;
import org.Employee.dto.AttendanceSummaryResponse;
import org.Employee.dto.OvertimeReportResponse;

import java.io.ByteArrayOutputStream;
import org.Employee.entity.Attendance;
import org.Employee.entity.AttendanceStatus;
import org.Employee.entity.Employee;
import org.Employee.repository.AttendanceRepository;
import org.Employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final double HALF_DAY_THRESHOLD = 4.0;
    private static final double FULL_DAY_THRESHOLD = 8.0;

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendancePublisher publisher;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 EmployeeRepository employeeRepository,
                                 AttendancePublisher publisher) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.publisher = publisher;
    }

    @Transactional
    @Override
    public AttendanceResponse checkIn(Long employeeId) {
        LocalDate today = LocalDate.now();

        if (attendanceRepository.findByEmployeeEmployeeIdAndDate(employeeId, today).isPresent()) {
            throw new RuntimeException("Already checked in today");
        }

        Employee employee = findEmployee(employeeId);

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(today);
        attendance.setCheckIn(LocalDateTime.now());

        AttendanceResponse response = toResponse(attendanceRepository.save(attendance));
        publisher.publish(EventType.CHECK_IN, response.getEmployeeId(), response.getUsername());
        return response;
    }

    @Transactional
    @Override
    public AttendanceResponse checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No check-in found for today"));

        if (attendance.getCheckOut() != null) {
            throw new RuntimeException("Already checked out today");
        }

        LocalDateTime checkOut = LocalDateTime.now();
        attendance.setCheckOut(checkOut);

        double hours = Duration.between(attendance.getCheckIn(), checkOut).toSeconds() / 3600.0;
        hours = Math.round(hours * 100.0) / 100.0;

        attendance.setWorkingHours(hours);
        attendance.setAttendanceStatus(calculateAttendanceStatus(hours));
        attendance.setOvertimeHours(calculateOvertimeHours(hours));

        AttendanceResponse response = toResponse(attendanceRepository.save(attendance));
        publisher.publish(EventType.CHECK_OUT, response.getEmployeeId(), response.getUsername());
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public List<AttendanceDto> getMyAttendance(Long employeeId) {
        return attendanceRepository.findByEmployeeEmployeeId(employeeId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AttendanceDto> getAttendanceByRange(Long employeeId, LocalDate from, LocalDate to) {
        return attendanceRepository.findByEmployeeEmployeeIdAndDateBetween(employeeId, from, to)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AttendanceDto> getAllAttendanceByRange(LocalDate from, LocalDate to) {
        return attendanceRepository.findByDateBetween(from, to)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public AttendanceSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        return new AttendanceSummaryResponse(
                attendanceRepository.countByDate(today),
                attendanceRepository.countCheckedIn(today),
                attendanceRepository.countCheckedOut(today),
                attendanceRepository.countOvertimeEmployees(today));
    }

    @Transactional(readOnly = true)
    @Override
    public byte[] generateAttendancePdf() {
        try {
            LocalDate today = LocalDate.now();
            List<Attendance> records = attendanceRepository.findAttendanceForReport(today);

            Document document = new Document();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, output);

            document.open();

            document.add(new Paragraph("Attendance Report"));
            document.add(new Paragraph("Generated On: " + today));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Employee");
            table.addCell("Date");
            table.addCell("Working Hours");

            for (Attendance a : records) {
                table.addCell(a.getEmployee().getUsername());
                table.addCell(a.getDate().toString());
                table.addCell(String.valueOf(a.getWorkingHours()));
            }

            document.add(table);
            document.close();

            return output.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public byte[] generateAttendanceExcel() {
        try {
            LocalDate today = LocalDate.now();
            List<Attendance> records = attendanceRepository.findAttendanceForReport(today);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Attendance Report");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Employee");
            header.createCell(1).setCellValue("Date");
            header.createCell(2).setCellValue("Working Hours");

            int rowNum = 1;
            for (Attendance a : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(a.getEmployee().getUsername());
                row.createCell(1).setCellValue(a.getDate().toString());
                row.createCell(2).setCellValue(a.getWorkingHours() != null ? a.getWorkingHours() : 0.0);
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            workbook.close();

            return output.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<OvertimeReportResponse> getOvertimeReport() {
        return attendanceRepository.findOvertimeEmployees(LocalDate.now())
                .stream()
                .map(a -> new OvertimeReportResponse(
                        a.getEmployee().getUsername(),
                        a.getOvertimeHours()))
                .toList();
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
    }

    private AttendanceStatus calculateAttendanceStatus(double hours) {
        if (hours == 0) {
            return AttendanceStatus.ABSENT;
        }
        if (hours < HALF_DAY_THRESHOLD) {
            return AttendanceStatus.HALF_DAY;
        }
        return AttendanceStatus.PRESENT;
    }

    private double calculateOvertimeHours(double hours) {
        if (hours > FULL_DAY_THRESHOLD) {
            return Math.round((hours - FULL_DAY_THRESHOLD) * 100.0) / 100.0;
        }
        return 0;
    }

    private AttendanceDto toDto(Attendance a) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getEmployeeId());
        dto.setUsername(a.getEmployee().getUsername());
        dto.setDate(a.getDate());
        dto.setCheckIn(a.getCheckIn());
        dto.setCheckOut(a.getCheckOut());
        dto.setWorkingHours(a.getWorkingHours());
        dto.setOvertimeHours(a.getOvertimeHours());
        dto.setAttendanceStatus(a.getAttendanceStatus());
        return dto;
    }

    private AttendanceResponse toResponse(Attendance a) {
        AttendanceResponse res = new AttendanceResponse();
        res.setEmployeeId(a.getEmployee().getEmployeeId());
        res.setUsername(a.getEmployee().getUsername());
        res.setDate(a.getDate());
        res.setCheckIn(a.getCheckIn());
        res.setCheckOut(a.getCheckOut());
        res.setWorkingHours(a.getWorkingHours());
        res.setOvertimeHours(a.getOvertimeHours());
        res.setAttendanceStatus(a.getAttendanceStatus());
        return res;
    }
}
