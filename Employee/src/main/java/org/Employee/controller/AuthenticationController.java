package org.Employee.controller;

import org.Employee.dto.LoginRequest;
import org.Employee.dto.LoginResponse;
import org.Employee.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(
            AuthenticationService authenticationService) {

        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest loginRequest) {

        LoginResponse response =
                authenticationService.login(loginRequest);

        return ResponseEntity.ok(response);
    }
}