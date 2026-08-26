package org.Employee.dto;

import org.Employee.enums.GoalStatus;

import java.time.LocalDate;

public class UpdateGoalRequest {

    private String title;

    private String description;

    private LocalDate targetDate;

    private GoalStatus status;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus status) { this.status = status; }
}
