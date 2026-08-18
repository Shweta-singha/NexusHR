package org.Employee.controller;

import org.Employee.jwt.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class DebugController {

    private final JwtUtils jwtUtils;

    public DebugController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/debug")
    public String debug(Authentication authentication) {
        if (authentication == null) {
            return "AUTH NULL — JWT filter did not set authentication";
        }
        return "User: " + authentication.getName()
                + " | Authorities: " + authentication.getAuthorities();
    }

    @GetMapping("/debug/me")
    public ResponseEntity<?> debugMe(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities()
                        .stream()
                        .map(a -> a.getAuthority())
                        .collect(Collectors.toList())
        ));
    }

    @GetMapping("/debug/token")
    public String debugToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "No Bearer token in Authorization header";
        }
        String token = authHeader.substring(7);
        return "TokenLength=" + token.length() + " | " + jwtUtils.validateTokenDebug(token);
    }
}
