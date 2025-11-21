package com.barangay.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object: Address
 * Immutable address representation.
 */
public class Address {
    private final String houseNumber;
    private final String street;
    private final String purok; // Zone/Purok
    private final String barangay;
    private final String city;
    private final String province;

    public Address(String houseNumber, String street, String purok,
            String barangay, String city, String province) {
        this.houseNumber = houseNumber;
        this.street = street;
        this.purok = purok;
        this.barangay = barangay;
        this.city = city;
        this.province = province;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getStreet() {
        return street;
    }

    public String getPurok() {
        return purok;
    }

    public String getBarangay() {
        return barangay;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (houseNumber != null && !houseNumber.isEmpty()) {
            sb.append(houseNumber).append(" ");
        }
        if (street != null && !street.isEmpty()) {
            sb.append(street).append(", ");
        }
        if (purok != null && !purok.isEmpty()) {
            sb.append("Purok ").append(purok).append(", ");
        }
        if (barangay != null && !barangay.isEmpty()) {
            sb.append("Barangay ").append(barangay).append(", ");
        }
        if (city != null && !city.isEmpty()) {
            sb.append(city).append(", ");
        }
        if (province != null && !province.isEmpty()) {
            sb.append(province);
        }
        return sb.toString().trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Address address = (Address) o;
        return Objects.equals(houseNumber, address.houseNumber) &&
                Objects.equals(street, address.street) &&
                Objects.equals(purok, address.purok) &&
                Objects.equals(barangay, address.barangay) &&
                Objects.equals(city, address.city) &&
                Objects.equals(province, address.province);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseNumber, street, purok, barangay, city, province);
    }

    @Override
    public String toString() {
        return getFullAddress();
    }
}
