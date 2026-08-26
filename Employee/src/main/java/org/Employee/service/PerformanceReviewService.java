package org.Employee.service;

import org.Employee.dto.CreateReviewRequest;
import org.Employee.dto.ReviewResponse;

import java.util.List;

public interface PerformanceReviewService {

    ReviewResponse create(String reviewerUsername, CreateReviewRequest request);

    List<ReviewResponse> getByEmployee(Long employeeId);
}
