package com.barangay.application.usecases;

import com.barangay.application.dto.UpdateBarangayInfoInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.BarangayInfo;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IBarangayInfoRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Use Case: Update barangay information.
 */
public class UpdateBarangayInfoUseCase {

    private static final String DEFAULT_INFO_ID = "BRGY_INFO";

    private final IBarangayInfoRepository barangayInfoRepository;
    private final SessionManager sessionManager;

    public UpdateBarangayInfoUseCase(IBarangayInfoRepository barangayInfoRepository, SessionManager sessionManager) {
        this.barangayInfoRepository = barangayInfoRepository;
        this.sessionManager = sessionManager;
    }

    public BarangayInfo execute(UpdateBarangayInfoInputDto input) {
        if (input == null) {
            throw new IllegalArgumentException("Update input cannot be null");
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("You must be logged in to update barangay information");
        }

        UserRole role = currentUser.getRole();
        if (role != UserRole.SUPER_ADMIN && role != UserRole.ADMIN) {
            throw new UnauthorizedOperationException("Only administrators can update barangay information");
        }

        BarangayInfo info = barangayInfoRepository.get()
                .orElseGet(() -> new BarangayInfo(DEFAULT_INFO_ID));

        applyInput(info, input);
        barangayInfoRepository.save(info);
        return info;
    }

    private void applyInput(BarangayInfo info, UpdateBarangayInfoInputDto input) {
        info.setBarangayName(normalize(input.getBarangayName()));
        info.setCity(normalize(input.getCity()));
        info.setProvince(normalize(input.getProvince()));
        info.setRegion(normalize(input.getRegion()));
        info.setAddress(normalize(input.getAddress()));
        info.setContactNumber(normalize(input.getContactNumber()));
        info.setEmail(normalize(input.getEmail()));
        info.setSealPath(normalize(input.getSealPath()));
        info.setDashboardImages(normalizeImages(input.getDashboardImages()));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<String> normalizeImages(List<String> images) {
        if (images == null) {
            return Collections.emptyList();
        }
        return images.stream()
                .map(this::normalize)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
