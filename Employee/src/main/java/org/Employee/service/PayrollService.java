package org.Employee.service;

import org.Employee.dto.GenerateMonthlyPayrollRequest;
import org.Employee.dto.GenerateMonthlyPayrollResponse;
import org.Employee.dto.GeneratePayrollRequest;
import org.Employee.dto.GeneratePayrollResponse;

import java.util.List;

public interface PayrollService {

    GeneratePayrollResponse generatePayroll(GeneratePayrollRequest request);

    GenerateMonthlyPayrollResponse generateMonthlyPayroll(GenerateMonthlyPayrollRequest request);

    GeneratePayrollResponse getPayroll(Long payrollId);

    List<GeneratePayrollResponse> getPayrollHistory(Long employeeId);

    List<GeneratePayrollResponse> getPayrollByMonth(String payrollMonth);

    GeneratePayrollResponse approvePayroll(Long payrollId);

    GeneratePayrollResponse lockPayroll(Long payrollId);

    GeneratePayrollResponse markPayrollPaid(Long payrollId);

    byte[] generatePfEsiChallan(String payrollMonth);
}
