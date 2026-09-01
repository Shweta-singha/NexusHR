package org.Employee.service;

import org.Employee.dto.DepartmentCostResponse;
import org.Employee.repository.SalaryStructureRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReportService {

    private final SalaryStructureRepository salaryStructureRepository;

    public ReportService(SalaryStructureRepository salaryStructureRepository) {
        this.salaryStructureRepository = salaryStructureRepository;
    }

    public List<DepartmentCostResponse> getDepartmentCosts() {
        return salaryStructureRepository.findCostByDepartment().stream()
                .map(row -> new DepartmentCostResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        row[2] instanceof BigDecimal bd ? bd : BigDecimal.valueOf(((Number) row[2]).doubleValue())))
                .toList();
    }
}
