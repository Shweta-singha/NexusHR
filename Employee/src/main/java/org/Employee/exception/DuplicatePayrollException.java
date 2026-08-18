package org.Employee.exception;

public class DuplicatePayrollException extends RuntimeException {

    public DuplicatePayrollException(String message) {
        super(message);
    }
}
