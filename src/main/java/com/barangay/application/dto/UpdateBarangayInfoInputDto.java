package com.barangay.application.dto;

/**
 * DTO for updating barangay information.
 */
public class UpdateBarangayInfoInputDto {
    private final String barangayName;
    private final String city;
    private final String province;
    private final String region;
    private final String address;
    private final String contactNumber;
    private final String email;
    private final String sealPath;

    public UpdateBarangayInfoInputDto(String barangayName, String city, String province,
            String region, String address, String contactNumber, String email, String sealPath) {
        this.barangayName = barangayName;
        this.city = city;
        this.province = province;
        this.region = region;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.sealPath = sealPath;
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
}
