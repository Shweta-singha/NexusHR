package org.Employee.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AttritionScoreResponse {

    private Long employeeId;
    private String employeeUsername;
    private String department;
    private BigDecimal riskScore;
    private String riskBand;
    private LocalDateTime scoredAt;
}
