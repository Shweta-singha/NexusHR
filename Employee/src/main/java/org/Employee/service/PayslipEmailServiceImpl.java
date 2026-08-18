package org.Employee.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.Employee.dto.EmailPayslipResponse;
import org.Employee.entity.PayrollRecord;
import org.Employee.exception.ResourceNotFoundException;
import org.Employee.repository.PayrollRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PayslipEmailServiceImpl implements PayslipEmailService {

    private static final Logger log = LoggerFactory.getLogger(PayslipEmailServiceImpl.class);

    private static final DateTimeFormatter MONTH_LABEL_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final PayrollRepository payrollRepository;
    private final PayslipService payslipService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    @Transactional(readOnly = true)
    public EmailPayslipResponse emailPayslip(Long payrollId) {

        PayrollRecord payroll = payrollRepository
                .findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found"));

        byte[] pdf = payslipService.generatePayslip(payrollId);

        String monthLabel = formatMonthLabel(payroll.getPayrollMonth());
        String employeeEmail = payroll.getEmployee().getEmail();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(senderEmail);
            helper.setTo(employeeEmail);
            helper.setSubject("Payslip for " + monthLabel);
            helper.setText(
                    "Dear " + payroll.getEmployee().getUsername() + ",\n\n"
                    + "Please find attached your payslip for " + monthLabel + ".\n\n"
                    + "Regards,\nNexusHR Payroll Team");

            helper.addAttachment(
                    "Payslip-" + payroll.getPayrollMonth() + ".pdf",
                    new ByteArrayResource(pdf));

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send payslip email for payroll {}", payrollId, e);
            throw new RuntimeException("Failed to send payslip email", e);
        }

        return EmailPayslipResponse.builder()
                .payrollId(payroll.getId())
                .employeeEmail(employeeEmail)
                .payrollMonth(payroll.getPayrollMonth())
                .message("Payslip emailed successfully")
                .build();
    }

    private String formatMonthLabel(String payrollMonth) {
        return YearMonth.parse(payrollMonth).format(MONTH_LABEL_FORMAT);
    }
}
