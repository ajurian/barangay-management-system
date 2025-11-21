package com.barangay.domain.entities;

import com.barangay.presentation.util.StringUtil;

public enum OfficialPosition {
    CAPTAIN,
    KAGAWAD,
    SK_CHAIRMAN,
    SECRETARY,
    TREASURER;

    @Override
    public String toString() {
        if (this.equals(SK_CHAIRMAN)) {
            return "SK Chairman";
        }

        return StringUtil.toTitleCase(super.toString());
    }
}
