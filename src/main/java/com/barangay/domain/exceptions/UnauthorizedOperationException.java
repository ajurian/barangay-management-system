package com.barangay.domain.exceptions;

public class UnauthorizedOperationException extends DomainException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
