package org.Employee.dto;

import java.time.LocalDateTime;

public class AttendanceEvent {

    public enum EventType { CHECK_IN, CHECK_OUT }

    private EventType eventType;
    private Long employeeId;
    private String username;
    private LocalDateTime timestamp;

    public AttendanceEvent() {}

    public AttendanceEvent(EventType eventType,
                           Long employeeId,
                           String username,
                           LocalDateTime timestamp) {
        this.eventType = eventType;
        this.employeeId = employeeId;
        this.username = username;
        this.timestamp = timestamp;
    }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
