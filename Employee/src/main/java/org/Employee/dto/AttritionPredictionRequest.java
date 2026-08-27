package org.Employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Field names and JSON keys must match ai-service/FEATURE_CONTRACT.md exactly.
 */
@Getter
@Setter
public class AttritionPredictionRequest {

    @JsonProperty("tenure_years")
    private Double tenureYears;

    @JsonProperty("role_level")
    private Integer roleLevel;

    @JsonProperty("overtime_hours_monthly")
    private Double overtimeHoursMonthly;

    @JsonProperty("absence_rate")
    private Double absenceRate;

    @JsonProperty("leave_utilization_rate")
    private Double leaveUtilizationRate;

    @JsonProperty("months_since_promotion")
    private Double monthsSincePromotion;

    @JsonProperty("salary_percentile_in_dept")
    private Double salaryPercentileInDept;

    @JsonProperty("job_satisfaction")
    private Integer jobSatisfaction;

    @JsonProperty("department")
    private String department;
}
