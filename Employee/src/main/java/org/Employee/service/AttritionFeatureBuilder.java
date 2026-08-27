package org.Employee.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.Employee.dto.AttritionPredictionRequest;
import org.Employee.entity.Attendance;
import org.Employee.entity.AttendanceStatus;
import org.Employee.entity.Employee;
import org.Employee.entity.EmployeeLeave;
import org.Employee.entity.SalaryStructure;
import org.Employee.enums.LeaveStatus;
import org.Employee.repository.AttendanceRepository;
import org.Employee.repository.EmployeeLeaveRepository;
import org.Employee.repository.EmployeeRepository;
import org.Employee.repository.LeaveTypeRepository;
import org.Employee.repository.SalaryStructureRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Computes the 9 ai-service/FEATURE_CONTRACT.md features for one Employee
 * from this project's real repositories. Several features have no directly
 * tracked source in this system (job satisfaction, promotion history,
 * department on most employee rows) - those fall back to clearly-labelled
 * defaults/proxies below rather than being silently fabricated. See Day 8's
 * report for the full rundown of which features are real vs. proxied and why.
 */
@Component
@RequiredArgsConstructor
public class AttritionFeatureBuilder {

    private static final Logger log = LoggerFactory.getLogger(AttritionFeatureBuilder.class);

    // No job-satisfaction survey/table exists anywhere in this system (the
    // only adjacent data, performance_reviews, has a single row across all
    // 42 seeded employees - nowhere near enough coverage to use as a proxy).
    // Fixed neutral default for every employee until real data exists.
    private static final int JOB_SATISFACTION_DEFAULT = 3;

    // Used whenever a salary percentile can't be computed at all (employee
    // has no SalaryStructure on file).
    private static final double SALARY_PERCENTILE_DEFAULT = 50.0;

    // Defensive only - V28 backfilled hire_date on every existing row, so a
    // null here would mean a row inserted after that migration without going
    // through the normal onboarding flow.
    private static final double TENURE_YEARS_DEFAULT = 1.0;
    private static final double MONTHS_SINCE_PROMOTION_DEFAULT = 24.0;

    private static final int ROLE_LEVEL_DEFAULT = 1;
    private static final Map<String, Integer> ROLE_LEVEL_BY_NAME = Map.of(
            "EMPLOYEE", 1,
            "MANAGER", 3,
            "HR_MANAGER", 4,
            "ADMIN", 5
    );

    // Real seeded department names are messy (17 rows, e.g. "Backend",
    // "DevOps"/"Devops"/"Cloud"/"Kubernetes" duplicates, "Postman Demo
    // Dept") and don't match the model's 6-category taxonomy the training
    // data used - and 39/42 employees have no department at all
    // (department_id IS NULL). Real names get folded into their closest
    // canonical bucket; anything unmapped or missing falls back to
    // "Operations" as a neutral catch-all (this seed data has no Sales or
    // Marketing departments to map to either).
    private static final String DEPARTMENT_DEFAULT = "Operations";
    private static final Map<String, String> DEPARTMENT_ALIASES = Map.ofEntries(
            Map.entry("Engineering", "Engineering"),
            Map.entry("Backend", "Engineering"),
            Map.entry("Frontend", "Engineering"),
            Map.entry("QA", "Engineering"),
            Map.entry("Devops", "Engineering"),
            Map.entry("DevOps", "Engineering"),
            Map.entry("Cloud", "Engineering"),
            Map.entry("Kubernetes", "Engineering"),
            Map.entry("HR", "HR"),
            Map.entry("Recruitment", "HR"),
            Map.entry("Finance", "Finance"),
            Map.entry("Payroll", "Finance"),
            Map.entry("Accounts", "Finance"),
            Map.entry("Audit", "Finance")
    );

    private final AttendanceRepository attendanceRepository;
    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final SalaryStructureRepository salaryStructureRepository;

    public AttritionPredictionRequest buildFeatures(Employee employee) {
        AttritionPredictionRequest request = new AttritionPredictionRequest();

        request.setTenureYears(tenureYears(employee));
        request.setRoleLevel(roleLevel(employee));
        request.setMonthsSincePromotion(monthsSincePromotion(employee));
        request.setJobSatisfaction(JOB_SATISFACTION_DEFAULT);
        request.setDepartment(department(employee));

        List<Attendance> attendance = attendanceRepository.findByEmployeeEmployeeId(employee.getEmployeeId());
        request.setOvertimeHoursMonthly(overtimeHoursMonthly(attendance));
        request.setAbsenceRate(absenceRate(attendance));

        request.setLeaveUtilizationRate(leaveUtilizationRate(employee));
        request.setSalaryPercentileInDept(salaryPercentileInDept(employee));

        return request;
    }

    private double tenureYears(Employee employee) {
        if (employee.getHireDate() == null) {
            log.warn("Employee {} has no hire_date - defaulting tenure_years to {}",
                    employee.getEmployeeId(), TENURE_YEARS_DEFAULT);
            return TENURE_YEARS_DEFAULT;
        }
        double years = ChronoUnit.DAYS.between(employee.getHireDate(), LocalDate.now()) / 365.25;
        return clamp(years, 0, 20);
    }

