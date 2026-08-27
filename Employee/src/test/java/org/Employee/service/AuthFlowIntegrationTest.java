package org.Employee.service;

import org.Employee.AbstractIntegrationTest;
import org.Employee.dto.LoginRequest;
import org.Employee.dto.LoginResponse;
import org.Employee.entity.Employee;
import org.Employee.entity.Role;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "AuthFlowTest@2026";

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationService authenticationService;

    private String username;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findByName("EMPLOYEE").orElseThrow();
        username = "auth-flow-test-" + System.nanoTime();

        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setEmail(username + "@example.com");
        employee.setPassword(passwordEncoder.encode(PASSWORD));
        employee.setRole(role);
        employeeRepository.save(employee);
    }

    @Test
    void login_returnsValidAccessAndRefreshTokens() {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(PASSWORD);

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(username, response.getUsername());
        assertNotEquals(response.getToken(), response.getRefreshToken());
    }

    @Test
    void refresh_rotatesTokens_andInvalidatesTheOldRefreshToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(PASSWORD);
        LoginResponse original = authenticationService.login(loginRequest);

        LoginResponse rotated = authenticationService.refresh(original.getRefreshToken());

        assertNotEquals(original.getToken(), rotated.getToken());
        assertNotEquals(original.getRefreshToken(), rotated.getRefreshToken());
        assertEquals(username, rotated.getUsername());

        // The old refresh token was revoked by the rotation above - reusing it
        // must fail, not silently issue yet another pair.
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authenticationService.refresh(original.getRefreshToken()));
        assertTrue(ex.getMessage().contains("not found") || ex.getMessage().contains("already used"),
                "unexpected message: " + ex.getMessage());
    }

    @Test
    void refresh_withNewlyRotatedToken_stillWorks() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(PASSWORD);
        LoginResponse original = authenticationService.login(loginRequest);

        LoginResponse rotated = authenticationService.refresh(original.getRefreshToken());

        // The NEW refresh token (not the revoked old one) must still be usable.
        LoginResponse rotatedAgain = authenticationService.refresh(rotated.getRefreshToken());
        assertNotEquals(rotated.getRefreshToken(), rotatedAgain.getRefreshToken());
    }
}
