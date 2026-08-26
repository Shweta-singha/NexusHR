package org.Employee.controller;

import jakarta.validation.Valid;
import org.Employee.dto.CreateGoalRequest;
import org.Employee.dto.GoalResponse;
import org.Employee.dto.UpdateGoalRequest;
import org.Employee.service.GoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GoalResponse> create(
            @Valid @RequestBody CreateGoalRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(goalService.create(authentication.getName(), request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GoalResponse>> getMyGoals(Authentication authentication) {
        return ResponseEntity.ok(goalService.getMyGoals(authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GoalResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(goalService.update(id, authentication.getName(), request));
    }
}
