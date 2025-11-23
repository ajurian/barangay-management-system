package com.barangay.presentation.controllers;

import com.barangay.application.dto.AppointmentSlipOutputDto;
import com.barangay.application.dto.SubmitVoterApplicationInputDto;
import com.barangay.domain.entities.ApplicationStatus;
import com.barangay.domain.entities.ApplicationType;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.entities.VoterApplication;
import com.barangay.domain.valueobjects.Address;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.util.DialogUtil;
import com.barangay.presentation.util.FormDialogUtil;
import com.barangay.presentation.util.FormFieldIndicator;
import com.barangay.presentation.util.TableCopyUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Module controller for managing voter applications lifecycle.
 */
public class VoterApplicationsController implements ModuleController {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    private TableView<VoterApplication> applicationsTable;

    @FXML
    private TableColumn<VoterApplication, String> idColumn;

    @FXML
    private TableColumn<VoterApplication, String> residentColumn;

    @FXML
    private TableColumn<VoterApplication, ApplicationType> typeColumn;

    @FXML
    private TableColumn<VoterApplication, ApplicationStatus> statusColumn;

    @FXML
    private TableColumn<VoterApplication, String> submittedColumn;

    @FXML
    private TableColumn<VoterApplication, String> appointmentColumn;

    @FXML
    private TableColumn<VoterApplication, String> updatedColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<ApplicationStatus> statusFilter;

    @FXML
    private ChoiceBox<ApplicationType> typeFilter;

    @FXML
    private Label applicationCountLabel;

    @FXML
    private Button reviewButton;

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Button scheduleButton;

    @FXML
    private Button markVerifiedButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button downloadSlipButton;

    @FXML
    private VBox staffFiltersContainer;

    @FXML
    private VBox residentBannerContainer;

    @FXML
    private Label residentBannerLabel;

    @FXML
    private HBox staffActionsBox;

    @FXML
    private HBox residentActionsBox;

    @FXML
    private Button submitApplicationButton;

    @FXML
    private Button residentDownloadSlipButton;

    private final ObservableList<VoterApplication> backingList = FXCollections.observableArrayList();

    private DIContainer container;
    private boolean residentMode;
    private ResidentId currentResidentId;

    @Override
    public void init(DIContainer container, MainLayoutController mainLayoutController) {
        this.container = container;
        this.residentMode = mainLayoutController.getCurrentUser().getRole() == UserRole.RESIDENT;
        this.currentResidentId = residentMode ? mainLayoutController.getCurrentUser().getLinkedResidentId() : null;
        configureTable();
        configureFilters();
        configureRoleView();
        refresh();
    }

    @Override
    public void refresh() {
        loadApplications();
        applyFilters();
        updateActionButtons();
    }

    @FXML
    private void handleApplyFilters() {
        if (residentMode) {
            return;
        }
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        if (residentMode) {
            return;
        }
        searchField.clear();
        statusFilter.getSelectionModel().selectFirst();
        typeFilter.getSelectionModel().selectFirst();
        applyFilters();
    }

    @FXML
    private void handleRefresh() {
        refresh();
    }

    @FXML
    private void handleSetUnderReview() {
        if (residentMode) {
            DialogUtil.showWarning("Voter Application", "Residents cannot change review states.");
            return;
        }
        VoterApplication selected = getSelectedApplication();
        if (selected == null) {
            return;
        }
        try {
            container.getReviewVoterApplicationUseCase().setUnderReview(selected.getId());
            DialogUtil.showInfo("Voter Application", "Application marked as under review.");
            refresh();
        } catch (Exception ex) {
            DialogUtil.showError("Voter Application", ex.getMessage());
        }
    }

