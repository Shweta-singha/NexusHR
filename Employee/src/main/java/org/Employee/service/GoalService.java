package org.Employee.service;

import org.Employee.dto.CreateGoalRequest;
import org.Employee.dto.GoalResponse;
import org.Employee.dto.UpdateGoalRequest;

import java.util.List;

public interface GoalService {

    GoalResponse create(String username, CreateGoalRequest request);

    List<GoalResponse> getMyGoals(String username);

    GoalResponse update(Long goalId, String username, UpdateGoalRequest request);
}
