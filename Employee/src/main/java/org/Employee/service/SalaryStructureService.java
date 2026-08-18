package org.Employee.service;

import org.Employee.dto.SalaryStructureRequest;
import org.Employee.dto.SalaryStructureResponse;

public interface SalaryStructureService {

    SalaryStructureResponse createSalaryStructure(SalaryStructureRequest request);

    SalaryStructureResponse getSalaryStructure(Long employeeId);
}
