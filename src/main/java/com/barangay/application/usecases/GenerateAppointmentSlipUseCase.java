package com.barangay.application.usecases;

import com.barangay.application.dto.AppointmentSlipOutputDto;
import com.barangay.application.services.AppointmentSlipGenerator;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.BarangayInfo;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.entities.VoterApplication;
import com.barangay.domain.entities.ApplicationStatus;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IBarangayInfoRepository;
import com.barangay.domain.repositories.IResidentRepository;
import com.barangay.domain.repositories.IVoterApplicationRepository;

import java.util.Arrays;
import java.util.List;

/**
 * Use case for generating voter application appointment slips.
 */
public class GenerateAppointmentSlipUseCase {
    private final IVoterApplicationRepository applicationRepository;
    private final IResidentRepository residentRepository;
    private final IBarangayInfoRepository barangayInfoRepository;
    private final SessionManager sessionManager;
    private final AppointmentSlipGenerator slipGenerator;

    public GenerateAppointmentSlipUseCase(IVoterApplicationRepository applicationRepository,
            IResidentRepository residentRepository, IBarangayInfoRepository barangayInfoRepository,
            SessionManager sessionManager, AppointmentSlipGenerator slipGenerator) {
        this.applicationRepository = applicationRepository;
        this.residentRepository = residentRepository;
        this.barangayInfoRepository = barangayInfoRepository;
        this.sessionManager = sessionManager;
        this.slipGenerator = slipGenerator;
    }

    public AppointmentSlipOutputDto execute(String applicationId) {
        if (applicationId == null || applicationId.isBlank()) {
            throw new IllegalArgumentException("Application ID is required");
        }
        User currentUser = requireLoggedInUser();

        VoterApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        ensureAccess(currentUser, application);
        ensureSchedulable(application);

        Resident resident = residentRepository.findById(application.getResidentId())
                .orElseThrow(() -> new IllegalStateException("Resident record not found"));

        BarangayInfo barangayInfo = barangayInfoRepository.get().orElse(null);

        AppointmentSlipGenerator.AppointmentSlipData data = new AppointmentSlipGenerator.AppointmentSlipData(
                barangayInfo != null ? barangayInfo.getBarangayName() : "Barangay Management Office",
                buildBarangayAddress(barangayInfo),
                barangayInfo != null ? barangayInfo.getContactNumber() : null,
                barangayInfo != null ? barangayInfo.getEmail() : null,
                application.getId(),
                application.getApplicationType().name().replace('_', ' '),
                application.getResidentId().getValue(),
                resident.getFullName(),
                resident.getContact(),
                resident.getAddress() != null ? resident.getAddress().getFullAddress() : null,
                application.getAppointmentDateTime(),
                application.getAppointmentVenue(),
                application.getAppointmentSlipReference(),
                defaultReminders());

        byte[] pdf = slipGenerator.generate(data);
        String suggestedFileName = "AppointmentSlip_" + application.getId() + ".pdf";
        return new AppointmentSlipOutputDto(suggestedFileName, pdf);
    }

    private User requireLoggedInUser() {
        User user = sessionManager.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedOperationException("Please sign in to continue");
        }
        return user;
    }

    private void ensureAccess(User user, VoterApplication application) {
        UserRole role = user.getRole();
        if (role == UserRole.RESIDENT) {
            if (user.getLinkedResidentId() == null) {
                throw new UnauthorizedOperationException("Your account is not linked to a resident profile");
            }
            if (!user.getLinkedResidentId().equals(application.getResidentId())) {
                throw new UnauthorizedOperationException("You can only download slips for your own application");
            }
            return;
        }
        if (role != UserRole.CLERK && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedOperationException("You are not authorized to download appointment slips");
        }
    }

    private void ensureSchedulable(VoterApplication application) {
        if (application.getStatus() != ApplicationStatus.SCHEDULED) {
            throw new IllegalStateException("Appointment slips are only available for scheduled applications");
        }
        if (application.getAppointmentDateTime() == null || application.getAppointmentVenue() == null
                || application.getAppointmentSlipReference() == null) {
            throw new IllegalStateException("Application is missing appointment schedule details");
        }
    }

    private String buildBarangayAddress(BarangayInfo info) {
        if (info == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        appendSegment(builder, info.getAddress());
        appendSegment(builder, info.getCity());
        appendSegment(builder, info.getProvince());
        appendSegment(builder, info.getRegion());
        return builder.toString();
    }

    private void appendSegment(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(value.trim());
    }

    private List<String> defaultReminders() {
        return Arrays.asList(
                "Bring at least one (1) valid government-issued ID.",
                "Arrive at least 15 minutes before the scheduled time.",
                "Present this slip to the verification officer upon arrival.");
    }
}
