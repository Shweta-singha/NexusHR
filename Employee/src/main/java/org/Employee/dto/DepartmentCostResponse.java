package org.Employee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class DepartmentCostResponse {

    private String departmentName;
    private Long employeeCount;
    private BigDecimal totalCtc;
}
