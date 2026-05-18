package org.Employee.service;

import org.Employee.dto.LoginRequest;
import org.Employee.dto.LoginResponse;
import org.Employee.jwt.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils) {

        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse login(LoginRequest loginRequest) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(),
                                loginRequest.getPassword()));

        String token =
                jwtUtils.generateToken(authentication);

        return new LoginResponse(
                token,
                authentication.getName());
    }
}