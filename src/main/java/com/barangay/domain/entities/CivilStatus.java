package com.barangay.domain.entities;

import com.barangay.presentation.util.StringUtil;

public enum CivilStatus {
    SINGLE,
    MARRIED,
    WIDOWED,
    SEPARATED,
    DIVORCED;

    @Override
    public String toString() {
        return StringUtil.toTitleCase(super.toString());
    }
}
