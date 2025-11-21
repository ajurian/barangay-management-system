package com.barangay.domain.entities;

import com.barangay.presentation.util.StringUtil;

public enum ApplicationType {
    NEW_REGISTRATION,
    TRANSFER,
    REACTIVATION;

    @Override
    public String toString() {
        return StringUtil.toTitleCase(super.toString());
    }
}
