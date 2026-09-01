package org.Employee.controller;

import org.Employee.dto.AttritionScoreResponse;
import org.Employee.service.AttritionScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AttritionController {

    private final AttritionScoreService attritionScoreService;

    public AttritionController(AttritionScoreService attritionScoreService) {
        this.attritionScoreService = attritionScoreService;
    }

    // Same role set as ReviewController's reviewer endpoints, for consistency.
    @GetMapping("/attrition-scores")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
    public ResponseEntity<List<AttritionScoreResponse>> getAllScores() {
        return ResponseEntity.ok(attritionScoreService.getAllScores());
    }
}
