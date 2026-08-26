package org.Employee.audit;

import org.Employee.entity.AuditLog;
import org.Employee.repository.AuditLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * Records a row in audit_log for every successful @Auditable method call.
 * Only fires on success (@Around, no logging on the exception path) - matches
 * how leave_audit/payroll_audit already only log completed transitions.
 *
 * "details" is deliberately minimal - just an id, the same convention
 * payroll_audit/leave_audit already use (a simple foreign-key reference,
 * not a full object dump). Full entity/DTO serialization was considered and
 * rejected: several audited methods return raw JPA entities (e.g. Employee,
 * whose toString() isn't overridden - safe from leaking its password hash,
 * but also risks LazyInitializationException on unfetched associations if
 * serialized generically), and request DTOs for these methods can carry a
 * plaintext password. Reflectively reading just an id getter sidesteps both.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            String performedBy = currentUsername();
            String details = detailsFor(joinPoint, result);

            auditLogRepository.save(new AuditLog(
                    auditable.entityType(),
                    auditable.action(),
                    performedBy,
                    details,
                    LocalDateTime.now()));
        } catch (Exception e) {
            // An audit-logging failure must never break the actual business
            // operation it's observing, which already succeeded above.
            log.error("Failed to write audit_log row for {} {}",
                    auditable.entityType(), auditable.action(), e);
        }

        return result;
    }

    private String currentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private String detailsFor(ProceedingJoinPoint joinPoint, Object result) {
        Object id = idOf(result);
        if (id == null && joinPoint.getArgs().length > 0) {
            id = idOf(joinPoint.getArgs()[0]);
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        return id != null ? "id=" + id : method.getName();
    }

    /** Best-effort: call a no-arg getId()/getEmployeeId() if the object has one. */
    private Object idOf(Object target) {
        if (target == null || target instanceof Number || target instanceof String) {
            return target;
        }
        // getId first (an entity's own primary key), then entity-specific ids
        // like getPayrollId, and getEmployeeId last - several response DTOs
        // (e.g. GeneratePayrollResponse) carry employeeId only as a secondary
        // reference, not their own identity, and would otherwise shadow it.
        for (String getterName : new String[] {"getId", "getPayrollId", "getEmployeeId"}) {
            try {
                Method getter = target.getClass().getMethod(getterName);
                Object value = getter.invoke(target);
                if (value != null) return value;
            } catch (ReflectiveOperationException ignored) {
                // no such getter on this type - try the next one / fall through
            }
        }
        return null;
    }
}
