package com.barangay.infrastructure.config;

import com.barangay.application.ports.IPasswordHasher;
import com.barangay.application.services.AppointmentSlipGenerator;
import com.barangay.application.services.PasswordValidator;
import com.barangay.application.services.SessionManager;
import com.barangay.application.usecases.*;
import com.barangay.domain.repositories.*;
import com.barangay.infrastructure.persistence.*;
import com.barangay.infrastructure.security.BCryptPasswordHasher;

/**
 * Dependency Injection Container
 * Following DIP: Wires up dependencies manually (alternative to using a DI
 * framework)
 * This is where concrete implementations are instantiated and injected.
 */
public class DIContainer {
    // Repositories (Infrastructure layer)
    private final IUserRepository userRepository;
    private final IResidentRepository residentRepository;
    private final IDocumentRepository documentRepository;
    private final IDocumentRequestRepository documentRequestRepository;
    private final IVoterApplicationRepository voterApplicationRepository;
    private final IOfficialRepository officialRepository;
    private final IBarangayInfoRepository barangayInfoRepository;

    // Services (Application layer)
    private final IPasswordHasher passwordHasher;
    private final PasswordValidator passwordValidator;
    private final SessionManager sessionManager;
    private final AppointmentSlipGenerator appointmentSlipGenerator;

    // Use Cases (Application layer)
    private final SystemSetupUseCase systemSetupUseCase;
    private final LoginUseCase loginUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final ReactivateUserUseCase reactivateUserUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final RegisterResidentUseCase registerResidentUseCase;
    private final SearchResidentsUseCase searchResidentsUseCase;
    private final UpdateResidentUseCase updateResidentUseCase;
    private final DeactivateResidentUseCase deactivateResidentUseCase;
    private final ReactivateResidentUseCase reactivateResidentUseCase;
    private final IssueDocumentUseCase issueDocumentUseCase;
    private final SearchDocumentsUseCase searchDocumentsUseCase;
    private final SubmitVoterApplicationUseCase submitVoterApplicationUseCase;
    private final ReviewVoterApplicationUseCase reviewVoterApplicationUseCase;
    private final ScheduleVerificationUseCase scheduleVerificationUseCase;
    private final VerifyVoterApplicationUseCase verifyVoterApplicationUseCase;
    private final GenerateAppointmentSlipUseCase generateAppointmentSlipUseCase;
    private final SubmitDocumentRequestUseCase submitDocumentRequestUseCase;
    private final ListDocumentRequestsUseCase listDocumentRequestsUseCase;
    private final UpdateDocumentRequestStatusUseCase updateDocumentRequestStatusUseCase;
    private final GetDocumentRequestCountsUseCase documentRequestCountsUseCase;
    private final GetDocumentRequestUseCase documentRequestUseCase;

    // Module 6: Barangay Officials Management
    private final RegisterOfficialUseCase registerOfficialUseCase;
    private final UpdateOfficialUseCase updateOfficialUseCase;
    private final EndTermUseCase endTermUseCase;
    private final ListOfficialsUseCase listOfficialsUseCase;

    // Module 7: Reports & Analytics
    private final GetResidentStatisticsUseCase getResidentStatisticsUseCase;
    private final GetDocumentStatisticsUseCase getDocumentStatisticsUseCase;

    // Module 8: System Administration
    private final GetBarangayInfoUseCase getBarangayInfoUseCase;
    private final UpdateBarangayInfoUseCase updateBarangayInfoUseCase;

