package com.barangay.presentation.controllers;

import com.barangay.application.dto.DocumentRequestCountsDto;
import com.barangay.application.dto.DocumentStatisticsDto;
import com.barangay.application.dto.ResidentStatisticsDto;
import com.barangay.domain.entities.ApplicationStatus;
import com.barangay.domain.entities.DocumentRequest;
import com.barangay.domain.entities.Document;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.entities.VoterApplication;
import com.barangay.domain.repositories.IVoterApplicationRepository;
import com.barangay.infrastructure.config.DIContainer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard displaying consolidated statistics and quick insights.
 */
public class DashboardController implements ModuleController {

    @FXML
    private Label totalResidentsLabel;

    @FXML
    private Label activeResidentsLabel;

    @FXML
    private Label maleResidentsLabel;

    @FXML
    private Label femaleResidentsLabel;

    @FXML
    private Label registeredVotersLabel;

    @FXML
    private Label totalDocumentsLabel;

    @FXML
    private Label documentsTodayLabel;

    @FXML
    private Label documentsMonthLabel;

    @FXML
    private Label barangayIdCountLabel;

    @FXML
    private Label clearanceCountLabel;

    @FXML
    private Label residencyCountLabel;

    @FXML
    private Label pendingApplicationsLabel;

    @FXML
    private Label scheduledApplicationsLabel;

    @FXML
    private Label verifiedApplicationsLabel;

    @FXML
    private Label pendingRequestsLabel;

    @FXML
    private Label underReviewRequestsLabel;

    @FXML
    private Label approvedRequestsLabel;

    @FXML
    private Label usersSummaryLabel;

    @FXML
    private ListView<String> insightsListView;

    @FXML
    private VBox adminDashboardContainer;

    @FXML
    private VBox residentDashboardContainer;

    @FXML
    private Label residentNameLabel;

    @FXML
    private Label residentIdLabel;

    @FXML
    private Label residentContactLabel;

    @FXML
    private Label residentAddressLabel;

    @FXML
    private Label residentStatusLabel;

    @FXML
    private Label residentVoterStatusLabel;

    @FXML
    private Label residentNoticeLabel;

    @FXML
    private ListView<String> residentDocumentsListView;

    @FXML
    private ListView<String> residentApplicationsListView;

    @FXML
    private ListView<String> residentRequestsListView;

    @FXML
    private Label residentMissingLinkLabel;

    @FXML
    private VBox carouselSection;

    @FXML
    private ImageView carouselImageView;

    @FXML
    private Label carouselCounterLabel;

    @FXML
    private Label carouselStatusLabel;

    @FXML
    private Button carouselPrevButton;

    @FXML
    private Button carouselNextButton;

    private DIContainer container;
    private MainLayoutController mainLayoutController;
    private User currentUser;

    private final ObservableList<String> insights = FXCollections.observableArrayList();

    private boolean residentMode;
    private Resident linkedResident;
    private List<String> carouselImages = Collections.emptyList();
    private int currentCarouselIndex;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    @Override
    public void init(DIContainer container, MainLayoutController mainLayoutController) {
        this.container = container;
        this.mainLayoutController = mainLayoutController;
        this.currentUser = mainLayoutController.getCurrentUser();
        this.residentMode = currentUser != null && currentUser.getRole() == UserRole.RESIDENT;
        insightsListView.setItems(insights);
        configureDashboardVisibility();
    }

    @Override
    public void refresh() {
        populateCarousel();
        if (residentMode) {
            populateResidentDashboard();
            return;
        }
        populateResidentCards();
        populateDocumentCards();
        populateApplicationCards();
        populateDocumentRequestCards();
        populateUserSummary();
        populateInsights();
    }

    private void populateResidentCards() {
        ResidentStatisticsDto stats = container.getResidentStatisticsUseCase().execute();
        totalResidentsLabel.setText(String.valueOf(stats.getTotalResidents()));
        activeResidentsLabel.setText(String.valueOf(stats.getActiveResidents()));
        maleResidentsLabel.setText(String.valueOf(stats.getMaleCount()));
        femaleResidentsLabel.setText(String.valueOf(stats.getFemaleCount()));
        registeredVotersLabel.setText(String.valueOf(stats.getRegisteredVoters()));
    }

