package org.Employee.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import org.Employee.dto.ChangePasswordRequest;
import org.Employee.dto.ForgotPasswordRequest;
import org.Employee.dto.LoginRequest;
import org.Employee.dto.LoginResponse;
import org.Employee.dto.RegisterRequest;
import org.Employee.dto.ResetPasswordRequest;
import org.Employee.entity.Employee;
import org.Employee.entity.LeaveBalance;
import org.Employee.entity.PasswordAudit;
import org.Employee.entity.PasswordResetToken;
import org.Employee.entity.Role;
import org.Employee.jwt.JwtUtils;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.LeaveBalanceRepository;
import org.Employee.repository.PasswordAuditRepository;
import org.Employee.repository.PasswordResetTokenRepository;
import org.Employee.repository.RoleRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String REFRESH_PREFIX   = "refresh:";

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordAuditRepository passwordAuditRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordAuditRepository passwordAuditRepository,
            PasswordEncoder passwordEncoder,
            StringRedisTemplate redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordAuditRepository = passwordAuditRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (employeeRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }
        // Public self-service registration can only ever create EMPLOYEE accounts.
        // Any role value in the request body is ignored - elevating a user to
        // MANAGER/HR_MANAGER/ADMIN requires an authenticated admin action via
        // AdminController#updateRole.
        Role role = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Role not found: EMPLOYEE"));

        Employee employee = new Employee();
        employee.setUsername(request.getUsername());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setEmail(request.getEmail());
        employee.setRole(role);
        employeeRepository.save(employee);

        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(employee);
        balance.setCasualBalance(12);
        balance.setSickBalance(12);
        balance.setEarnedBalance(java.math.BigDecimal.valueOf(24));
        balance.setCompOffBalance(0);
        leaveBalanceRepository.save(balance);

        return buildTokenPair(request.getUsername());
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));
        return buildTokenPair(auth.getName());
    }

    public LoginResponse refresh(String rawRefreshToken) {
        if (!jwtUtils.validateToken(rawRefreshToken)
                || !jwtUtils.isRefreshToken(rawRefreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String redisKey = REFRESH_PREFIX + rawRefreshToken;
        String username = redisTemplate.opsForValue().get(redisKey);
        if (username == null) {
            throw new RuntimeException("Refresh token not found or already used");
        }
        redisTemplate.delete(redisKey);
        return buildTokenPair(username);
    }

    public void logout(String bearerAccessToken, String rawRefreshToken) {
        String accessToken = extractToken(bearerAccessToken);
        if (jwtUtils.validateToken(accessToken)) {
            Date exp = jwtUtils.getExpirationFromToken(accessToken);
            long ttlMs = exp.getTime() - System.currentTimeMillis();
            if (ttlMs > 0) {
                redisTemplate.opsForValue()
                        .set(BLACKLIST_PREFIX + accessToken, "1", Duration.ofMillis(ttlMs));
            }
        }
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            redisTemplate.delete(REFRESH_PREFIX + rawRefreshToken);
        }
    }

    // Phase 1 — employee changes their own password (requires current password)
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), employee.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);

        logPasswordAction(employee.getEmployeeId(), "SELF_CHANGE", username);
    }

    // Phase 2 — generate a one-time reset token (15-minute TTL)
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        Employee employee = employeeRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmployee(employee);
        resetToken.setToken(token);
        resetToken.setExpiryTime(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    // Phase 3 — consume the token and set a new password
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token already used");
        }
        if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        Employee employee = resetToken.getEmployee();
        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        logPasswordAction(employee.getEmployeeId(), "FORGOT_PASSWORD_RESET", employee.getUsername());
    }

    private void logPasswordAction(Long employeeId, String action, String performedBy) {
        PasswordAudit audit = new PasswordAudit();
        audit.setEmployeeId(employeeId);
        audit.setAction(action);
        audit.setPerformedBy(performedBy);
        audit.setPerformedAt(LocalDateTime.now());
        passwordAuditRepository.save(audit);
    }

    private LoginResponse buildTokenPair(String username) {
        String accessToken  = jwtUtils.generateAccessToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);
        Date exp = jwtUtils.getExpirationFromToken(refreshToken);
        long ttlMs = exp.getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue()
                .set(REFRESH_PREFIX + refreshToken, username, Duration.ofMillis(ttlMs));
        return new LoginResponse(accessToken, refreshToken, username);
    }

    private String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new RuntimeException("Missing or malformed Authorization header");
    }
}
