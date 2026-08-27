package org.Employee.batch.writer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.Employee.batch.processor.EmployeeAttritionFeatures;
import org.Employee.client.AttritionClient;
import org.Employee.dto.AttritionPredictionRequest;
import org.Employee.dto.AttritionPredictionResponse;
import org.Employee.entity.AttritionScore;
import org.Employee.repository.AttritionScoreRepository;
import org.Employee.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttritionScoreWriter implements ItemWriter<EmployeeAttritionFeatures> {

    private static final Logger log = LoggerFactory.getLogger(AttritionScoreWriter.class);

    private static final String MODEL_VERSION = "rf-v1";

    private final AttritionClient attritionClient;
    private final AttritionScoreRepository attritionScoreRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends EmployeeAttritionFeatures> chunk) {
        List<? extends EmployeeAttritionFeatures> items = chunk.getItems();
        List<AttritionPredictionRequest> requests = items.stream()
                .map(EmployeeAttritionFeatures::features)
                .toList();

        List<AttritionPredictionResponse> responses;
        try {
            responses = attritionClient.predictBatch(requests);
        } catch (RestClientException e) {
            // ai-service being unreachable/slow shouldn't take down the
            // whole batch job (or the app) - log and skip this chunk,
            // leaving its employees' existing scores (if any) untouched
            // until the next successful run.
            log.error("Attrition scoring skipped for {} employees - ai-service call failed: {}",
                    items.size(), e.getMessage());
            return;
        }

        if (responses.size() != items.size()) {
            log.error("Attrition scoring skipped - ai-service returned {} scores for {} employees",
                    responses.size(), items.size());
            return;
        }

        LocalDateTime scoredAt = LocalDateTime.now();
        for (int i = 0; i < items.size(); i++) {
            Long employeeId = items.get(i).employeeId();
            AttritionPredictionResponse response = responses.get(i);

            AttritionScore score = attritionScoreRepository.findByEmployeeEmployeeId(employeeId)
                    .orElseGet(() -> AttritionScore.builder()
                            .employee(employeeRepository.getReferenceById(employeeId))
                            .build());

            score.setRiskScore(BigDecimal.valueOf(response.getRiskScore()).setScale(4, RoundingMode.HALF_UP));
            score.setRiskBand(response.getRiskBand());
            score.setScoredAt(scoredAt);
            score.setModelVersion(MODEL_VERSION);

            attritionScoreRepository.save(score);

            log.info("Attrition score written for employee ID: {} | risk_score={} risk_band={}",
                    employeeId, score.getRiskScore(), score.getRiskBand());
        }
    }
}
