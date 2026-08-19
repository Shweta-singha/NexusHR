package org.Employee.service;

import org.Employee.dto.LeaveApplyRequest;
import org.Employee.dto.LeaveBalanceResponse;
import org.Employee.entity.EmployeeLeave;

import java.util.List;

public interface LeaveService {

    EmployeeLeave applyLeave(String username, LeaveApplyRequest request);

    EmployeeLeave submitLeave(Long leaveId, String username);

    List<EmployeeLeave> getMyLeaves(String username);

    EmployeeLeave approveLeave(Long leaveId, String managerUsername);

    EmployeeLeave rejectLeave(Long leaveId, String managerUsername, String reason);

    EmployeeLeave cancelLeave(Long leaveId, String username);

    void carryForwardBalances();

    LeaveBalanceResponse getMyBalance(String username);

    byte[] generateLeavePdf();

    byte[] generateLeaveExcel();
}
