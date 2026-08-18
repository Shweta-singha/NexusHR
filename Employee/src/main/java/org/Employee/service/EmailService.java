package org.Employee.service;

public interface EmailService {

    void sendLeaveApprovedEmail(String to, String employeeName, String leaveType);

    void sendLeaveRejectedEmail(String to, String employeeName, String leaveType, String reason);
}
