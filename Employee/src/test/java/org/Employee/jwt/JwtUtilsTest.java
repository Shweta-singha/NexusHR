package org.Employee.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecretKey("test-only-secret-key-not-for-real-use-32bytes-min");
        properties.setExpirationMs(900000);
        properties.setRefreshExpirationMs(604800000);
        jwtUtils = new JwtUtils(properties);
    }

    @Test
    void accessToken_isValidAndCarriesUsername() {
        String token = jwtUtils.generateAccessToken("alice");

        assertTrue(jwtUtils.validateToken(token));
        assertEquals("alice", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void accessAndRefreshTokens_areDistinguishedByType() {
        String access = jwtUtils.generateAccessToken("alice");
        String refresh = jwtUtils.generateRefreshToken("alice");

        assertTrue(jwtUtils.isAccessToken(access));
        assertFalse(jwtUtils.isRefreshToken(access));

        assertTrue(jwtUtils.isRefreshToken(refresh));
        assertFalse(jwtUtils.isAccessToken(refresh));
    }

    @Test
    void validateToken_rejectsGarbageInput() {
        assertFalse(jwtUtils.validateToken("not-a-real-jwt"));
    }

    @Test
    void validateToken_rejectsTokenSignedWithDifferentKey() {
        JwtProperties otherProperties = new JwtProperties();
        otherProperties.setSecretKey("a-completely-different-secret-key-also-32-bytes-min");
        otherProperties.setExpirationMs(900000);
        JwtUtils otherJwtUtils = new JwtUtils(otherProperties);

        String tokenFromOtherIssuer = otherJwtUtils.generateAccessToken("alice");

        assertFalse(jwtUtils.validateToken(tokenFromOtherIssuer));
    }

    @Test
    void expirationFromToken_matchesConfiguredExpiry() {
        long before = System.currentTimeMillis();
        String token = jwtUtils.generateAccessToken("alice");
        long after = System.currentTimeMillis();

        long expiryMillis = jwtUtils.getExpirationFromToken(token).getTime();

        // JWT "exp" is a NumericDate - seconds since epoch - so serialization
        // truncates sub-second precision; allow a full second of slack below.
        assertTrue(expiryMillis >= before + 900000 - 1000);
        assertTrue(expiryMillis <= after + 900000);
    }
}
