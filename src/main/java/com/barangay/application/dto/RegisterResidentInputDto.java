package com.barangay.application.dto;

import com.barangay.domain.entities.*;
import java.time.LocalDate;

/**
 * DTO for registering a new resident
 */
public class RegisterResidentInputDto {
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String suffix;
    private final LocalDate birthDate;
    private final String birthPlace;
    private final Gender gender;
    private final CivilStatus civilStatus;
    private final String nationality;
    private final String contact;
    private final String houseNumber;
    private final String street;
    private final String purok;
    private final String barangay;
    private final String city;
    private final String province;
    private final String occupation;
    private final String employment;
    private final IncomeBracket incomeBracket;
    private final EducationLevel educationLevel;

    public RegisterResidentInputDto(String firstName, String middleName, String lastName,
            String suffix, LocalDate birthDate, String birthPlace,
            Gender gender, CivilStatus civilStatus, String nationality,
            String contact, String houseNumber, String street,
            String purok, String barangay, String city, String province,
            String occupation, String employment,
            IncomeBracket incomeBracket, EducationLevel educationLevel) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.suffix = suffix;
        this.birthDate = birthDate;
        this.birthPlace = birthPlace;
        this.gender = gender;
        this.civilStatus = civilStatus;
        this.nationality = nationality;
        this.contact = contact;
        this.houseNumber = houseNumber;
        this.street = street;
        this.purok = purok;
        this.barangay = barangay;
        this.city = city;
        this.province = province;
        this.occupation = occupation;
        this.employment = employment;
        this.incomeBracket = incomeBracket;
        this.educationLevel = educationLevel;
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSuffix() {
        return suffix;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public Gender getGender() {
        return gender;
    }

    public CivilStatus getCivilStatus() {
        return civilStatus;
    }

    public String getNationality() {
        return nationality;
    }

    public String getContact() {
        return contact;
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

    public String getOccupation() {
        return occupation;
    }

    public String getEmployment() {
        return employment;
    }

    public IncomeBracket getIncomeBracket() {
        return incomeBracket;
    }

    public EducationLevel getEducationLevel() {
        return educationLevel;
    }
}
