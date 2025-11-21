package com.barangay.infrastructure.security;

import com.barangay.application.ports.IPasswordHasher;
import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt implementation of password hasher.
 * Following DIP: Implements the port interface defined in application layer.
 */
public class BCryptPasswordHasher implements IPasswordHasher {

    @Override
    public String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    @Override
    public boolean verify(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
