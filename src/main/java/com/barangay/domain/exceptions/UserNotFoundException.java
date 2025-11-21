package com.barangay.domain.exceptions;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
