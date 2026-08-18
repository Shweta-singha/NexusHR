package org.Employee.service;

import org.Employee.dto.LeaveApplyRequest;
import org.Employee.dto.LeaveBalanceResponse;
import org.Employee.entity.EmployeeLeave;

import java.util.List;

public interface LeaveService {

    EmployeeLeave applyLeave(String username, LeaveApplyRequest request);

    List<EmployeeLeave> getMyLeaves(String username);

    EmployeeLeave approveLeave(Long leaveId, String managerUsername);

    EmployeeLeave rejectLeave(Long leaveId, String managerUsername, String reason);

    LeaveBalanceResponse getMyBalance(String username);

    byte[] generateLeavePdf();

    byte[] generateLeaveExcel();
}
