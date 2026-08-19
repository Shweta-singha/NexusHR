package org.Employee.jwt;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;

@Component
@ConfigurationProperties(prefix = "app.security.jwt")
@Validated
public class JwtProperties {

    private static final int MIN_SECRET_KEY_BYTES = 32; // 256 bits, the HS256 floor

    @NotBlank
    private String secretKey;
    private long expirationMs = 900000;
    private long refreshExpirationMs = 604800000;

    @PostConstruct
    public void validate() {
        // @ConfigurationProperties resolves an unresolvable ${JWT_SECRET_KEY} to the
        // literal placeholder text rather than leaving it blank/null, so @NotBlank
        // alone won't catch a missing env var - check for that explicitly here.
        if (secretKey == null || secretKey.isBlank() || secretKey.startsWith("${")) {
            throw new IllegalStateException(
                    "app.security.jwt.secret-key is not set. Set the JWT_SECRET_KEY environment variable.");
        }
        if (secretKey.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_KEY_BYTES) {
            throw new IllegalStateException(
                    "app.security.jwt.secret-key must be at least 256 bits (32 bytes) for HS256.");
        }
    }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public long getExpirationMs() { return expirationMs; }
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }

    public long getRefreshExpirationMs() { return refreshExpirationMs; }
    public void setRefreshExpirationMs(long refreshExpirationMs) { this.refreshExpirationMs = refreshExpirationMs; }
}
