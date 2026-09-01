package org.Employee.service;

import org.Employee.dto.AttritionScoreResponse;
import org.Employee.entity.AttritionScore;
import org.Employee.entity.Department;
import org.Employee.repository.AttritionScoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttritionScoreService {

    private final AttritionScoreRepository attritionScoreRepository;

    public AttritionScoreService(AttritionScoreRepository attritionScoreRepository) {
        this.attritionScoreRepository = attritionScoreRepository;
    }

    public List<AttritionScoreResponse> getAllScores() {
        return attritionScoreRepository.findAllWithEmployeeOrderByRiskScoreDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private AttritionScoreResponse toResponse(AttritionScore score) {
        AttritionScoreResponse response = new AttritionScoreResponse();
        response.setEmployeeId(score.getEmployee().getEmployeeId());
        response.setEmployeeUsername(score.getEmployee().getUsername());
        Department department = score.getEmployee().getDepartment();
        response.setDepartment(department != null ? department.getName() : null);
        response.setRiskScore(score.getRiskScore());
        response.setRiskBand(score.getRiskBand());
        response.setScoredAt(score.getScoredAt());
        return response;
    }
}
