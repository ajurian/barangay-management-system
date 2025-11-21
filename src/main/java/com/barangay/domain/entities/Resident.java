package com.barangay.domain.entities;

import com.barangay.domain.valueobjects.Address;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain Entity: Resident
 * Represents a barangay resident with core demographic information.
 * Following SRP: Handles only resident entity data and basic validation.
 */
public class Resident {
    private final ResidentId id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix; // Jr., Sr., III, etc.
    private LocalDate birthDate;
    private String birthPlace;
    private Gender gender;
    private CivilStatus civilStatus;
    private String nationality;
    private String contact;
    private Address address;
    private String occupation;
    private String employment;
    private IncomeBracket incomeBracket; // Optional
    private EducationLevel educationLevel; // Optional
    private boolean isVoter;
    private boolean isActive;
    private String deactivationReason;
    private final LocalDateTime registeredAt;
    private LocalDateTime updatedAt;

    public Resident(ResidentId id, String firstName, String lastName,
            LocalDate birthDate, Gender gender) {
        if (id == null || firstName == null || lastName == null ||
                birthDate == null || gender == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.isActive = true;
        this.isVoter = false;
        this.registeredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public ResidentId getId() {
        return id;
    }

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

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        fullName.append(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ").append(middleName);
        }
        fullName.append(" ").append(lastName);
        if (suffix != null && !suffix.isEmpty()) {
            fullName.append(" ").append(suffix);
        }
        return fullName.toString();
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

    public Address getAddress() {
        return address;
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

    public boolean isVoter() {
        return isVoter;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getDeactivationReason() {
        return deactivationReason;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters with business rules
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        this.firstName = firstName;
        this.updatedAt = LocalDateTime.now();
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
        this.updatedAt = LocalDateTime.now();
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
        this.updatedAt = LocalDateTime.now();
    }

    public void setBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date cannot be null");
        }
        this.birthDate = birthDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
        this.updatedAt = LocalDateTime.now();
    }

    public void setGender(Gender gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        this.gender = gender;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCivilStatus(CivilStatus civilStatus) {
        this.civilStatus = civilStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
        this.updatedAt = LocalDateTime.now();
    }

    public void setContact(String contact) {
        this.contact = contact;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAddress(Address address) {
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEmployment(String employment) {
        this.employment = employment;
        this.updatedAt = LocalDateTime.now();
    }

    public void setIncomeBracket(IncomeBracket incomeBracket) {
        this.incomeBracket = incomeBracket;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEducationLevel(EducationLevel educationLevel) {
        this.educationLevel = educationLevel;
        this.updatedAt = LocalDateTime.now();
    }

    public void setVoter(boolean voter) {
        isVoter = voter;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate(String reason) {
        this.isActive = false;
        this.deactivationReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.isActive = true;
        this.deactivationReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate age based on birth date
     */
    public int getAge() {
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    /**
     * Business rule: Check for potential duplicate
     */
    public boolean isPotentialDuplicateOf(Resident other) {
        return this.firstName.equalsIgnoreCase(other.firstName) &&
                this.lastName.equalsIgnoreCase(other.lastName) &&
                this.birthDate.equals(other.birthDate);
    }
}
