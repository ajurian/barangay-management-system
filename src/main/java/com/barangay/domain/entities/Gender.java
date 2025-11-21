package com.barangay.domain.entities;

import com.barangay.presentation.util.StringUtil;

public enum Gender {
    MALE,
    FEMALE;

    @Override
    public String toString() {
        return StringUtil.toTitleCase(super.toString());
    }
}
