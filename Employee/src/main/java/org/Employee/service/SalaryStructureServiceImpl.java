package org.Employee.service;

import org.Employee.dto.SalaryStructureRequest;
import org.Employee.dto.SalaryStructureResponse;
import org.Employee.entity.Employee;
import org.Employee.entity.SalaryStructure;
import org.Employee.exception.DuplicateResourceException;
import org.Employee.exception.ResourceNotFoundException;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.SalaryStructureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SalaryStructureServiceImpl implements SalaryStructureService {

    private final SalaryStructureRepository salaryRepository;
    private final EmployeeRepository employeeRepository;

    public SalaryStructureServiceImpl(SalaryStructureRepository salaryRepository,
                                      EmployeeRepository employeeRepository) {
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public SalaryStructureResponse createSalaryStructure(SalaryStructureRequest request) {
        salaryRepository.findByEmployeeEmployeeId(request.getEmployeeId())
                .ifPresent(s -> { throw new DuplicateResourceException("Salary structure already exists"); });

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        double ctc = request.getBasicPay()
                + request.getHra()
                + request.getSpecialAllowance()
                + request.getConveyanceAllowance()
                + request.getMedicalAllowance()
                + request.getBonus();

        SalaryStructure salary = SalaryStructure.builder()
                .employee(employee)
                .basicPay(request.getBasicPay())
                .hra(request.getHra())
                .specialAllowance(request.getSpecialAllowance())
                .conveyanceAllowance(request.getConveyanceAllowance())
                .medicalAllowance(request.getMedicalAllowance())
                .bonus(request.getBonus())
                .ctc(ctc)
                .build();

        salaryRepository.save(salary);
        return mapToResponse(salary);
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryStructureResponse getSalaryStructure(Long employeeId) {
        SalaryStructure salary = salaryRepository.findByEmployeeEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found"));
        return mapToResponse(salary);
    }

    private SalaryStructureResponse mapToResponse(SalaryStructure salary) {
        SalaryStructureResponse response = new SalaryStructureResponse();
        response.setId(salary.getId());
        response.setEmployeeId(salary.getEmployee().getEmployeeId());
        response.setEmployeeName(salary.getEmployee().getUsername());
        response.setBasicPay(salary.getBasicPay());
        response.setHra(salary.getHra());
        response.setSpecialAllowance(salary.getSpecialAllowance());
        response.setConveyanceAllowance(salary.getConveyanceAllowance());
        response.setMedicalAllowance(salary.getMedicalAllowance());
        response.setBonus(salary.getBonus());
        response.setCtc(salary.getCtc());
        return response;
    }
}
