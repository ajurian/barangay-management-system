package com.barangay.application.services;

/**
 * Validator for password strength.
 * Following SRP: Only handles password validation logic.
 */
public class PasswordValidator {
    private static final int MIN_LENGTH = 8;

    /**
     * Validate password meets minimum requirements
     * 
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        return hasLetter && hasDigit;
    }

    /**
     * Get validation message
     */
    public String getValidationMessage() {
        return "Password must be at least " + MIN_LENGTH +
                " characters long and contain both letters and numbers.";
    }
}
