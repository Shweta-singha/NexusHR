package org.Employee.repository;

import org.Employee.entity.AttritionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AttritionScoreRepository extends JpaRepository<AttritionScore, Long> {

    Optional<AttritionScore> findByEmployeeEmployeeId(Long employeeId);

    // JOIN FETCH employee + department in one query - AttritionController's
    // list endpoint needs both for every row, so the alternative
    // (findAllByOrderByRiskScoreDesc()) would N+1 on department lazy-loads.
    @Query("""
            SELECT s FROM AttritionScore s
            JOIN FETCH s.employee e
            LEFT JOIN FETCH e.department
            ORDER BY s.riskScore DESC
            """)
    List<AttritionScore> findAllWithEmployeeOrderByRiskScoreDesc();
}
