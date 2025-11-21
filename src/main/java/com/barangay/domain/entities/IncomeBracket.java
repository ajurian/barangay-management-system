package com.barangay.domain.entities;

public enum IncomeBracket {
    BELOW_10K,
    TEN_TO_20K,
    TWENTY_TO_40K,
    FORTY_TO_70K,
    SEVENTY_TO_110K,
    ABOVE_110K;

    @Override
    public String toString() {
        switch (this) {
            case BELOW_10K:
                return "Below ₱10,000";
            case TEN_TO_20K:
                return "₱10,000 - ₱20,000";
            case TWENTY_TO_40K:
                return "₱20,000 - ₱40,000";
            case FORTY_TO_70K:
                return "₱40,000 - ₱70,000";
            case SEVENTY_TO_110K:
                return "₱70,000 - ₱110,000";
            case ABOVE_110K:
                return "Above ₱110,000";
            default:
                return "";
        }
    }
}
