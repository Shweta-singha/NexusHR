package org.Employee.service;

import org.Employee.audit.Auditable;
import org.Employee.dto.CreateGoalRequest;
import org.Employee.dto.GoalResponse;
import org.Employee.dto.UpdateGoalRequest;
import org.Employee.entity.Employee;
import org.Employee.entity.Goal;
import org.Employee.enums.GoalStatus;
import org.Employee.exception.ResourceNotFoundException;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.GoalRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final EmployeeRepository employeeRepository;

    public GoalServiceImpl(GoalRepository goalRepository, EmployeeRepository employeeRepository) {
        this.goalRepository = goalRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Auditable(entityType = "GOAL", action = "CREATE")
    public GoalResponse create(String username, CreateGoalRequest request) {
        Employee employee = findEmployee(username);

        Goal goal = new Goal();
        goal.setEmployee(employee);
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setTargetDate(request.getTargetDate());
        goal.setStatus(GoalStatus.NOT_STARTED);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());

        return toResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> getMyGoals(String username) {
        Employee employee = findEmployee(username);
        return goalRepository.findByEmployeeEmployeeId(employee.getEmployeeId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Auditable(entityType = "GOAL", action = "UPDATE")
    public GoalResponse update(Long goalId, String username, UpdateGoalRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + goalId));

        if (!goal.getEmployee().getUsername().equals(username)) {
            throw new AccessDeniedException("You can only update your own goals");
        }

        if (request.getTitle() != null) goal.setTitle(request.getTitle());
        if (request.getDescription() != null) goal.setDescription(request.getDescription());
        if (request.getTargetDate() != null) goal.setTargetDate(request.getTargetDate());
        if (request.getStatus() != null) goal.setStatus(request.getStatus());
        goal.setUpdatedAt(LocalDateTime.now());

        return toResponse(goalRepository.save(goal));
    }

    private Employee findEmployee(String username) {
        return employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + username));
    }

    private GoalResponse toResponse(Goal goal) {
        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setEmployeeId(goal.getEmployee().getEmployeeId());
        response.setEmployeeUsername(goal.getEmployee().getUsername());
        response.setTitle(goal.getTitle());
        response.setDescription(goal.getDescription());
        response.setTargetDate(goal.getTargetDate());
        response.setStatus(goal.getStatus());
        response.setCreatedAt(goal.getCreatedAt());
        response.setUpdatedAt(goal.getUpdatedAt());
        return response;
    }
}
