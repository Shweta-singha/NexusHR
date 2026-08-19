package org.Employee.controller;

import org.Employee.dto.LeaveApplyRequest;
import org.Employee.dto.LeaveBalanceResponse;
import org.Employee.dto.LeaveResponse;
import org.Employee.dto.RejectLeaveRequest;
import org.Employee.entity.EmployeeLeave;
import org.Employee.service.LeaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveResponse> applyLeave(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody LeaveApplyRequest request) {
        EmployeeLeave leave = leaveService.applyLeave(userDetails.getUsername(), request);
        return ResponseEntity.ok(toResponse(leave));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaveResponse>> myLeaves(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<LeaveResponse> responses = leaveService.getMyLeaves(userDetails.getUsername())
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveBalanceResponse> myBalance(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(leaveService.getMyBalance(userDetails.getUsername()));
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveResponse> submitLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EmployeeLeave leave = leaveService.submitLeave(id, userDetails.getUsername());
        return ResponseEntity.ok(toResponse(leave));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveResponse> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EmployeeLeave leave = leaveService.cancelLeave(id, userDetails.getUsername());
        return ResponseEntity.ok(toResponse(leave));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public LeaveResponse approveLeave(
            @PathVariable Long id,
            Authentication authentication) {
        EmployeeLeave leave = leaveService.approveLeave(id, authentication.getName());
        return toResponse(leave);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public LeaveResponse rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) RejectLeaveRequest request,
            Authentication authentication) {
        String reason = request != null ? request.getReason() : null;
        EmployeeLeave leave = leaveService.rejectLeave(id, authentication.getName(), reason);
        return toResponse(leave);
    }

    private LeaveResponse toResponse(EmployeeLeave leave) {
        LeaveResponse res = new LeaveResponse();
        res.setId(leave.getId());
        res.setLeaveType(leave.getLeaveType().getName());
        res.setStartDate(leave.getStartDate());
        res.setEndDate(leave.getEndDate());
        res.setTotalDays(ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1);
        res.setReason(leave.getReason());
        res.setStatus(leave.getStatus());
        res.setAppliedAt(leave.getAppliedAt());
        res.setApprovedAt(leave.getApprovedAt());
        res.setApprovedBy(leave.getApprovedBy());
        return res;
    }
}
