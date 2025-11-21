package com.barangay.application.services;

import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;

/**
 * Session manager to track current logged-in user.
 * Following Singleton pattern.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public UserRole getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public void logout() {
        this.currentUser = null;
    }
}