    private void populateDocumentCards() {
        DocumentStatisticsDto stats = container.getDocumentStatisticsUseCase().execute();
        totalDocumentsLabel.setText(String.valueOf(stats.getTotalDocuments()));
        documentsTodayLabel.setText(String.valueOf(stats.getDocumentsToday()));
        documentsMonthLabel.setText(String.valueOf(stats.getDocumentsThisMonth()));
        barangayIdCountLabel.setText(String.valueOf(stats.getBarangayIDs()));
        clearanceCountLabel.setText(String.valueOf(stats.getClearances()));
        residencyCountLabel.setText(String.valueOf(stats.getResidencyCertificates()));
    }

    private void populateApplicationCards() {
        IVoterApplicationRepository repository = container.getVoterApplicationRepository();
        pendingApplicationsLabel.setText(String.valueOf(repository.countPending()));
        scheduledApplicationsLabel.setText(String.valueOf(
                repository.findByStatus(ApplicationStatus.SCHEDULED).size()));
        verifiedApplicationsLabel.setText(String.valueOf(
                repository.findByStatus(ApplicationStatus.VERIFIED).size()));
    }

    private void populateDocumentRequestCards() {
        if (pendingRequestsLabel == null) {
            return;
        }
        try {
            DocumentRequestCountsDto counts = container.getDocumentRequestCountsUseCase().execute();
            pendingRequestsLabel.setText(String.valueOf(counts.getPending()));
            underReviewRequestsLabel.setText(String.valueOf(counts.getUnderReview()));
            approvedRequestsLabel.setText(String.valueOf(counts.getApproved()));
        } catch (Exception ex) {
            pendingRequestsLabel.setText("--");
            underReviewRequestsLabel.setText("--");
            approvedRequestsLabel.setText("--");
        }
    }

    private void populateUserSummary() {
        int superAdmins = container.getUserRepository().countByRole(UserRole.SUPER_ADMIN);
        int admins = container.getUserRepository().countByRole(UserRole.ADMIN);
        int clerks = container.getUserRepository().countByRole(UserRole.CLERK);
        int residents = container.getUserRepository().countByRole(UserRole.RESIDENT);
        usersSummaryLabel.setText(String.format("Super Admins: %d  |  Admins: %d  |  Clerks: %d  |  Residents: %d",
                superAdmins, admins, clerks, residents));
    }

    private void populateInsights() {
        insights.clear();
        int pending = Integer.parseInt(pendingApplicationsLabel.getText());
        if (pending > 0) {
            insights.add("\u2022 " + pending + " voter applications require review.");
        } else {
            insights.add("\u2022 All voter applications are up to date.");
        }

        int todayDocs = Integer.parseInt(documentsTodayLabel.getText());
        if (todayDocs == 0) {
            insights.add("\u2022 No documents issued today yet.");
        } else {
            insights.add("\u2022 " + todayDocs + " documents issued today.");
        }

        insights.add("\u2022 Registered voters make up " + percentage(
                Integer.parseInt(registeredVotersLabel.getText()),
                Integer.parseInt(totalResidentsLabel.getText())) + "% of residents.");

        if (pendingRequestsLabel != null && underReviewRequestsLabel != null) {
            try {
                int pendingRequests = Integer.parseInt(pendingRequestsLabel.getText());
                int underReview = Integer.parseInt(underReviewRequestsLabel.getText());
                int awaiting = pendingRequests + underReview;
                if (awaiting > 0) {
                    insights.add("\u2022 " + awaiting + " online document request(s) awaiting review.");
                } else {
                    insights.add("\u2022 No pending online document requests.");
                }
            } catch (NumberFormatException ignored) {
                insights.add("\u2022 Document request counters unavailable.");
            }
        }

        insights.add("\u2022 Active officials on record: " +
                container.getListOfficialsUseCase().getCurrentOfficials().size());

        insights.add("\u2022 Welcome back, " +
                mainLayoutController.getCurrentUser().getUsername() + "!");
    }

    private void populateCarousel() {
        if (carouselSection == null) {
            return;
        }
        try {
            carouselImages = container.getGetBarangayInfoUseCase().execute().getDashboardImages();
        } catch (Exception ex) {
            carouselImages = Collections.emptyList();
        }

        if (carouselImages == null || carouselImages.isEmpty()) {
            setCarouselVisibility(false);
            return;
        }

        setCarouselVisibility(true);
        currentCarouselIndex = 0;
        updateCarouselImage();
    }