    @FXML
    private void handleApprove() {
        if (residentMode) {
            DialogUtil.showWarning("Approve Application", "Residents cannot approve applications.");
            return;
        }
        VoterApplication selected = getSelectedApplication();
        if (selected == null) {
            return;
        }
        if (selected.getStatus() != ApplicationStatus.PENDING &&
                selected.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            DialogUtil.showWarning("Approve Application", "Only pending or under review applications can be approved.");
            return;
        }

        Optional<String> notes = promptForNotes("Approval Notes");
        notes.ifPresent(value -> {
            try {
                container.getReviewVoterApplicationUseCase().approve(selected.getId(), value);
                DialogUtil.showInfo("Approve Application", "Application approved successfully.");
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Approve Application", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleReject() {
        if (residentMode) {
            DialogUtil.showWarning("Reject Application", "Residents cannot reject applications.");
            return;
        }
        VoterApplication selected = getSelectedApplication();
        if (selected == null) {
            return;
        }
        if (selected.getStatus() == ApplicationStatus.REJECTED) {
            DialogUtil.showWarning("Reject Application", "Application is already rejected.");
            return;
        }

        Optional<String> notes = promptForNotes("Rejection Notes");
        notes.ifPresent(value -> {
            try {
                container.getReviewVoterApplicationUseCase().reject(selected.getId(), value);
                DialogUtil.showInfo("Reject Application", "Application rejected.");
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Reject Application", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleScheduleVerification() {
        if (residentMode) {
            DialogUtil.showWarning("Schedule Verification", "Residents cannot schedule verifications.");
            return;
        }
        VoterApplication selected = getSelectedApplication();
        if (selected == null) {
            return;
        }
        if (selected.getStatus() != ApplicationStatus.APPROVED) {
            DialogUtil.showWarning("Schedule Verification", "Only approved applications can be scheduled.");
            return;
        }

        Optional<ScheduleInput> scheduleInput = promptForSchedule();
        scheduleInput.ifPresent(input -> {
            try {
                container.getScheduleVerificationUseCase().execute(selected.getId(), input.dateTime(), input.venue());
                DialogUtil.showInfo("Schedule Verification", "Verification schedule saved.");
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Schedule Verification", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleMarkVerified() {
        if (residentMode) {
            DialogUtil.showWarning("Mark Verified", "Residents cannot mark applications as verified.");
            return;
        }
        VoterApplication selected = getSelectedApplication();
        if (selected == null) {
            return;
        }
        if (selected.getStatus() != ApplicationStatus.SCHEDULED) {
            DialogUtil.showWarning("Mark Verified", "Only scheduled applications can be marked as verified.");
            return;
        }
        try {
            container.getVerifyVoterApplicationUseCase().execute(selected.getId());
            DialogUtil.showInfo("Mark Verified", "Application marked as verified.");
            refresh();
        } catch (Exception ex) {
            DialogUtil.showError("Mark Verified", ex.getMessage());
        }
    }

    @FXML
    private void handleSubmitApplication() {
        if (!residentMode) {
            return;
        }
        if (currentResidentId == null) {
            DialogUtil.showError("Submit Application", "Your account is not linked to a resident profile yet.");
            return;
        }
        Optional<SubmitVoterApplicationInputDto> result = showSubmitDialog();
        result.ifPresent(input -> {
            try {
                String applicationId = container.getSubmitVoterApplicationUseCase().execute(input);
                DialogUtil.showInfo("Submit Application", "Application submitted. Reference: " + applicationId);
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Submit Application", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleDownloadSlip() {
        VoterApplication selected = getSelectedApplication();
        if (selected == null) {
            return;
        }
        if (selected.getStatus() != ApplicationStatus.SCHEDULED) {
            DialogUtil.showWarning("Appointment Slip",
                    "Only applications with a scheduled verification have downloadable slips.");
            return;
        }

        try {
            AppointmentSlipOutputDto slip = container.getGenerateAppointmentSlipUseCase()
                    .execute(selected.getId());
            File destination = chooseSlipDestination(slip.getSuggestedFileName());
            if (destination == null) {
                return;
            }
            Files.write(destination.toPath(), slip.getFileContent(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            DialogUtil.showInfo("Appointment Slip", "Slip saved to:\n" + destination.getAbsolutePath());
        } catch (Exception ex) {
            DialogUtil.showError("Appointment Slip", ex.getMessage());
        }
    }

    @FXML
    private void handleViewDetails() {
        if (residentMode) {
            DialogUtil.showWarning("Voter Application", "Residents cannot access detailed applicant view from here.");
            return;
        }
        VoterApplication selected = getSelectedApplication();
        if (selected == null) {
            return;
        }

        Resident resident = container.getResidentRepository()
                .findById(selected.getResidentId())
                .orElse(null);

        Dialog<ButtonType> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle("Voter Application Details");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setHeaderText(String.format("%s · %s", selected.getId(), formatEnum(selected.getStatus())));

        VBox content = new VBox(12);
        content.setPadding(new Insets(12));

        content.getChildren().add(createDetailsSection("Application Summary", new String[][] {
                { "Application Type", formatEnum(selected.getApplicationType()) },
                { "Status", formatEnum(selected.getStatus()) },
                { "Submitted", formatDateTime(selected.getSubmittedAt()) },
                { "Last Updated", formatDateTime(selected.getUpdatedAt()) },
                { "Reviewed By", formatValue(selected.getReviewedBy()) },
                { "Review Notes", formatValue(selected.getReviewNotes(), "No notes recorded") }
        }));

        content.getChildren().add(createDetailsSection("Supporting Information", new String[][] {
            { "Resident ID", selected.getResidentId().getValue() },
            { "Current Registration Details",
                formatValue(selected.getCurrentRegistrationDetails(), "Not provided") }
        }));
        content.getChildren().add(createIdPhotoSection(selected));

        content.getChildren().add(createDetailsSection("Appointment", new String[][] {
                { "Schedule", formatDateTime(selected.getAppointmentDateTime()) },
                { "Venue", formatValue(selected.getAppointmentVenue()) },
                { "Slip Reference", formatValue(selected.getAppointmentSlipReference()) }
        }));

        if (resident != null) {
            content.getChildren().add(createDetailsSection("Resident Profile", new String[][] {
                    { "Name", formatValue(resident.getFullName()) },
                    { "Birth Date", formatDate(resident.getBirthDate()) },
                    { "Gender", formatEnum(resident.getGender()) },
                    { "Civil Status", formatEnum(resident.getCivilStatus()) },
                    { "Contact", formatValue(resident.getContact()) },
                    { "Address", formatAddress(resident.getAddress()) },
                    { "Occupation", formatValue(resident.getOccupation()) },
                    { "Is Voter", formatBoolean(resident.isVoter()) },
                    { "Resident Status", resident.isActive() ? "Active" : "Inactive" }
            }));
        } else {
            Label missingResident = new Label(
                    "Resident record not found. The resident may have been removed or is inactive.");
            missingResident.setWrapText(true);
            content.getChildren().add(missingResident);
        }

        dialog.getDialogPane().setContent(content);
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        residentColumn
                .setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getResidentId().getValue()));
        typeColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getApplicationType()));
        statusColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStatus()));
        submittedColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                formatDateTime(cell.getValue().getSubmittedAt())));
        appointmentColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                formatDateTime(cell.getValue().getAppointmentDateTime())));
        updatedColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                formatDateTime(cell.getValue().getUpdatedAt())));

        applicationsTable.setItems(FXCollections.observableArrayList());
        TableCopyUtil.attachCopyContextMenu(applicationsTable,
                application -> application != null ? application.getId() : null,
                "Copy Application ID");
        applicationsTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> updateActionButtons());
    }

    private void configureFilters() {
        ObservableList<ApplicationStatus> statuses = FXCollections.observableArrayList();
        statuses.add(null);
        statuses.addAll(Arrays.asList(ApplicationStatus.values()));
        statusFilter.setItems(statuses);
        statusFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(ApplicationStatus status) {
                return status == null ? "All" : formatEnum(status);
            }

            @Override
            public ApplicationStatus fromString(String string) {
                return null;
            }
        });

