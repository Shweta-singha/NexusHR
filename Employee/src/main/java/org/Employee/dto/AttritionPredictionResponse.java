package org.Employee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttritionPredictionResponse {

    @JsonProperty("risk_score")
    private Double riskScore;

    @JsonProperty("risk_band")
    private String riskBand;
}
