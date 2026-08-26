package org.Employee.controller;

import jakarta.validation.Valid;
import org.Employee.dto.CreateReviewRequest;
import org.Employee.dto.ReviewResponse;
import org.Employee.service.PerformanceReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final PerformanceReviewService reviewService;

    public ReviewController(PerformanceReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
    public ResponseEntity<ReviewResponse> create(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(reviewService.create(authentication.getName(), request));
    }

    @GetMapping("/employee/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
    public ResponseEntity<List<ReviewResponse>> getByEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getByEmployee(id));
    }
}
