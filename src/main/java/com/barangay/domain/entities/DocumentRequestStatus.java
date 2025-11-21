package com.barangay.domain.entities;

import com.barangay.presentation.util.StringUtil;

/**
 * Status lifecycle for resident document requests (Module 5).
 */
public enum DocumentRequestStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    ISSUED,
    REJECTED;

    /**
     * Returns true if transition from current to target state is allowed.
     */
    public boolean canTransitionTo(DocumentRequestStatus target) {
        switch (this) {
            case PENDING:
                return target == UNDER_REVIEW || target == REJECTED;
            case UNDER_REVIEW:
                return target == APPROVED || target == REJECTED;
            case APPROVED:
                return target == ISSUED || target == REJECTED;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return StringUtil.toTitleCase(super.toString());
    }
}