    private void setCarouselVisibility(boolean visible) {
        if (carouselSection == null) {
            return;
        }
        carouselSection.setVisible(visible);
        carouselSection.setManaged(visible);
        if (!visible && carouselCounterLabel != null) {
            carouselCounterLabel.setText("");
        }
    }

    private void updateCarouselImage() {
        if (carouselImageView == null || carouselImages == null || carouselImages.isEmpty()) {
            return;
        }

        String path = carouselImages.get(currentCarouselIndex);
        Image image = tryLoadImage(path);
        boolean hasImage = image != null && !image.isError();
        carouselImageView.setImage(hasImage ? image : null);

        if (carouselStatusLabel != null) {
            carouselStatusLabel.setText(hasImage ? "" : "Image unavailable");
            carouselStatusLabel.setVisible(!hasImage);
            carouselStatusLabel.setManaged(!hasImage);
        }

        if (carouselCounterLabel != null) {
            carouselCounterLabel.setText((currentCarouselIndex + 1) + " / " + carouselImages.size());
        }

        if (carouselPrevButton != null) {
            carouselPrevButton.setDisable(currentCarouselIndex == 0);
        }
        if (carouselNextButton != null) {
            carouselNextButton.setDisable(currentCarouselIndex >= carouselImages.size() - 1);
        }
    }

    @FXML
    private void handleCarouselPrev() {
        if (currentCarouselIndex <= 0) {
            return;
        }
        currentCarouselIndex--;
        updateCarouselImage();
    }

    @FXML
    private void handleCarouselNext() {
        if (carouselImages == null || currentCarouselIndex >= carouselImages.size() - 1) {
            return;
        }
        currentCarouselIndex++;
        updateCarouselImage();
    }

