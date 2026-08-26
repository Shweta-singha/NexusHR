package org.Employee.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method whose successful completion should be recorded in
 * audit_log. Only fires on successful returns, matching how leave_audit and
 * payroll_audit already only log completed transitions, not failed attempts.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {

    String entityType();

    String action();
}
