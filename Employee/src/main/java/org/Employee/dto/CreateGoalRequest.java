package org.Employee.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class CreateGoalRequest {

    @NotBlank(message = "Goal title is required")
    private String title;

    private String description;

    private LocalDate targetDate;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
}
