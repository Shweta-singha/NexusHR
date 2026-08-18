package org.Employee.service;

import org.Employee.dto.AttendanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Manages active SSE connections and broadcasts attendance events to all connected dashboard clients
@Service
public class AttendanceSseService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceSseService.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter addEmitter() {
        log.info("NEW SSE CLIENT CONNECTED");
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    @Scheduled(fixedRate = 30000)
    public void heartbeat() {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("alive"));
            } catch (IOException | IllegalStateException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }

    public void broadcast(AttendanceEvent event) {
        log.info("SSE BROADCASTING EVENT -> type={} employeeId={} username={}",
                event.getEventType(), event.getEmployeeId(), event.getUsername());
        log.info("ACTIVE SSE CLIENTS = {}", emitters.size());
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("attendance")
                        .data(event));
            } catch (IOException | IllegalStateException e) {
                dead.add(emitter);
                log.warn("Removed stale SSE emitter: {}", e.getMessage());
            }
        }
        emitters.removeAll(dead);
    }
}