    private Image tryLoadImage(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String trimmed = path.trim();
        try {
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                return new Image(trimmed, true);
            }

            if (trimmed.startsWith("classpath:")) {
                String resourcePath = trimmed.substring("classpath:".length());
                InputStream stream = getClass().getResourceAsStream(resourcePath.startsWith("/")
                        ? resourcePath : "/" + resourcePath);
                if (stream != null) {
                    return new Image(stream);
                }
            }

            if (trimmed.startsWith("/")) {
                URL resource = getClass().getResource(trimmed);
                if (resource != null) {
                    return new Image(resource.toExternalForm(), true);
                }
            }

            Path filePath = Path.of(trimmed);
            if (Files.exists(filePath)) {
                return new Image(filePath.toUri().toString(), true);
            }

            try {
                URL url = new URL(trimmed);
                return new Image(url.toExternalForm(), true);
            } catch (MalformedURLException ignored) {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private void configureDashboardVisibility() {
        setVisible(adminDashboardContainer, !residentMode);
        setVisible(residentDashboardContainer, residentMode);
    }

    private void populateResidentDashboard() {
        loadLinkedResident();
        if (linkedResident == null) {
            showMissingResidentLinkState();
            return;
        }

        residentMissingLinkLabel.setVisible(false);
        residentMissingLinkLabel.setManaged(false);

        residentNameLabel.setText(linkedResident.getFullName());
        residentIdLabel.setText(linkedResident.getId().getValue());
        residentContactLabel.setText("Contact: " + optionalValue(linkedResident.getContact()));
        residentAddressLabel.setText("Address: " + formatAddress(linkedResident));
        residentStatusLabel.setText(linkedResident.isActive() ? "Active" : "Inactive");
        residentVoterStatusLabel.setText(linkedResident.isVoter() ? "Registered voter" : "Not yet a registered voter");
        residentNoticeLabel.setText(linkedResident.isVoter()
                ? "Need a document? Request one online or visit the Documents section for downloads."
                : "Track or submit voter applications from the Voter Applications module.");

        populateResidentDocuments();
        populateResidentApplications();
        populateResidentRequests();
    }

    private void populateResidentDocuments() {
        if (residentDocumentsListView == null) {
            return;
        }
        List<Document> documents = container.getDocumentRepository()
                .findByResidentId(linkedResident.getId());
        List<String> entries = documents.stream()
                .sorted(Comparator.comparing(Document::getIssuedDate).reversed())
                .map(doc -> String.format("%s • %s • Issued %s", doc.getReference().getValue(),
                        doc.getType(), doc.getIssuedDate()))
                .collect(Collectors.toList());
        if (entries.isEmpty()) {
            entries.add("No documents issued yet.");
        }
        residentDocumentsListView.setItems(FXCollections.observableArrayList(entries));
    }

    private void populateResidentApplications() {
        if (residentApplicationsListView == null) {
            return;
        }
        List<String> entries = container.getVoterApplicationRepository()
                .findByResidentId(linkedResident.getId())
                .stream()
                .sorted(Comparator.comparing(VoterApplication::getSubmittedAt).reversed())
                .map(app -> {
                    String date = formatDateTime(app.getSubmittedAt());
                    String base = String.format("%s • %s (%s)", app.getId(), app.getApplicationType(), app.getStatus());
                    if (app.getAppointmentDateTime() != null) {
                        base += String.format(" • Schedule: %s", formatDateTime(app.getAppointmentDateTime()));
                    }
                    return base + " • Submitted " + date;
                })
                .collect(Collectors.toList());
        if (entries.isEmpty()) {
            entries.add("No voter applications submitted yet.");
        }
        residentApplicationsListView.setItems(FXCollections.observableArrayList(entries));
    }

    private void populateResidentRequests() {
        if (residentRequestsListView == null) {
            return;
        }
        try {
            List<DocumentRequest> requests = container.getListDocumentRequestsUseCase()
                    .execute(null, null);
            List<String> entries = requests.stream()
                    .sorted(Comparator.comparing(DocumentRequest::getCreatedAt).reversed())
                    .map(req -> {
                        String status = req.getStatus().toString();
                        if (req.getLinkedDocumentReference() != null) {
                            status += " • Issued: " + req.getLinkedDocumentReference();
                        }
                        return String.format("%s • %s • %s (updated %s)",
                                req.getId(),
                                req.getDocumentType(),
                                status,
                                formatDateTime(req.getUpdatedAt()));
                    })
                    .collect(Collectors.toList());
            if (entries.isEmpty()) {
                entries.add("No document requests submitted yet.");
            }
            residentRequestsListView.setItems(FXCollections.observableArrayList(entries));
        } catch (Exception ex) {
            residentRequestsListView.setItems(
                    FXCollections.observableArrayList("Unable to load document requests."));
        }
    }

    private void loadLinkedResident() {
        ResidentId residentId = currentUser != null ? currentUser.getLinkedResidentId() : null;
        if (residentId == null) {
            linkedResident = null;
            return;
        }
        linkedResident = container.getResidentRepository().findById(residentId).orElse(null);
    }

    private void showMissingResidentLinkState() {
        residentDocumentsListView.setItems(FXCollections.observableArrayList());
        residentApplicationsListView.setItems(FXCollections.observableArrayList());
        if (residentRequestsListView != null) {
            residentRequestsListView.setItems(FXCollections.observableArrayList());
        }
        residentMissingLinkLabel.setVisible(true);
        residentMissingLinkLabel.setManaged(true);
        residentNameLabel.setText("--");
        residentIdLabel.setText("--");
        residentContactLabel.setText("Contact: --");
        residentAddressLabel.setText("Address: --");
        residentStatusLabel.setText("--");
        residentVoterStatusLabel.setText("Voter status unavailable");
        residentNoticeLabel.setText("We need to link your resident profile. Please visit the barangay office.");
    }

    private void setVisible(VBox node, boolean visible) {
        if (node == null) {
            return;
        }
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private String optionalValue(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }

    private String formatAddress(Resident resident) {
        if (resident.getAddress() == null) {
            return "Not provided";
        }
        return resident.getAddress().toString();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "--";
        }
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    private String percentage(int part, int whole) {
        if (whole == 0) {
            return "0";
        }
        double pct = (double) part / (double) whole * 100.0;
        return String.format("%.1f", pct);
    }

    @FXML
    private void handleRequestDocument() {
        if (mainLayoutController != null) {
            mainLayoutController.openDocumentRequests();
        }
    }
}
