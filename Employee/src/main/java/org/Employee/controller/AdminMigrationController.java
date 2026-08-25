package org.Employee.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TEMPORARY — one-time local-to-Render data migration, since this machine's
 * network blocks outbound port 5432 to Render's external Postgres URL but
 * allows 443. Rides over the app's own already-working DB connection instead
 * of a direct client connection. Remove this controller once the migration
 * is done.
 */
@RestController
@RequestMapping("/api/admin/migrate")
public class AdminMigrationController {

    private final JdbcTemplate jdbcTemplate;

    public AdminMigrationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping(value = "/execute-sql", consumes = "text/plain")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> executeSql(@RequestBody String sql) {
        jdbcTemplate.execute((java.sql.Connection conn) -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
            return null;
        });
        return ResponseEntity.ok("Executed successfully");
    }

    @GetMapping("/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> verify() {
        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("employees", jdbcTemplate.queryForObject("SELECT count(*) FROM employees", Long.class));
        counts.put("attendance", jdbcTemplate.queryForObject("SELECT count(*) FROM attendance", Long.class));
        counts.put("employee_leaves", jdbcTemplate.queryForObject("SELECT count(*) FROM employee_leaves", Long.class));
        counts.put("payroll_records", jdbcTemplate.queryForObject("SELECT count(*) FROM payroll_records", Long.class));
        counts.put("departments", jdbcTemplate.queryForObject("SELECT count(*) FROM departments", Long.class));
        counts.put("salary_structures", jdbcTemplate.queryForObject("SELECT count(*) FROM salary_structures", Long.class));
        return ResponseEntity.ok(counts);
    }
}
