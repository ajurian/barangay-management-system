package com.barangay.domain.entities;

import java.util.Objects;

/**
 * Value Object: ResidentId
 * Format: BR-YYYY-XXXXXXXXXX
 * Immutable identifier for Resident entities.
 */
public class ResidentId {
    private final String value;

    private ResidentId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("ResidentId value cannot be null or empty");
        }
        if (!value.matches("BR-\\d{4}-\\d{10}")) {
            throw new IllegalArgumentException("ResidentId must follow format BR-YYYY-XXXXXXXXXX");
        }
        this.value = value;
    }

    public static ResidentId fromString(String value) {
        return new ResidentId(value);
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
        ResidentId that = (ResidentId) o;
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
