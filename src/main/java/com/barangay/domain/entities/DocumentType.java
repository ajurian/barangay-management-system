package com.barangay.domain.entities;

public enum DocumentType {
    BARANGAY_ID,
    BARANGAY_CLEARANCE,
    CERTIFICATE_OF_RESIDENCY;

    @Override
    public String toString() {
        switch (this) {
            case BARANGAY_ID:
                return "Barangay ID";
            case BARANGAY_CLEARANCE:
                return "Barangay Clearance";
            case CERTIFICATE_OF_RESIDENCY:
                return "Certificate of Residency";
            default:
                return super.toString();
        }
    }
}
