package org.Employee.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService 
{
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendLeaveApprovedEmail(String to, String employeeName, String leaveType) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Leave Request Approved");
        message.setText(
                "Dear " + employeeName + ",\n\n" +
                "Your " + leaveType + " leave request has been approved.\n\n" +
                "Regards,\nHR Nexus"
        );
        log.info("Sending email from {} to {}", fromEmail, to);
        mailSender.send(message);
        log.info("Leave approved email sent to {}", to);
    }

    @Override
    public void sendLeaveRejectedEmail(String to, String employeeName, String leaveType, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Leave Request Rejected");
        message.setText(
                "Dear " + employeeName + ",\n\n" +
                "Your " + leaveType + " leave request has been rejected.\n\n" +
                "Reason: " + reason + "\n\n" +
                "Regards,\nHR Nexus"
        );
        log.info("Sending email from {} to {}", fromEmail, to);
        mailSender.send(message);
        log.info("Leave rejected email sent to {}", to);
    }
}
