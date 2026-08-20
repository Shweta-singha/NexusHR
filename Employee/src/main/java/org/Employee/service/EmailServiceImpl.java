package org.Employee.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Employee.entity.NotificationLog;
import org.Employee.repository.NotificationLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService
{
    private static final String CHANNEL_EMAIL = "EMAIL";

    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async("taskExecutor")
    public void sendLeaveApprovedEmail(String to, String employeeName, String leaveType) {
        String subject = "Leave Request Approved";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(
                "Dear " + employeeName + ",\n\n" +
                "Your " + leaveType + " leave request has been approved.\n\n" +
                "Regards,\nHR Nexus"
        );
        sendWithRetryAndLog(to, subject, message);
    }

    @Override
    @Async("taskExecutor")
    public void sendLeaveRejectedEmail(String to, String employeeName, String leaveType, String reason) {
        String subject = "Leave Request Rejected";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(
                "Dear " + employeeName + ",\n\n" +
                "Your " + leaveType + " leave request has been rejected.\n\n" +
                "Reason: " + reason + "\n\n" +
                "Regards,\nHR Nexus"
        );
        sendWithRetryAndLog(to, subject, message);
    }

    private void sendWithRetryAndLog(String to, String subject, SimpleMailMessage message) {
        try {
            mailSender.send(message);
            persistLog(to, subject, "SENT", 1);
            log.info("Email sent to {}: {}", to, subject);
            return;
        } catch (MailException e) {
            log.warn("Email send attempt 1 failed for {}: {}", to, e.getMessage());
        }

        try {
            mailSender.send(message);
            persistLog(to, subject, "RETRIED", 2);
            log.info("Email sent to {} on retry: {}", to, subject);
        } catch (MailException e) {
            persistLog(to, subject, "FAILED", 2);
            log.error("Email to {} failed after retry: {}", to, subject, e);
        }
    }

    private void persistLog(String to, String subject, String status, int attempts) {
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setRecipientEmail(to);
        notificationLog.setChannel(CHANNEL_EMAIL);
        notificationLog.setSubject(subject);
        notificationLog.setStatus(status);
        notificationLog.setAttempts(attempts);
        notificationLog.setLastAttemptAt(LocalDateTime.now());
        notificationLog.setCreatedAt(LocalDateTime.now());
        notificationLogRepository.save(notificationLog);
    }
}
