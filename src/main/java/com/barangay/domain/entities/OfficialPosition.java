package com.barangay.domain.entities;

import com.barangay.presentation.util.StringUtil;

public enum OfficialPosition {
    CAPTAIN(1),
    KAGAWAD(7),
    SK_CHAIRMAN(1),
    SECRETARY(1),
    TREASURER(1);

    private final int maxAllowed;

    OfficialPosition(int maxAllowed) {
        this.maxAllowed = maxAllowed;
    }

    /**
     * Gets the maximum number of officials allowed for this position.
     * For example, KAGAWAD allows 7 officials, while other positions allow only 1.
     */
    public int getMaxAllowed() {
        return maxAllowed;
    }

    @Override
    public String toString() {
        if (this.equals(SK_CHAIRMAN)) {
            return "SK Chairman";
        }

        return StringUtil.toTitleCase(super.toString());
    }
}
