package com.barangay.application.ports;

/**
 * Port interface for password hashing.
 * Following ISP and DIP: Small focused interface that use cases depend on.
 */
public interface IPasswordHasher {
    /**
     * Hash a plain text password
     */
    String hash(String plainPassword);

    /**
     * Verify a plain text password against a hash
     */
    boolean verify(String plainPassword, String hashedPassword);
}
