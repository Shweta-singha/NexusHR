package org.Employee.service;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.Employee.dto.LeaveApplyRequest;
import org.Employee.dto.LeaveBalanceResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import org.Employee.entity.Employee;
import org.Employee.entity.EmployeeLeave;
import org.Employee.entity.LeaveAudit;
import org.Employee.entity.LeaveBalance;
import org.Employee.entity.LeaveType;
import org.Employee.enums.LeaveStatus;
import org.Employee.repository.EmployeeLeaveRepository;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.LeaveAuditRepository;
import org.Employee.repository.LeaveBalanceRepository;
import org.Employee.repository.LeaveTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveServiceImpl implements LeaveService {

    private static final Logger log = LoggerFactory.getLogger(LeaveServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeLeaveRepository leaveRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveAuditRepository leaveAuditRepository;
    private final EmailService emailService;

    public LeaveServiceImpl(EmployeeRepository employeeRepository,
                            EmployeeLeaveRepository leaveRepository,
                            LeaveTypeRepository leaveTypeRepository,
                            LeaveBalanceRepository leaveBalanceRepository,
                            LeaveAuditRepository leaveAuditRepository,
                            EmailService emailService) {
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveAuditRepository = leaveAuditRepository;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public EmployeeLeave applyLeave(String username, LeaveApplyRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + username));

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("Leave type not found: " + request.getLeaveTypeId()));

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeEmployeeId(employee.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Leave balance not found for employee: " + username));

        checkBalance(balance, leaveType.getName(), days);

        if (leaveRepository.existsOverlap(
                employee.getEmployeeId(), request.getStartDate(), request.getEndDate())) {
            throw new RuntimeException(
                    "You already have a SUBMITTED or APPROVED leave that overlaps the requested date range");
        }

        EmployeeLeave leave = new EmployeeLeave();
        leave.setEmployee(employee);
        leave.setLeaveType(leaveType);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStatus(LeaveStatus.SUBMITTED);
        leave.setAppliedAt(LocalDateTime.now());

        return leaveRepository.save(leave);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EmployeeLeave> getMyLeaves(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + username));
        return leaveRepository.findByEmployeeEmployeeId(employee.getEmployeeId());
    }

    @Transactional(readOnly = true)
    @Override
    public LeaveBalanceResponse getMyBalance(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + username));
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeEmployeeId(employee.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Leave balance not found for employee: " + username));
        return new LeaveBalanceResponse(
                balance.getCasualBalance(),
                balance.getSickBalance(),
                balance.getEarnedBalance(),
                balance.getCompOffBalance());
    }

    @Transactional
    @Override
    public EmployeeLeave approveLeave(Long leaveId, String managerUsername) {
        EmployeeLeave leave = leaveRepository.findByIdFetched(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found: " + leaveId));

        if (leave.getStatus() != LeaveStatus.SUBMITTED) {
            throw new RuntimeException(
                    "Only SUBMITTED leaves can be approved. Current status: " + leave.getStatus());
        }

        deductBalance(leave);

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedAt(LocalDateTime.now());
        leave.setApprovedBy(managerUsername);
        leaveRepository.save(leave);

        logLeaveAction(leaveId, "APPROVED", managerUsername);

        try {
            emailService.sendLeaveApprovedEmail(
                    leave.getEmployee().getEmail(),
                    leave.getEmployee().getUsername(),
                    leave.getLeaveType().getName());
        } catch (Exception e) {
            log.warn("Failed to send leave approved email to {}: {}", leave.getEmployee().getEmail(), e.getMessage());
        }

        return leave;
    }

    @Transactional
    @Override
    public EmployeeLeave rejectLeave(Long leaveId, String managerUsername, String reason) {
        EmployeeLeave leave = leaveRepository.findByIdFetched(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found: " + leaveId));

        if (leave.getStatus() != LeaveStatus.SUBMITTED) {
            throw new RuntimeException(
                    "Only SUBMITTED leaves can be rejected. Current status: " + leave.getStatus());
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedAt(LocalDateTime.now());
        leave.setApprovedBy(managerUsername);
        leaveRepository.save(leave);

        logLeaveAction(leaveId, "REJECTED", managerUsername);

        try {
            emailService.sendLeaveRejectedEmail(
                    leave.getEmployee().getEmail(),
                    leave.getEmployee().getUsername(),
                    leave.getLeaveType().getName(),
                    reason != null ? reason : "No reason provided");
        } catch (Exception e) {
            log.warn("Failed to send leave rejected email to {}: {}", leave.getEmployee().getEmail(), e.getMessage());
        }

        return leave;
    }

    @Transactional(readOnly = true)
    @Override
    public byte[] generateLeavePdf() {
        try {
            List<EmployeeLeave> leaves = leaveRepository.findAllForReport();

            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();
            document.add(new Paragraph("Leave Report"));
            document.add(new Paragraph("Generated On: " + LocalDate.now()));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Employee");
            table.addCell("Leave Type");
            table.addCell("Status");

            for (EmployeeLeave leave : leaves) {
                table.addCell(leave.getEmployee().getUsername());
                table.addCell(leave.getLeaveType().getName());
                table.addCell(leave.getStatus().name());
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Leave PDF", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public byte[] generateLeaveExcel() {
        try {
            List<EmployeeLeave> leaves = leaveRepository.findAllForReport();

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Leave Report");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Employee");
            header.createCell(1).setCellValue("Leave Type");
            header.createCell(2).setCellValue("Status");

            int rowNum = 1;
            for (EmployeeLeave leave : leaves) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(leave.getEmployee().getUsername());
                row.createCell(1).setCellValue(leave.getLeaveType().getName());
                row.createCell(2).setCellValue(leave.getStatus().name());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Leave Excel", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void checkBalance(LeaveBalance balance, String leaveTypeName, long days) {
        double available = switch (leaveTypeName) {
            case "CASUAL"   -> balance.getCasualBalance();
            case "SICK"     -> balance.getSickBalance();
            case "EARNED"   -> balance.getEarnedBalance().doubleValue();
            case "COMP_OFF" -> balance.getCompOffBalance();
            default -> throw new RuntimeException("Unknown leave type: " + leaveTypeName);
        };
        if (available < days) {
            throw new RuntimeException(
                    "Insufficient " + leaveTypeName + " balance. Available: " + available
                    + ", Requested: " + days);
        }
    }

    private void deductBalance(EmployeeLeave leave) {
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeEmployeeId(leave.getEmployee().getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Leave balance not found"));

        long days = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
        String leaveTypeName = leave.getLeaveType().getName();

        checkBalance(balance, leaveTypeName, days);

        switch (leaveTypeName) {
            case "CASUAL"   -> balance.setCasualBalance(balance.getCasualBalance()   - (int) days);
            case "SICK"     -> balance.setSickBalance(balance.getSickBalance()       - (int) days);
            case "EARNED"   -> balance.setEarnedBalance(balance.getEarnedBalance().subtract(java.math.BigDecimal.valueOf(days)));
            case "COMP_OFF" -> balance.setCompOffBalance(balance.getCompOffBalance() - (int) days);
            default -> throw new RuntimeException("Unknown leave type: " + leaveTypeName);
        }

        leaveBalanceRepository.save(balance);
    }

    private void logLeaveAction(Long leaveId, String action, String performedBy) {
        LeaveAudit audit = new LeaveAudit();
        audit.setLeaveId(leaveId);
        audit.setAction(action);
        audit.setPerformedBy(performedBy);
        audit.setPerformedAt(LocalDateTime.now());
        leaveAuditRepository.save(audit);
    }
}
