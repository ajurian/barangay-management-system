package com.barangay.application.usecases;

import com.barangay.application.dto.RegisterResidentInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.DuplicateResidentException;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IResidentRepository;
import com.barangay.domain.valueobjects.Address;

import java.util.List;

/**
 * Use Case: Register New Resident
 * Following SRP: Handles only resident registration logic.
 */
public class RegisterResidentUseCase {
    private final IResidentRepository residentRepository;
    private final SessionManager sessionManager;

    public RegisterResidentUseCase(IResidentRepository residentRepository, SessionManager sessionManager) {
        this.residentRepository = residentRepository;
        this.sessionManager = sessionManager;
    }

    public String execute(RegisterResidentInputDto input) {
        // Check authorization
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        // Only CLERK, ADMIN, and SUPER_ADMIN can register residents
        UserRole role = currentUser.getRole();
        if (role != UserRole.CLERK && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedOperationException("You are not authorized to register residents");
        }

        // Validate required fields
        if (input.getFirstName() == null || input.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (input.getLastName() == null || input.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (input.getBirthDate() == null) {
            throw new IllegalArgumentException("Birth date is required");
        }
        if (input.getGender() == null) {
            throw new IllegalArgumentException("Gender is required");
        }

        // Check for potential duplicates
        List<Resident> potentialDuplicates = residentRepository.findPotentialDuplicates(
                input.getFirstName(),
                input.getLastName(),
                input.getBirthDate());

        if (!potentialDuplicates.isEmpty()) {
            throw new DuplicateResidentException(
                    "A resident with the same name and birth date already exists");
        }

        // Generate resident ID
        ResidentId residentId = residentRepository.generateNextId();

        // Create resident entity
        Resident resident = new Resident(
                residentId,
                input.getFirstName(),
                input.getLastName(),
                input.getBirthDate(),
                input.getGender());

        // Set optional fields
        resident.setMiddleName(input.getMiddleName());
        resident.setSuffix(input.getSuffix());
        resident.setBirthPlace(input.getBirthPlace());
        resident.setCivilStatus(input.getCivilStatus());
        resident.setNationality(input.getNationality());
        resident.setContact(input.getContact());
        resident.setOccupation(input.getOccupation());
        resident.setEmployment(input.getEmployment());
        resident.setIncomeBracket(input.getIncomeBracket());
        resident.setEducationLevel(input.getEducationLevel());

        // Set address
        if (input.getBarangay() != null || input.getCity() != null || input.getProvince() != null) {
            Address address = new Address(
                    input.getHouseNumber(),
                    input.getStreet(),
                    input.getPurok(),
                    input.getBarangay(),
                    input.getCity(),
                    input.getProvince());
            resident.setAddress(address);
        }

        // Save resident
        residentRepository.save(resident);

        return residentId.getValue();
    }
}
