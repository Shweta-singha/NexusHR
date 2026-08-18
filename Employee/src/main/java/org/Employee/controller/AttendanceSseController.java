package org.Employee.controller;

import org.Employee.service.AttendanceSseService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// Provides the SSE stream endpoint for the real-time attendance dashboard

@RestController
@RequestMapping("/api/attendance")
public class AttendanceSseController {

    private final AttendanceSseService sseService;

    public AttendanceSseController(AttendanceSseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter stream() {
        return sseService.addEmitter();
    }
}
