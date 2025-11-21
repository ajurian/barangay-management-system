package com.barangay.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: UserId
 * Immutable identifier for User entities.
 * Following Value Object pattern: equality based on value, immutable.
 */
public class UserId {
    private final String value;

    private UserId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("UserId value cannot be null or empty");
        }
        this.value = value;
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId fromString(String value) {
        return new UserId(value);
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
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
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
