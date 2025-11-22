package com.barangay.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain Entity: BarangayInfo
 * Represents barangay configuration and information.
 * Singleton-like entity (only one record should exist).
 */
public class BarangayInfo {
    private String id;
    private String barangayName;
    private String city;
    private String province;
    private String region;
    private String address;
    private String contactNumber;
    private String email;
    private String sealPath;
    private LocalDateTime updatedAt;
    private final List<String> dashboardImages;

    public BarangayInfo(String id) {
        this(id, null, null, null, null, null, null, null, null, LocalDateTime.now(), null);
    }

    public BarangayInfo(String id, String barangayName, String city, String province,
            String region, String address, String contactNumber, String email,
            String sealPath, LocalDateTime updatedAt) {
        this(id, barangayName, city, province, region, address, contactNumber, email, sealPath,
                updatedAt, null);
    }

    public BarangayInfo(String id, String barangayName, String city, String province,
            String region, String address, String contactNumber, String email,
            String sealPath, LocalDateTime updatedAt, List<String> dashboardImages) {
        this.id = id;
        this.barangayName = barangayName;
        this.city = city;
        this.province = province;
        this.region = region;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.sealPath = sealPath;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.dashboardImages = new ArrayList<>();
        replaceDashboardImages(dashboardImages, false);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getBarangayName() {
        return barangayName;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getRegion() {
        return region;
    }

    public String getAddress() {
        return address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getSealPath() {
        return sealPath;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<String> getDashboardImages() {
        return Collections.unmodifiableList(dashboardImages);
    }

    // Setters
    public void setBarangayName(String barangayName) {
        this.barangayName = barangayName;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCity(String city) {
        this.city = city;
        this.updatedAt = LocalDateTime.now();
    }

    public void setProvince(String province) {
        this.province = province;
        this.updatedAt = LocalDateTime.now();
    }

    public void setRegion(String region) {
        this.region = region;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAddress(String address) {
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSealPath(String sealPath) {
        this.sealPath = sealPath;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDashboardImages(List<String> images) {
        replaceDashboardImages(images, true);
    }

    private void replaceDashboardImages(List<String> images, boolean updateTimestamp) {
        dashboardImages.clear();
        if (images != null) {
            images.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .forEach(dashboardImages::add);
        }
        if (updateTimestamp) {
            this.updatedAt = LocalDateTime.now();
        }
    }
}
