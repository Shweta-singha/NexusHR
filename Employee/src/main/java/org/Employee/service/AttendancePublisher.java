package org.Employee.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.Employee.dto.AttendanceEvent;
import org.Employee.dto.AttendanceEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Serializes AttendanceEvent to a JSON string and publishes it to the Redis
 * attendance-events channel via StringRedisTemplate.
 *
 * Using StringRedisTemplate (value serializer = StringRedisSerializer) ensures
 * the subscriber receives raw UTF-8 JSON bytes — no Jackson type-wrapper overhead,
 * no mismatch between the publisher's ObjectMapper and the subscriber's ObjectMapper.
 */
@Service
public class AttendancePublisher {

    private static final Logger log = LoggerFactory.getLogger(AttendancePublisher.class);
    private static final String CHANNEL = "attendance-events";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public AttendancePublisher(StringRedisTemplate stringRedisTemplate,
                               ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(EventType eventType, Long employeeId, String username) {
        AttendanceEvent event = new AttendanceEvent(
                eventType, employeeId, username, LocalDateTime.now());
        try {
            String json = objectMapper.writeValueAsString(event);
            log.info("PUBLISHING EVENT -> channel={} payload={}", CHANNEL, json);
            stringRedisTemplate.convertAndSend(CHANNEL, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize AttendanceEvent for Redis publish", e);
        }
    }
}
