package com.barangay.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object: DocumentReference
 * Immutable document reference number.
 * Format: BID-YYYY-XXXXXXXXXX, BC-YYYY-XXXXXXXXXX, CR-YYYY-XXXXXXXXXX
 */
public class DocumentReference {
    private final String value;

    private DocumentReference(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("DocumentReference cannot be null or empty");
        }
        if (!value.matches("(BID|BC|CR)-\\d{4}-\\d{10}")) {
            throw new IllegalArgumentException("DocumentReference must follow format PREFIX-YYYY-XXXXXXXXXX");
        }
        this.value = value;
    }

    public static DocumentReference fromString(String value) {
        return new DocumentReference(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DocumentReference that = (DocumentReference) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
