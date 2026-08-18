package org.Employee.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.Employee.dto.AttendanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
/**
 * MessageListener that receives raw UTF-8 JSON bytes from the Redis
 * attendance-events channel, deserializes them with the same ObjectMapper
 * used by AttendancePublisher, and forwards the event to all SSE clients.
 */
@Service
public class AttendanceSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(AttendanceSubscriber.class);
    private static final String CHANNEL = "attendance-events";

    private final AttendanceSseService sseService;
    private final ObjectMapper objectMapper;

    public AttendanceSubscriber(AttendanceSseService sseService, ObjectMapper objectMapper) {
        this.sseService = sseService;
        this.objectMapper = objectMapper;
    }
    @PostConstruct
    void init() {
        log.info("ATTENDANCE SUBSCRIBER CREATED");
        log.info("LISTENING ON CHANNEL {}", CHANNEL);
    }
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("REDIS MESSAGE RECEIVED -> channel={} payload={}", CHANNEL, json);
        try 
        {
            AttendanceEvent event = objectMapper.readValue(json, AttendanceEvent.class);
            sseService.broadcast(event);
        } catch (Exception e) {
            log.error("Failed to deserialize AttendanceEvent: body='{}' error={}",
                    json, e.getMessage());
        }
    }
}
