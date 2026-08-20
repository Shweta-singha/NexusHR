package org.Employee.controller;

import org.Employee.dto.LeaveTypeResponse;
import org.Employee.repository.LeaveTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController {

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveTypeController(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaveTypeResponse>> getAll() {
        List<LeaveTypeResponse> types = leaveTypeRepository.findAll().stream()
                .map(t -> new LeaveTypeResponse(t.getId(), t.getName(), t.getMaxDaysPerYear()))
                .toList();
        return ResponseEntity.ok(types);
    }
}
