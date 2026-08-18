package org.Employee.controller;

import jakarta.validation.Valid;
import org.Employee.dto.ChangePasswordRequest;
import org.Employee.dto.ForgotPasswordRequest;
import org.Employee.dto.LoginRequest;
import org.Employee.dto.LoginResponse;
import org.Employee.dto.RefreshRequest;
import org.Employee.dto.RegisterRequest;
import org.Employee.dto.ResetPasswordRequest;
import org.Employee.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.login(loginRequest));
    }

    /**
     * Exchange a valid refresh token for a new access + refresh token pair.
     * The old refresh token is revoked (rotation).
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(
                authenticationService.refresh(request.getRefreshToken()));
    }

    /**
     * Logout: blacklists the access token and revokes the refresh token.
     * Send the refresh token in the body to fully invalidate the session.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody(required = false) RefreshRequest body) {
        String refreshToken = (body != null) ? body.getRefreshToken() : null;
        authenticationService.logout(bearerToken, refreshToken);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // Phase 1 — authenticated employee changes own password
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        authenticationService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok("Password changed successfully");
    }

    // Phase 2 — request a reset token (public; token returned in body for now, email later)
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String token = authenticationService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("resetToken", token));
    }

    // Phase 3 — consume reset token and set new password
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }

}
