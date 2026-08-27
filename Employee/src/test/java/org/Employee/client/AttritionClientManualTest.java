package org.Employee.client;

import java.util.List;

import org.Employee.dto.AttritionPredictionRequest;
import org.Employee.dto.AttritionPredictionResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

/**
 * Manual verification only - requires ai-service running locally on
 * localhost:8000 (uvicorn main:app). Not part of the regular build/CI;
 * enable and run by hand to sanity-check AttritionClient against a live
 * ai-service. Not wired into the payroll/leave batch flow yet - that's
 * Day 8.
 */
@Disabled("Manual only - requires ai-service running locally on localhost:8000")
class AttritionClientManualTest {

    private final AttritionClient client =
            new AttritionClient(RestClient.builder(), "http://localhost:8000");

    @Test
    void predict_returnsRealScoreFromLiveAiService() {
        AttritionPredictionRequest request = new AttritionPredictionRequest();
        request.setTenureYears(1.5);
        request.setRoleLevel(1);
        request.setOvertimeHoursMonthly(35.0);
        request.setAbsenceRate(0.12);
        request.setLeaveUtilizationRate(0.3);
        request.setMonthsSincePromotion(40.0);
        request.setSalaryPercentileInDept(15.0);
        request.setJobSatisfaction(2);
        request.setDepartment("Sales");

        AttritionPredictionResponse response = client.predict(request);

        System.out.println("predict() -> risk_score=" + response.getRiskScore()
                + " risk_band=" + response.getRiskBand());
    }

    @Test
    void predictBatch_returnsResponsesInRequestOrder() {
        AttritionPredictionRequest highRisk = new AttritionPredictionRequest();
        highRisk.setTenureYears(0.5);
        highRisk.setRoleLevel(1);
        highRisk.setOvertimeHoursMonthly(45.0);
        highRisk.setAbsenceRate(0.2);
        highRisk.setLeaveUtilizationRate(0.1);
        highRisk.setMonthsSincePromotion(6.0);
        highRisk.setSalaryPercentileInDept(10.0);
        highRisk.setJobSatisfaction(1);
        highRisk.setDepartment("Operations");

        AttritionPredictionRequest lowRisk = new AttritionPredictionRequest();
        lowRisk.setTenureYears(12.0);
        lowRisk.setRoleLevel(5);
        lowRisk.setOvertimeHoursMonthly(0.0);
        lowRisk.setAbsenceRate(0.01);
        lowRisk.setLeaveUtilizationRate(0.7);
        lowRisk.setMonthsSincePromotion(2.0);
        lowRisk.setSalaryPercentileInDept(95.0);
        lowRisk.setJobSatisfaction(5);
        lowRisk.setDepartment("HR");

        List<AttritionPredictionResponse> responses =
                client.predictBatch(List.of(highRisk, lowRisk));

        responses.forEach(r ->
                System.out.println("predictBatch() -> risk_score=" + r.getRiskScore()
                        + " risk_band=" + r.getRiskBand()));
    }
}
