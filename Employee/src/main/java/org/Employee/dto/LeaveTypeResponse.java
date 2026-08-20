package org.Employee.dto;

public class LeaveTypeResponse {

    private Long id;
    private String name;
    private Integer maxDaysPerYear;

    public LeaveTypeResponse() {}

    public LeaveTypeResponse(Long id, String name, Integer maxDaysPerYear) {
        this.id = id;
        this.name = name;
        this.maxDaysPerYear = maxDaysPerYear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getMaxDaysPerYear() { return maxDaysPerYear; }
    public void setMaxDaysPerYear(Integer maxDaysPerYear) { this.maxDaysPerYear = maxDaysPerYear; }
}
