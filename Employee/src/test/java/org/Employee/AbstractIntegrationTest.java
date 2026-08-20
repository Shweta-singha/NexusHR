package org.Employee;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Postgres + Redis containers for @SpringBootTest classes. Declared as
 * static fields on this base class (Testcontainers' singleton-container
 * pattern) so they start once per JVM and are reused across every subclass,
 * instead of each test class paying container-startup cost separately.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7")).withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        // Overridden directly so tests don't depend on the JWT_SECRET_KEY env
        // var being set in whatever shell runs the build.
        registry.add("app.security.jwt.secret-key",
                () -> "test-only-secret-key-not-for-real-use-32bytes-min");
    }
}
