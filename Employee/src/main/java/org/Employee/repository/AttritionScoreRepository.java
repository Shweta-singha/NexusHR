package org.Employee.repository;

import org.Employee.entity.AttritionScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttritionScoreRepository extends JpaRepository<AttritionScore, Long> {

    Optional<AttritionScore> findByEmployeeEmployeeId(Long employeeId);
}
