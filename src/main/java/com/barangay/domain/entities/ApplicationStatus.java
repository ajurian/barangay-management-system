package com.barangay.domain.entities;

import com.barangay.presentation.util.StringUtil;

public enum ApplicationStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    SCHEDULED,
    VERIFIED;

    @Override
    public String toString() {
        return StringUtil.toTitleCase(super.toString());
    }
}
