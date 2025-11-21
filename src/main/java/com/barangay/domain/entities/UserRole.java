package com.barangay.domain.entities;

import com.barangay.presentation.util.StringUtil;

/**
 * Domain Enumeration: UserRole
 * Defines the role hierarchy in the system.
 */
public enum UserRole {
    SUPER_ADMIN,
    ADMIN,
    CLERK,
    RESIDENT;

    @Override
    public String toString() {
        return StringUtil.toTitleCase(super.toString());
    }
}
