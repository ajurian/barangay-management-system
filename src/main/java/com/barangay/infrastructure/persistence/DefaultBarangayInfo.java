package com.barangay.infrastructure.persistence;

import com.barangay.domain.entities.BarangayInfo;

import java.time.LocalDateTime;
import java.util.Collections;

public final class DefaultBarangayInfo {
    private static final String ID = "BRGY_INFO";
    private static final String BARANGAY_NAME = "Aurelia Heights";
    private static final String CITY = "Antipolo City";
    private static final String PROVINCE = "Rizal";
    private static final String REGION = "Region IV-A";
    private static final String ADDRESS = "7 Green Hills, Barangay Aurelia Heights, Antipolo City";
    private static final String CONTACT_NUMBER = "+63 900 000 0000";
    private static final String EMAIL = "barangay.aureliaheights@gmail.com";
    private static final String SEAL_PATH = "";

    private DefaultBarangayInfo() {
    }

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
            LocalDateTime.now(),
            Collections.emptyList());
    }
}
