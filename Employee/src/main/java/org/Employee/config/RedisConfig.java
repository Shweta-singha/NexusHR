package org.Employee.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.Employee.service.AttendanceSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    // -------------------------------------------------------------------------
    // Cache
    // -------------------------------------------------------------------------

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );
        return RedisCacheManager.builder(factory).cacheDefaults(config).build();
    }

    // -------------------------------------------------------------------------
    // Templates
    // -------------------------------------------------------------------------

    @Bean
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    /**
     * General-purpose template with Jackson JSON value serialization.
     * Pub/Sub uses StringRedisTemplate to guarantee plain UTF-8 string
     * payloads with no Jackson type-wrapper overhead.
     */
    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    // -------------------------------------------------------------------------
    // Pub/Sub
    // -------------------------------------------------------------------------

    @Bean
    ChannelTopic attendanceEventsTopic() {
        return new ChannelTopic("attendance-events");
    }

    /**
     * RedisMessageListenerContainer implements SmartLifecycle.
     * Spring calls afterPropertiesSet() and start() automatically —
     * do NOT call them manually here. Doing so causes:
     *   IllegalStateException: Container already initialized
     * because Spring invokes them a second time during bean initialization.
     */
    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory,
            AttendanceSubscriber attendanceSubscriber,
            ChannelTopic attendanceEventsTopic) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(attendanceSubscriber, attendanceEventsTopic);
        container.setErrorHandler(t -> log.error("REDIS LISTENER ERROR", t));

        log.info("REGISTERED SUBSCRIBER on channel '{}'", attendanceEventsTopic.getTopic());
        return container;
    }

    // -------------------------------------------------------------------------
    // Diagnostics
    // -------------------------------------------------------------------------

    @Bean
    CommandLineRunner redisConnectionTest(StringRedisTemplate redisTemplate) {
        return args -> {
            log.info("TESTING REDIS CONNECTION...");
            redisTemplate.opsForValue().set("health", "ok");
            log.info("REDIS VALUE = {}", redisTemplate.opsForValue().get("health"));
            redisTemplate.delete("health");
        };
    }

    @Bean
    CommandLineRunner redisFactoryDebug(RedisConnectionFactory factory) {
        return args -> log.info("REDIS FACTORY = {}", factory.getClass().getName());
    }

}