    // Module 10: Profile Management
    private final UpdateProfileUseCase updateProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    public DIContainer() {
        // Initialize database
        DatabaseConnection.initializeDatabase();

        // Instantiate repositories
        this.userRepository = new UserRepository();
        this.residentRepository = new ResidentRepository();
        this.documentRepository = new DocumentRepository();
        this.documentRequestRepository = new DocumentRequestRepository();
        this.voterApplicationRepository = new VoterApplicationRepository();
        this.officialRepository = new OfficialRepository();
        this.barangayInfoRepository = new BarangayInfoRepository();

        // Instantiate services
        this.passwordHasher = new BCryptPasswordHasher();
        this.passwordValidator = new PasswordValidator();
        this.sessionManager = SessionManager.getInstance();
        this.appointmentSlipGenerator = new AppointmentSlipGenerator();

        // Instantiate use cases with dependency injection
        this.systemSetupUseCase = new SystemSetupUseCase(
                userRepository, passwordHasher, passwordValidator);

        this.loginUseCase = new LoginUseCase(userRepository, passwordHasher);

        this.createUserUseCase = new CreateUserUseCase(
                userRepository, residentRepository, passwordHasher, passwordValidator, sessionManager);

        this.deactivateUserUseCase = new DeactivateUserUseCase(userRepository, sessionManager);
        this.reactivateUserUseCase = new ReactivateUserUseCase(userRepository, sessionManager);
        this.changeUserRoleUseCase = new ChangeUserRoleUseCase(userRepository, sessionManager);

        this.resetPasswordUseCase = new ResetPasswordUseCase(
                userRepository, passwordHasher, passwordValidator, sessionManager);

        this.listUsersUseCase = new ListUsersUseCase(userRepository);

        this.registerResidentUseCase = new RegisterResidentUseCase(
                residentRepository, sessionManager);

        this.searchResidentsUseCase = new SearchResidentsUseCase(residentRepository);

        this.updateResidentUseCase = new UpdateResidentUseCase(
                residentRepository, sessionManager);

        this.deactivateResidentUseCase = new DeactivateResidentUseCase(
                residentRepository, sessionManager);

        this.reactivateResidentUseCase = new ReactivateResidentUseCase(
                residentRepository, sessionManager);

        this.issueDocumentUseCase = new IssueDocumentUseCase(
                documentRepository, residentRepository, sessionManager, documentRequestRepository);

        this.searchDocumentsUseCase = new SearchDocumentsUseCase(documentRepository);

        this.submitVoterApplicationUseCase = new SubmitVoterApplicationUseCase(
                voterApplicationRepository, residentRepository, sessionManager);

        this.reviewVoterApplicationUseCase = new ReviewVoterApplicationUseCase(
                voterApplicationRepository, sessionManager);

        this.scheduleVerificationUseCase = new ScheduleVerificationUseCase(
                voterApplicationRepository, sessionManager);

        this.verifyVoterApplicationUseCase = new VerifyVoterApplicationUseCase(
                voterApplicationRepository, residentRepository, sessionManager);

        this.generateAppointmentSlipUseCase = new GenerateAppointmentSlipUseCase(
                voterApplicationRepository, residentRepository, barangayInfoRepository,
                sessionManager, appointmentSlipGenerator);

        this.submitDocumentRequestUseCase = new SubmitDocumentRequestUseCase(
                documentRequestRepository, residentRepository, sessionManager);

        this.listDocumentRequestsUseCase = new ListDocumentRequestsUseCase(
                documentRequestRepository, sessionManager);

        this.updateDocumentRequestStatusUseCase = new UpdateDocumentRequestStatusUseCase(
                documentRequestRepository, sessionManager);

        this.documentRequestCountsUseCase = new GetDocumentRequestCountsUseCase(
                documentRequestRepository, sessionManager);

        this.documentRequestUseCase = new GetDocumentRequestUseCase(
                documentRequestRepository, sessionManager);

        // Module 6: Barangay Officials Management
        this.registerOfficialUseCase = new RegisterOfficialUseCase(
                officialRepository, residentRepository, sessionManager);

        this.updateOfficialUseCase = new UpdateOfficialUseCase(
                officialRepository, sessionManager);

        this.endTermUseCase = new EndTermUseCase(
                officialRepository, sessionManager);

        this.listOfficialsUseCase = new ListOfficialsUseCase(officialRepository);

        // Module 7: Reports & Analytics
        this.getResidentStatisticsUseCase = new GetResidentStatisticsUseCase(residentRepository);
        this.getDocumentStatisticsUseCase = new GetDocumentStatisticsUseCase(documentRepository);

        // Module 8: System Administration
        this.getBarangayInfoUseCase = new GetBarangayInfoUseCase(barangayInfoRepository);
        this.updateBarangayInfoUseCase = new UpdateBarangayInfoUseCase(barangayInfoRepository, sessionManager);

        // Module 10: Profile Management
        this.updateProfileUseCase = new UpdateProfileUseCase(userRepository, residentRepository, sessionManager);
        this.changePasswordUseCase = new ChangePasswordUseCase(
                userRepository, passwordHasher, passwordValidator, sessionManager);
    }

    // Getters for use cases
    public SystemSetupUseCase getSystemSetupUseCase() {
        return systemSetupUseCase;
    }

    public LoginUseCase getLoginUseCase() {
        return loginUseCase;
    }

    public CreateUserUseCase getCreateUserUseCase() {
        return createUserUseCase;
    }

    public DeactivateUserUseCase getDeactivateUserUseCase() {
        return deactivateUserUseCase;
    }

    public ReactivateUserUseCase getReactivateUserUseCase() {
        return reactivateUserUseCase;
    }

