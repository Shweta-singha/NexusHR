package org.Employee.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS  = "access";
    private static final String REFRESH = "refresh";

    private final JwtProperties jwtProperties;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    // ── generators ──────────────────────────────────────────────────────────

    public String generateToken(Authentication authentication) {
        return generateAccessToken(authentication.getName());
    }

    public String generateAccessToken(String username) {
        return buildToken(username, jwtProperties.getExpirationMs(), ACCESS);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, jwtProperties.getRefreshExpirationMs(), REFRESH);
    }

    private String buildToken(String subject, long expirationMs, String type) {
        return Jwts.builder()
                .setSubject(subject)
                .setId(UUID.randomUUID().toString()) // jti - JWTs are otherwise
                // fully deterministic given (subject, type, second-precision iat/exp),
                // so two tokens issued for the same user+type within the same
                // wall-clock second would be textually identical without this.
                // That matters here specifically: refresh-token rotation revokes
                // the old token by deleting its Redis entry keyed on the token
                // string, then stores the "new" one under its own token-string
                // key - if both strings were identical, that nets out to
                // immediately re-validating the token that was just revoked.
                .claim(TOKEN_TYPE_CLAIM, type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ── validators ───────────────────────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            Claims c = parseClaims(token);
            return ACCESS.equals(c.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims c = parseClaims(token);
            return REFRESH.equals(c.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    // ── extractors ───────────────────────────────────────────────────────────

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Date getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    // ── debug ────────────────────────────────────────────────────────────────

    public String validateTokenDebug(String token) {
        try {
            Claims c = parseClaims(token);
            return "VALID | type=" + c.get(TOKEN_TYPE_CLAIM)
                    + " | sub=" + c.getSubject()
                    + " | exp=" + c.getExpiration()
                    + " | now=" + new Date();
        } catch (Exception e) {
            return "INVALID | " + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + " | now=" + new Date()
                    + " | keyLen=" + jwtProperties.getSecretKey().length();
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
