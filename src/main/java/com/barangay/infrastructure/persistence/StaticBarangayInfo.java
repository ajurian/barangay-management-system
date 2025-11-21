package com.barangay.infrastructure.persistence;

import com.barangay.domain.entities.BarangayInfo;

import java.time.LocalDateTime;

/**
 * Central place for defining barangay information constants.
 * Edit these values and rebuild the application whenever updates are needed.
 */
public final class StaticBarangayInfo {
    private static final String ID = "BRGY_INFO";
    private static final String BARANGAY_NAME = "Aurelia Heights";
    private static final String CITY = "Antipolo City";
    private static final String PROVINCE = "Rizal";
    private static final String REGION = "Region IV-A";
    private static final String ADDRESS = "123 Sample Street, Barangay Aurelia Heights, Antipolo City";
    private static final String CONTACT_NUMBER = "+63 900 000 0000";
    private static final String EMAIL = "barangay.aureliaheights@gmail.com";
    private static final String SEAL_PATH = "/images/barangay_seal_old.png";

    private StaticBarangayInfo() {
    }

    /**
     * Returns a fresh {@link BarangayInfo} instance populated with the constant
     * values.
     * The copy ensures callers cannot mutate the static state accidentally.
     */
    public static BarangayInfo getInfo() {
        return new BarangayInfo(
                ID,
                BARANGAY_NAME,
                CITY,
                PROVINCE,
                REGION,
                ADDRESS,
                CONTACT_NUMBER,
                EMAIL,
                SEAL_PATH,
                LocalDateTime.now());
    }
}