        ObservableList<ApplicationType> types = FXCollections.observableArrayList();
        types.add(null);
        types.addAll(Arrays.asList(ApplicationType.values()));
        typeFilter.setItems(types);
        typeFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(ApplicationType type) {
                return type == null ? "All" : formatEnum(type);
            }

            @Override
            public ApplicationType fromString(String string) {
                return null;
            }
        });

        statusFilter.getSelectionModel().selectFirst();
        typeFilter.getSelectionModel().selectFirst();

        statusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        typeFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadApplications() {
        List<VoterApplication> all;
        if (residentMode) {
            if (currentResidentId == null) {
                backingList.clear();
                return;
            }
            all = container.getVoterApplicationRepository()
                    .findByResidentId(currentResidentId)
                    .stream()
                    .sorted(Comparator.comparing(VoterApplication::getSubmittedAt).reversed())
                    .collect(Collectors.toList());
        } else {
            all = container.getVoterApplicationRepository()
                    .findAll()
                    .stream()
                    .sorted(Comparator.comparing(VoterApplication::getSubmittedAt).reversed())
                    .collect(Collectors.toList());
        }
        backingList.setAll(all);
    }

    private void applyFilters() {
        if (residentMode) {
            applicationsTable.setItems(FXCollections.observableArrayList(backingList));
            applicationCountLabel.setText(String.format("Showing %d application(s)", backingList.size()));
            return;
        }
        String term = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        ApplicationStatus status = statusFilter.getSelectionModel().getSelectedItem();
        ApplicationType type = typeFilter.getSelectionModel().getSelectedItem();

        List<VoterApplication> filtered = backingList.stream()
                .filter(app -> status == null || app.getStatus() == status)
                .filter(app -> type == null || app.getApplicationType() == type)
                .filter(app -> term.isEmpty() ||
                        app.getId().toLowerCase().contains(term) ||
                        app.getResidentId().getValue().toLowerCase().contains(term))
                .collect(Collectors.toList());

        applicationsTable.setItems(FXCollections.observableArrayList(filtered));
        applicationCountLabel
                .setText(String.format("Showing %d of %d applications", filtered.size(), backingList.size()));
        updateActionButtons();
    }

    private void updateActionButtons() {
        VoterApplication selected = applicationsTable.getSelectionModel().getSelectedItem();
        boolean hasSelection = selected != null;
        boolean slipAvailable = hasSelection && selected.getStatus() == ApplicationStatus.SCHEDULED;

        if (residentMode) {
            reviewButton.setDisable(true);
            approveButton.setDisable(true);
            rejectButton.setDisable(true);
            scheduleButton.setDisable(true);
            markVerifiedButton.setDisable(true);
            if (downloadSlipButton != null) {
                downloadSlipButton.setDisable(true);
            }
            if (residentDownloadSlipButton != null) {
                residentDownloadSlipButton.setDisable(!slipAvailable);
            }
            return;
        }

        reviewButton.setDisable(!hasSelection);
        approveButton.setDisable(!hasSelection);
        rejectButton.setDisable(!hasSelection);
        scheduleButton.setDisable(!hasSelection);
        markVerifiedButton.setDisable(!hasSelection);
        if (viewDetailsButton != null) {
            viewDetailsButton.setDisable(!hasSelection);
        }
        if (downloadSlipButton != null) {
            downloadSlipButton.setDisable(!slipAvailable);
        }
        if (residentDownloadSlipButton != null) {
            residentDownloadSlipButton.setDisable(true);
        }
    }

    private VoterApplication getSelectedApplication() {
        VoterApplication selected = applicationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Voter Application", "Please select an application first.");
        }
        return selected;
    }

    private Optional<String> promptForNotes(String title) {
        Dialog<String> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter remarks (optional)");
        notesArea.setPrefRowCount(4);

        dialog.getDialogPane().setContent(notesArea);
        dialog.setResultConverter(button -> button == ButtonType.OK ? notesArea.getText() : null);
        return dialog.showAndWait();
    }

    private Optional<ScheduleInput> promptForSchedule() {
        Dialog<ScheduleInput> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle("Schedule Verification");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");
        TextField timeField = new TextField();
        timeField.setPromptText("Time (HH:mm)");
        TextField venueField = new TextField();
        venueField.setPromptText("Venue");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, FormFieldIndicator.requiredLabel("Date"), datePicker);
        grid.addRow(1, FormFieldIndicator.requiredLabel("Time"), timeField);
        grid.addRow(2, FormFieldIndicator.requiredLabel("Venue"), venueField);

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            LocalDate date = datePicker.getValue();
            String timeValue = timeField.getText();
            String venue = venueField.getText();
            if (date == null || timeValue == null || timeValue.trim().isEmpty() ||
                    venue == null || venue.trim().isEmpty()) {
                return Optional.of("Date, time, and venue are required.");
            }
            try {
                LocalTime.parse(timeValue.trim());
            } catch (DateTimeParseException ex) {
                return Optional.of("Invalid time format. Use HH:mm.");
            }
            return Optional.empty();
        }, "Schedule Verification");

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.parse(timeField.getText().trim());
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            return new ScheduleInput(dateTime, venueField.getText().trim());
        });

        return dialog.showAndWait();
    }

    private Optional<SubmitVoterApplicationInputDto> showSubmitDialog() {
        Dialog<SubmitVoterApplicationInputDto> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle("Submit Voter Application");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<ApplicationType> typeChoice = new ChoiceBox<>(
                FXCollections.observableArrayList(ApplicationType.values()));
        typeChoice.getSelectionModel().select(ApplicationType.NEW_REGISTRATION);
        TextField idFrontField = new TextField();
        idFrontField.setPromptText("Select file...");
        idFrontField.setEditable(false);
        Button frontBrowseButton = new Button("Browse...");
        frontBrowseButton.setOnAction(event -> {
            File selected = chooseFile(dialog, "Select Valid ID (Front)");
            if (selected != null) {
                idFrontField.setText(selected.getAbsolutePath());
            }
        });
        HBox frontInput = new HBox(8, idFrontField, frontBrowseButton);

        TextField idBackField = new TextField();
        idBackField.setPromptText("Select file...");
        idBackField.setEditable(false);
        Button backBrowseButton = new Button("Browse...");
        backBrowseButton.setOnAction(event -> {
            File selected = chooseFile(dialog, "Select Valid ID (Back)");
            if (selected != null) {
                idBackField.setText(selected.getAbsolutePath());
            }
        });
        HBox backInput = new HBox(8, idBackField, backBrowseButton);

        TextArea registrationDetailsArea = new TextArea();
        registrationDetailsArea.setPromptText("Current registration details (if transfer)");
        registrationDetailsArea.setPrefRowCount(3);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, FormFieldIndicator.requiredLabel("Application Type"), typeChoice);
        grid.addRow(1, FormFieldIndicator.requiredLabel("Valid ID - Front"), frontInput);
        grid.addRow(2, FormFieldIndicator.requiredLabel("Valid ID - Back"), backInput);
        grid.addRow(3, FormFieldIndicator.optionalLabel("Current Registration"), registrationDetailsArea);

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            if (idFrontField.getText() == null || idFrontField.getText().isBlank() ||
                    idBackField.getText() == null || idBackField.getText().isBlank()) {
                return Optional.of("Please select valid ID files for front and back.");
            }
            return Optional.empty();
        }, "Submit Application");

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            return new SubmitVoterApplicationInputDto(
                    currentResidentId.getValue(),
                    typeChoice.getValue(),
                    registrationDetailsArea.getText(),
                    idFrontField.getText().trim(),
                    idBackField.getText().trim());
        });

        return dialog.showAndWait();
    }

    private File chooseFile(Dialog<?> dialog, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg"));
        Window owner = dialog.getDialogPane().getScene() != null
                ? dialog.getDialogPane().getScene().getWindow()
                : null;
        return fileChooser.showOpenDialog(owner);
    }

    private File chooseSlipDestination(String suggestedFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Appointment Slip");
        fileChooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        fileChooser.setInitialFileName(suggestedFileName);
        Window owner = applicationsTable.getScene() != null
                ? applicationsTable.getScene().getWindow()
                : null;
        return fileChooser.showSaveDialog(owner);
    }

    private void configureRoleView() {
        setNodeVisible(staffFiltersContainer, !residentMode);
        setNodeVisible(staffActionsBox, !residentMode);
        setNodeVisible(residentBannerContainer, residentMode);
        setNodeVisible(residentActionsBox, residentMode);
        if (residentMode) {
            if (residentBannerLabel != null) {
                residentBannerLabel.setText(currentResidentId == null
                        ? "Your account is not yet linked to a resident profile. Please visit the barangay office."
                        : "Submit a new application or track its progress below.");
            }
            if (submitApplicationButton != null) {
                submitApplicationButton.setDisable(currentResidentId == null);
            }
            if (residentDownloadSlipButton != null) {
                residentDownloadSlipButton.setDisable(true);
            }
        }
    }

    private void setNodeVisible(javafx.scene.Node node, boolean visible) {
        if (node == null) {
            return;
        }
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "--";
        }
        return DATE_TIME_FORMAT.format(dateTime);
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "--";
        }
        return DATE_FORMAT.format(date);
    }

    private String formatEnum(Enum<?> value) {
        if (value == null) {
            return "--";
        }
        return value.toString();
    }

    private String formatValue(String value) {
        return formatValue(value, "--");
    }

    private String formatValue(String value, String emptyPlaceholder) {
        return (value == null || value.isBlank()) ? emptyPlaceholder : value.trim();
    }

    private String formatAddress(Address address) {
        return address == null ? "--" : formatValue(address.getFullAddress());
    }

    private String formatBoolean(boolean value) {
        return value ? "Yes" : "No";
    }

    private VBox createDetailsSection(String title, String[][] rows) {
        VBox section = new VBox(6);
        Label header = new Label(title);
        header.setStyle("-fx-font-weight: bold;");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        for (int i = 0; i < rows.length; i++) {
            addDetailRow(grid, i, rows[i][0], rows[i][1]);
        }
        section.getChildren().addAll(header, grid);
        return section;
    }

    private void addDetailRow(GridPane grid, int rowIndex, String labelText, String valueText) {
        Label label = new Label(labelText + ":");
        label.setStyle("-fx-font-weight: bold;");
        Label value = new Label(valueText);
        value.setWrapText(true);
        grid.add(label, 0, rowIndex);
        grid.add(value, 1, rowIndex);
    }

    private VBox createIdPhotoSection(VoterApplication application) {
        VBox section = new VBox(6);
        Label header = new Label("Valid ID Photos");
        header.setStyle("-fx-font-weight: bold;");
        HBox actions = new HBox(8);
        Button frontButton = new Button("View Front ID");
        frontButton.setDisable(!isPhotoAvailable(application.getValidIdFrontPath()));
        frontButton.setOnAction(evt -> showIdPhoto("Valid ID - Front", application.getValidIdFrontPath()));
        Button backButton = new Button("View Back ID");
        backButton.setDisable(!isPhotoAvailable(application.getValidIdBackPath()));
        backButton.setOnAction(evt -> showIdPhoto("Valid ID - Back", application.getValidIdBackPath()));
        actions.getChildren().addAll(frontButton, backButton);
        section.getChildren().addAll(header, actions);
        return section;
    }

    private boolean isPhotoAvailable(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return false;
        }
        try {
            Path path = Paths.get(storedPath);
            return Files.exists(path);
        } catch (InvalidPathException ex) {
            return false;
        }
    }

    private void showIdPhoto(String title, String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            DialogUtil.showWarning(title, "No file path recorded for this ID side.");
            return;
        }
        Path path;
        try {
            path = Paths.get(storedPath);
        } catch (InvalidPathException ex) {
            DialogUtil.showError(title, "Stored path is invalid.");
            return;
        }
        if (!Files.exists(path)) {
            DialogUtil.showWarning(title, "The referenced ID image could not be found.");
            return;
        }
        try {
            Image image = new Image(path.toUri().toString(), 640, 720, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(520);
            imageView.setSmooth(true);
            Dialog<Void> preview = new Dialog<>();
            FormDialogUtil.applyAppStyles(preview);
            preview.setTitle(title);
            preview.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            preview.getDialogPane().setContent(imageView);
            preview.showAndWait();
        } catch (Exception ex) {
            DialogUtil.showError(title, "Unable to load ID image: " + ex.getMessage());
        }
    }

    private static class ScheduleInput {
        private final LocalDateTime dateTime;
        private final String venue;

        private ScheduleInput(LocalDateTime dateTime, String venue) {
            this.dateTime = dateTime;
            this.venue = venue;
        }

        private LocalDateTime dateTime() {
            return dateTime;
        }

        private String venue() {
            return venue;
        }
    }
}