    public ChangeUserRoleUseCase getChangeUserRoleUseCase() {
        return changeUserRoleUseCase;
    }

    public ResetPasswordUseCase getResetPasswordUseCase() {
        return resetPasswordUseCase;
    }

    public ListUsersUseCase getListUsersUseCase() {
        return listUsersUseCase;
    }

    public RegisterResidentUseCase getRegisterResidentUseCase() {
        return registerResidentUseCase;
    }

    public SearchResidentsUseCase getSearchResidentsUseCase() {
        return searchResidentsUseCase;
    }

    public UpdateResidentUseCase getUpdateResidentUseCase() {
        return updateResidentUseCase;
    }

    public DeactivateResidentUseCase getDeactivateResidentUseCase() {
        return deactivateResidentUseCase;
    }

    public ReactivateResidentUseCase getReactivateResidentUseCase() {
        return reactivateResidentUseCase;
    }

    public IssueDocumentUseCase getIssueDocumentUseCase() {
        return issueDocumentUseCase;
    }

    public SearchDocumentsUseCase getSearchDocumentsUseCase() {
        return searchDocumentsUseCase;
    }

    public SubmitVoterApplicationUseCase getSubmitVoterApplicationUseCase() {
        return submitVoterApplicationUseCase;
    }

    public ReviewVoterApplicationUseCase getReviewVoterApplicationUseCase() {
        return reviewVoterApplicationUseCase;
    }

    public ScheduleVerificationUseCase getScheduleVerificationUseCase() {
        return scheduleVerificationUseCase;
    }

    public VerifyVoterApplicationUseCase getVerifyVoterApplicationUseCase() {
        return verifyVoterApplicationUseCase;
    }

    public GenerateAppointmentSlipUseCase getGenerateAppointmentSlipUseCase() {
        return generateAppointmentSlipUseCase;
    }

    public SubmitDocumentRequestUseCase getSubmitDocumentRequestUseCase() {
        return submitDocumentRequestUseCase;
    }

    public ListDocumentRequestsUseCase getListDocumentRequestsUseCase() {
        return listDocumentRequestsUseCase;
    }

    public UpdateDocumentRequestStatusUseCase getUpdateDocumentRequestStatusUseCase() {
        return updateDocumentRequestStatusUseCase;
    }

    public GetDocumentRequestCountsUseCase getDocumentRequestCountsUseCase() {
        return documentRequestCountsUseCase;
    }

    public GetDocumentRequestUseCase getDocumentRequestUseCase() {
        return documentRequestUseCase;
    }

    // Module 6: Barangay Officials Management
    public RegisterOfficialUseCase getRegisterOfficialUseCase() {
        return registerOfficialUseCase;
    }

    public UpdateOfficialUseCase getUpdateOfficialUseCase() {
        return updateOfficialUseCase;
    }

    public EndTermUseCase getEndTermUseCase() {
        return endTermUseCase;
    }

    public ListOfficialsUseCase getListOfficialsUseCase() {
        return listOfficialsUseCase;
    }

    // Module 7: Reports & Analytics
    public GetResidentStatisticsUseCase getResidentStatisticsUseCase() {
        return getResidentStatisticsUseCase;
    }

    public GetDocumentStatisticsUseCase getDocumentStatisticsUseCase() {
        return getDocumentStatisticsUseCase;
    }

    // Module 8: System Administration
    public GetBarangayInfoUseCase getGetBarangayInfoUseCase() {
        return getBarangayInfoUseCase;
    }

    public UpdateBarangayInfoUseCase getUpdateBarangayInfoUseCase() {
        return updateBarangayInfoUseCase;
    }

    // Module 10: Profile Management
    public UpdateProfileUseCase getUpdateProfileUseCase() {
        return updateProfileUseCase;
    }

    public ChangePasswordUseCase getChangePasswordUseCase() {
        return changePasswordUseCase;
    }

    // Getters for repositories (for direct access when needed)
    public IUserRepository getUserRepository() {
        return userRepository;
    }

    public IResidentRepository getResidentRepository() {
        return residentRepository;
    }

    public IDocumentRepository getDocumentRepository() {
        return documentRepository;
    }

    public IDocumentRequestRepository getDocumentRequestRepository() {
        return documentRequestRepository;
    }

    public IVoterApplicationRepository getVoterApplicationRepository() {
        return voterApplicationRepository;
    }

    public IOfficialRepository getOfficialRepository() {
        return officialRepository;
    }

    public IBarangayInfoRepository getBarangayInfoRepository() {
        return barangayInfoRepository;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
