package com.barangay.domain.exceptions;

/**
 * Exception thrown when a user attempts an unauthorized action
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
