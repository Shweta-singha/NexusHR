package org.Employee.batch.processor;

import org.Employee.dto.AttritionPredictionRequest;

/**
 * Pairs a chunk item's employee id with the feature request built for it.
 * AttritionScoreWriter needs the id to know which employee each element of
 * AttritionClient.predictBatch()'s response list belongs to (same order as
 * the request list, per Day 7's verification) - employee_id isn't itself
 * one of the model's features, so it doesn't belong on
 * AttritionPredictionRequest, which is serialized directly as the request
 * body.
 */
public record EmployeeAttritionFeatures(Long employeeId, AttritionPredictionRequest features) {
}
