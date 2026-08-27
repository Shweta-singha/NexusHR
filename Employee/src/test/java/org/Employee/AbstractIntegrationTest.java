package org.Employee;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Postgres + Redis containers for @SpringBootTest classes. Declared as
 * static fields on this base class so they start once per JVM and are reused
 * across every subclass, instead of each test class paying container-startup
 * cost separately.
 *
 * Deliberately NOT using @Testcontainers + @Container here: that combo only
 * guarantees reuse *within* one test class - JUnit5's @Testcontainers
 * extension starts/stops per test class regardless of the static field being
 * shared, so with multiple @SpringBootTest classes each one silently spun up
 * its own fresh Postgres+Redis pair (confirmed via the logs: two containers,
 * two different random ports, same JVM). That broke the *second*
 * @SpringBootTest class's HikariPool outright (0 connections, request timed
 * out) since its DataSource had already been wired to the first pair's
 * connection details before those got torn down. Only surfaced today because
 * this project had exactly one @SpringBootTest class before now - nothing
 * ever exercised cross-class reuse until several were added. Starting the
 * containers in a static initializer instead is the actual correct
 * Testcontainers singleton pattern: no extension is managing their
 * lifecycle, so nothing stops them between test classes. Ryuk (started
 * automatically by Testcontainers, logs confirm it's active) still cleans
 * them up when the JVM exits regardless.
 */
public abstract class AbstractIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7")).withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

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
