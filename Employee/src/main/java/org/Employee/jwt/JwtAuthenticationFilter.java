package org.Employee.jwt;
import java.io.IOException;
import java.util.List;

import org.Employee.service.CustomUserDetailsService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;
    private final StringRedisTemplate redisTemplate;

    // Only the genuinely public auth endpoints - "/api/auth/" as a blanket
    // prefix also skipped /api/auth/me and /api/auth/change-password, which
    // both need SecurityContextHolder's Authentication populated to work.
    private static final List<String> EXCLUDED_PATHS = List.of(
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/v3/api-docs",
        "/swagger-ui",
        "/swagger-resources",
        "/webjars"
    );

    public JwtAuthenticationFilter(
            JwtUtils jwtUtils,
            CustomUserDetailsService customUserDetailsService,
            StringRedisTemplate redisTemplate) {
        this.jwtUtils = jwtUtils;
        this.customUserDetailsService = customUserDetailsService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        // Reject refresh tokens — they are only valid for /api/auth/refresh
        if (!jwtUtils.isAccessToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Reject blacklisted tokens
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception ignored) {}

        if (jwtUtils.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = jwtUtils.getUsernameFromToken(token);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
