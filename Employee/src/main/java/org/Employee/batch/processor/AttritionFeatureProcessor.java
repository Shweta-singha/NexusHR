package org.Employee.batch.processor;

import lombok.RequiredArgsConstructor;
import org.Employee.dto.AttritionPredictionRequest;
import org.Employee.entity.Employee;
import org.Employee.service.AttritionFeatureBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class AttritionFeatureProcessor implements ItemProcessor<Employee, EmployeeAttritionFeatures> {

    private static final Logger log = LoggerFactory.getLogger(AttritionFeatureProcessor.class);

    private final AttritionFeatureBuilder attritionFeatureBuilder;

    @Override
    public EmployeeAttritionFeatures process(Employee employee) {
        log.debug("Computing attrition features for employee ID: {}", employee.getEmployeeId());
        AttritionPredictionRequest features = attritionFeatureBuilder.buildFeatures(employee);
        return new EmployeeAttritionFeatures(employee.getEmployeeId(), features);
    }
}
