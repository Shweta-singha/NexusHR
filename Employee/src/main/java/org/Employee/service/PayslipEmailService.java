package org.Employee.service;

import org.Employee.dto.EmailPayslipResponse;

public interface PayslipEmailService {

    EmailPayslipResponse emailPayslip(Long payrollId);
}
