package org.Employee.service;

import org.Employee.audit.Auditable;
import org.Employee.dto.CreateReviewRequest;
import org.Employee.dto.ReviewResponse;
import org.Employee.entity.Employee;
import org.Employee.entity.PerformanceReview;
import org.Employee.exception.ResourceNotFoundException;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.PerformanceReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PerformanceReviewServiceImpl implements PerformanceReviewService {

    private final PerformanceReviewRepository reviewRepository;
    private final EmployeeRepository employeeRepository;

    public PerformanceReviewServiceImpl(PerformanceReviewRepository reviewRepository,
                                         EmployeeRepository employeeRepository) {
        this.reviewRepository = reviewRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Auditable(entityType = "PERFORMANCE_REVIEW", action = "CREATE")
    public ReviewResponse create(String reviewerUsername, CreateReviewRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + request.getEmployeeId()));

        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setReviewer(reviewerUsername);
        review.setReviewPeriod(request.getReviewPeriod());
        review.setRating(request.getRating());
        review.setComments(request.getComments());
        review.setCreatedAt(LocalDateTime.now());

        return toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getByEmployee(Long employeeId) {
        return reviewRepository.findByEmployeeEmployeeId(employeeId)
                .stream().map(this::toResponse).toList();
    }

    private ReviewResponse toResponse(PerformanceReview review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setEmployeeId(review.getEmployee().getEmployeeId());
        response.setEmployeeUsername(review.getEmployee().getUsername());
        response.setReviewer(review.getReviewer());
        response.setReviewPeriod(review.getReviewPeriod());
        response.setRating(review.getRating());
        response.setComments(review.getComments());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