    private int roleLevel(Employee employee) {
        if (employee.getRole() == null || employee.getRole().getName() == null) {
            return ROLE_LEVEL_DEFAULT;
        }
        return ROLE_LEVEL_BY_NAME.getOrDefault(employee.getRole().getName(), ROLE_LEVEL_DEFAULT);
    }

    /**
     * No promotion-history table exists - Employee only tracks the single
     * most recent promotion via promoted_at, and currently 0 of 42 seeded
     * employees have it set. Falls back to months since hire_date, which
     * is the correct semantic anyway for "never promoted": time accrued
     * without advancement.
     */
    private double monthsSincePromotion(Employee employee) {
        LocalDate reference = employee.getPromotedAt() != null
                ? employee.getPromotedAt().toLocalDate()
                : employee.getHireDate();

        if (reference == null) {
            return MONTHS_SINCE_PROMOTION_DEFAULT;
        }
        double months = ChronoUnit.MONTHS.between(reference, LocalDate.now());
        return clamp(months, 0, 60);
    }

    private String department(Employee employee) {
        String realName = employeeRepository.findDepartmentNameByEmployeeId(employee.getEmployeeId())
                .orElse(null);
        if (realName == null) {
            return DEPARTMENT_DEFAULT;
        }
        return DEPARTMENT_ALIASES.getOrDefault(realName, DEPARTMENT_DEFAULT);
    }

    private double overtimeHoursMonthly(List<Attendance> attendance) {
        if (attendance.isEmpty()) {
            return 0.0;
        }
        double totalOvertime = attendance.stream()
                .mapToDouble(a -> a.getOvertimeHours() == null ? 0.0 : a.getOvertimeHours())
                .sum();
        long monthsSpanned = attendance.stream()
                .map(a -> YearMonth.from(a.getDate()))
                .distinct()
                .count();
        return clamp(totalOvertime / Math.max(monthsSpanned, 1), 0, 60);
    }

    private double absenceRate(List<Attendance> attendance) {
        if (attendance.isEmpty()) {
            return 0.0;
        }
        long absentCount = attendance.stream()
                .filter(a -> a.getAttendanceStatus() == AttendanceStatus.ABSENT)
                .count();
        return clamp((double) absentCount / attendance.size(), 0, 1);
    }

    private double leaveUtilizationRate(Employee employee) {
        List<EmployeeLeave> leaves = employeeLeaveRepository.findByEmployeeEmployeeId(employee.getEmployeeId());

        long daysTaken = leaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .mapToLong(l -> ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1)
                .sum();

        int totalEntitlement = leaveTypeRepository.findAll().stream()
                .mapToInt(lt -> lt.getMaxDaysPerYear() == null ? 0 : lt.getMaxDaysPerYear())
                .sum();

        if (totalEntitlement <= 0) {
            return 0.0;
        }
        return clamp((double) daysTaken / totalEntitlement, 0, 1);
    }

    /**
     * Percentile within the employee's own department when that's a
     * meaningful comparison (department set, at least one other department
     * peer with a salary on file); otherwise falls back to a company-wide
     * percentile. That fallback is the common case today - 39 of 42 seeded
     * employees have no department_id at all, so a strict "within null" or
     * "within a group of 1" percentile would be meaningless for most rows.
     *
     * Recomputed from all SalaryStructure rows on every call rather than
     * precomputed once per job run - O(n) per employee, but n is ~40 rows
     * here so the O(n^2) total is trivial. Revisit if this dataset ever
     * grows to a size where that stops being true.
     */
    private double salaryPercentileInDept(Employee employee) {
        Optional<SalaryStructure> own = salaryStructureRepository.findByEmployeeEmployeeId(employee.getEmployeeId());
        if (own.isEmpty()) {
            return SALARY_PERCENTILE_DEFAULT;
        }
        double ownCtc = own.get().getCtc();

        Long deptId = employeeRepository.findDepartmentIdByEmployeeId(employee.getEmployeeId()).orElse(null);

        // (departmentId, ctc) pairs for every salary structure in one query,
        // instead of loading full SalaryStructure rows and navigating each
        // one's employee.department (LAZY - unsafe outside an open session).
        List<Object[]> all = salaryStructureRepository.findAllDepartmentIdAndCtc();

        List<Double> comparisonPool = List.of();
        if (deptId != null) {
            comparisonPool = all.stream()
                    .filter(row -> deptId.equals(row[0]))
                    .map(row -> (Double) row[1])
                    .toList();
        }

        if (comparisonPool.size() < 2) {
            comparisonPool = all.stream().map(row -> (Double) row[1]).toList();
        }
        if (comparisonPool.isEmpty()) {
            return SALARY_PERCENTILE_DEFAULT;
        }

        long countBelow = comparisonPool.stream().filter(ctc -> ctc < ownCtc).count();
        return clamp(100.0 * countBelow / comparisonPool.size(), 0, 100);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
