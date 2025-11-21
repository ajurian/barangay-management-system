package com.barangay.application.usecases;

import com.barangay.application.dto.RegisterResidentInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.ResidentNotFoundException;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IResidentRepository;
import com.barangay.domain.valueobjects.Address;

/**
 * Use Case: Update Resident Information
 */
public class UpdateResidentUseCase {
    private final IResidentRepository residentRepository;
    private final SessionManager sessionManager;

    public UpdateResidentUseCase(IResidentRepository residentRepository, SessionManager sessionManager) {
        this.residentRepository = residentRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(String residentIdStr, RegisterResidentInputDto input) {
        // Check authorization
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        UserRole role = currentUser.getRole();
        if (role != UserRole.CLERK && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedOperationException("You are not authorized to update residents");
        }

        // Find resident
        ResidentId residentId = ResidentId.fromString(residentIdStr);
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResidentNotFoundException("Resident not found"));

        // Update fields
        resident.setFirstName(input.getFirstName());
        resident.setMiddleName(input.getMiddleName());
        resident.setLastName(input.getLastName());
        resident.setSuffix(input.getSuffix());
        resident.setBirthDate(input.getBirthDate());
        resident.setBirthPlace(input.getBirthPlace());
        resident.setGender(input.getGender());
        resident.setCivilStatus(input.getCivilStatus());
        resident.setNationality(input.getNationality());
        resident.setContact(input.getContact());
        resident.setOccupation(input.getOccupation());
        resident.setEmployment(input.getEmployment());
        resident.setIncomeBracket(input.getIncomeBracket());
        resident.setEducationLevel(input.getEducationLevel());

        // Update address
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

        // Save changes
        residentRepository.save(resident);
    }
}
