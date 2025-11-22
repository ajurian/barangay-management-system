package com.barangay.presentation.controllers;

import com.barangay.application.dto.IssueDocumentInputDto;
import com.barangay.application.usecases.ListDocumentRequestsUseCase;
import com.barangay.application.usecases.SearchDocumentsUseCase;
import com.barangay.domain.entities.Document;
import com.barangay.domain.entities.DocumentRequest;
import com.barangay.domain.entities.DocumentRequestStatus;
import com.barangay.domain.entities.DocumentType;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.UserRole;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.util.DialogUtil;
import com.barangay.presentation.util.FormDialogUtil;
import com.barangay.presentation.util.TableCopyUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Module controller for document issuance and search.
 */
public class DocumentsController implements ModuleController {

    @FXML
    private TableView<Document> documentsTable;

    @FXML
    private TableColumn<Document, String> referenceColumn;

    @FXML
    private TableColumn<Document, String> residentColumn;

    @FXML
    private TableColumn<Document, DocumentType> typeColumn;

    @FXML
    private TableColumn<Document, String> purposeColumn;

    @FXML
    private TableColumn<Document, LocalDate> issuedDateColumn;

    @FXML
    private TableColumn<Document, LocalDate> validUntilColumn;

    @FXML
    private TableColumn<Document, String> issuedByColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<DocumentType> typeFilter;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label documentCountLabel;

    @FXML
    private HBox filterBar;

    @FXML
    private Label residentInfoLabel;

    @FXML
    private Button issueDocumentButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button downloadPhotoButton;

    @FXML
    private Tab fromRequestsTab;

    @FXML
    private TableView<DocumentRequest> requestQueueTable;

    @FXML
    private TableColumn<DocumentRequest, String> queueRequestIdColumn;

    @FXML
    private TableColumn<DocumentRequest, String> queueResidentColumn;

    @FXML
    private TableColumn<DocumentRequest, DocumentType> queueTypeColumn;

    @FXML
    private TableColumn<DocumentRequest, String> queuePurposeColumn;

    @FXML
    private TableColumn<DocumentRequest, DocumentRequestStatus> queueStatusColumn;

    @FXML
    private Label requestQueueLabel;

    @FXML
    private Button issueFromRequestButton;

    private final ObservableList<Document> backingList = FXCollections.observableArrayList();
    private final ObservableList<DocumentRequest> requestQueueList = FXCollections.observableArrayList();

    private DIContainer container;
    private boolean residentMode;
    private ResidentId currentResidentId;

    @Override
    public void init(DIContainer container, MainLayoutController mainLayoutController) {
        this.container = container;
        this.residentMode = mainLayoutController.getCurrentUser().getRole() == UserRole.RESIDENT;
        this.currentResidentId = residentMode ? mainLayoutController.getCurrentUser().getLinkedResidentId() : null;
        configureTable();
        configureRequestQueueTable();
        configureFilters();
        configureRoleView();
    }

    @Override
    public void refresh() {
        if (residentMode) {
            loadResidentDocuments();
            documentsTable.setItems(FXCollections.observableArrayList(backingList));
            documentCountLabel.setText(String.format("You have %d document(s)", backingList.size()));
        } else {
            loadAllDocuments();
            applyFilters();
            loadRequestQueue();
        }
        if (documentsTable != null) {
            documentsTable.getSelectionModel().clearSelection();
        }
        updateDocumentSelectionActions(null);
    }

    @FXML
    private void handleApplyFilters() {
        if (residentMode) {
            return;
        }
        applyFilters();
    }

    @FXML
    private void handleRefreshDocuments() {
        refresh();
    }

    @FXML
    private void handleClearFilters() {
        if (residentMode) {
            return;
        }
        searchField.clear();
        typeFilter.getSelectionModel().selectFirst();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        applyFilters();
    }

    @FXML
    private void handleIssueDocument() {
        if (residentMode) {
            DialogUtil.showInfo("Issue Document", "Residents can only view issued documents.");
            return;
        }
        Optional<IssueDocumentFormResult> result = showIssueDocumentDialog(null);
        result.ifPresent(this::processDocumentIssuance);
    }

    @FXML
    private void handleViewDetails() {
        Document selected = documentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Document Details", "Please select a document first.");
            return;
        }
        String details = String.format(
                "Reference: %s\nResident ID: %s\nType: %s\nPurpose: %s\nIssued: %s\nValid Until: %s\nIssued By: %s\nAdditional Info: %s",
                selected.getReference().getValue(),
                selected.getResidentId().getValue(),
                selected.getType(),
                optionalString(selected.getPurpose()),
                selected.getIssuedDate(),
                selected.getValidUntil(),
                optionalString(selected.getIssuedBy()),
                optionalString(selected.getAdditionalInfo()));
        DialogUtil.showInfo("Document Details", details);
    }

    @FXML
    private void handleDownloadPhoto() {
        Document selected = documentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Download Photo", "Please select a document first.");
            return;
        }
        if (!photoExists(selected.getPhotoPath())) {
            DialogUtil.showWarning("Download Photo", "No uploaded photo found for the selected document.");
            return;
        }
        File destination = choosePhotoDestination(selected);
        if (destination == null) {
            return;
        }
        try {
            copyPhotoTo(selected.getPhotoPath(), destination.toPath());
            DialogUtil.showInfo("Download Photo", "Photo saved to: " + destination.getAbsolutePath());
        } catch (IOException ex) {
            DialogUtil.showError("Download Photo", ex.getMessage());
        }
    }

    private void configureTable() {
        referenceColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getReference().getValue()));
        residentColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getResidentId().getValue()));
        typeColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getType()));
        purposeColumn
                .setCellValueFactory(cell -> new SimpleStringProperty(optionalString(cell.getValue().getPurpose())));
        issuedDateColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getIssuedDate()));
        validUntilColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getValidUntil()));
        issuedByColumn
                .setCellValueFactory(cell -> new SimpleStringProperty(optionalString(cell.getValue().getIssuedBy())));
        documentsTable.setItems(FXCollections.observableArrayList());
        documentsTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> updateDocumentSelectionActions(newVal));
        updateDocumentSelectionActions(null);
        TableCopyUtil.attachCopyContextMenu(documentsTable,
                document -> document != null && document.getReference() != null
                        ? document.getReference().getValue()
                        : null,
                "Copy Document Reference");
    }

    private void configureFilters() {
        ObservableList<DocumentType> items = FXCollections.observableArrayList();
        items.add(null);
        items.addAll(Arrays.asList(DocumentType.values()));
        typeFilter.setItems(items);
        typeFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(DocumentType type) {
                return type == null ? "All" : type.toString();
            }

            @Override
            public DocumentType fromString(String string) {
                return null;
            }
        });
        typeFilter.getSelectionModel().selectFirst();
    }

    private void configureRequestQueueTable() {
        if (requestQueueTable == null) {
            return;
        }
        queueRequestIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        queueResidentColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getResidentId().getValue()));
        queueTypeColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDocumentType()));
        queuePurposeColumn
                .setCellValueFactory(cell -> new SimpleStringProperty(optionalString(cell.getValue().getPurpose())));
        queueStatusColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStatus()));
        requestQueueTable.setItems(requestQueueList);
        requestQueueTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> updateRequestQueueButtonState(newVal));
        updateRequestQueueButtonState(null);
    }

    private void loadAllDocuments() {
        SearchDocumentsUseCase searchDocumentsUseCase = container.getSearchDocumentsUseCase();
        List<Document> results = new ArrayList<>(searchDocumentsUseCase.search(""));
        backingList.setAll(results);
    }

    private void loadResidentDocuments() {
        if (currentResidentId == null) {
            backingList.clear();
            if (residentInfoLabel != null) {
                residentInfoLabel.setText(
                        "Your account is not yet linked to a resident profile. Please contact the barangay office.");
                residentInfoLabel.setManaged(true);
                residentInfoLabel.setVisible(true);
            }
            return;
        }
        List<Document> docs = container.getDocumentRepository().findByResidentId(currentResidentId);
        backingList.setAll(docs);
        if (residentInfoLabel != null) {
            residentInfoLabel
                    .setText("Below are the documents issued to you. Visit the office for reprints if needed.");
            residentInfoLabel.setManaged(true);
            residentInfoLabel.setVisible(true);
        }
    }

    private void applyFilters() {
        String term = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        DocumentType type = typeFilter.getSelectionModel().getSelectedItem();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        List<Document> filtered = backingList.stream()
                .filter(doc -> type == null || doc.getType() == type)
                .filter(doc -> term.isEmpty() ||
                        doc.getReference().getValue().toLowerCase().contains(term) ||
                        doc.getResidentId().getValue().toLowerCase().contains(term) ||
                        (doc.getPurpose() != null && doc.getPurpose().toLowerCase().contains(term)))
                .filter(doc -> {
                    LocalDate issued = doc.getIssuedDate();
                    if (start != null && issued.isBefore(start)) {
                        return false;
                    }
                    if (end != null && issued.isAfter(end)) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        documentsTable.setItems(FXCollections.observableArrayList(filtered));
        documentCountLabel.setText(String.format("Showing %d of %d documents", filtered.size(), backingList.size()));
    }

    private String optionalString(String value) {
        return value == null || value.isEmpty() ? "N/A" : value;
    }

    private void configureRoleView() {
        if (residentMode) {
            setNodeVisible(filterBar, false);
            setNodeVisible(issueDocumentButton, false);
            if (fromRequestsTab != null) {
                fromRequestsTab.setDisable(true);
                fromRequestsTab.setText("From Requests (staff only)");
            }
            if (residentInfoLabel != null) {
                residentInfoLabel.setManaged(true);
                residentInfoLabel.setVisible(true);
            }
        } else if (residentInfoLabel != null) {
            residentInfoLabel.setVisible(false);
            residentInfoLabel.setManaged(false);
        }
    }

    @FXML
    private void handleIssueFromRequest() {
        if (residentMode) {
            DialogUtil.showWarning("Document Issuance", "Residents cannot issue documents.");
            return;
        }
        if (requestQueueTable == null) {
            DialogUtil.showWarning("Document Issuance", "Request queue is not available.");
            return;
        }
        DocumentRequest selected = requestQueueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("From Requests", "Select a request to issue.");
            return;
        }
        if (selected.getStatus() != DocumentRequestStatus.APPROVED) {
            DialogUtil.showWarning("From Requests", "Only approved requests can be issued from this tab.");
            return;
        }
        Optional<IssueDocumentFormResult> result = showIssueDocumentDialog(selected);
        result.ifPresent(this::processDocumentIssuance);
    }

    @FXML
    private void handleRefreshQueue() {
        loadRequestQueue();
    }

    private void loadRequestQueue() {
        if (residentMode || requestQueueTable == null) {
            return;
        }
        ListDocumentRequestsUseCase requestUseCase = container.getListDocumentRequestsUseCase();
        List<DocumentRequest> requests = requestUseCase.execute(DocumentRequestStatus.APPROVED, null);
        requestQueueList.setAll(requests);
        if (requestQueueLabel != null) {
            requestQueueLabel.setText(String.format("%d approved request(s) ready for issuance", requests.size()));
        }
        if (requestQueueTable != null) {
            requestQueueTable.getSelectionModel().clearSelection();
        }
        updateRequestQueueButtonState(null);
    }

    private void setNodeVisible(javafx.scene.Node node, boolean visible) {
        if (node == null) {
            return;
        }
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void updateDocumentSelectionActions(Document selected) {
        boolean hasSelection = selected != null;
        if (viewDetailsButton != null) {
            viewDetailsButton.setDisable(!hasSelection);
        }
        if (downloadPhotoButton == null) {
            return;
        }
        boolean canDownload = hasSelection && selected.hasPhoto()
            && photoExists(selected.getPhotoPath());
        downloadPhotoButton.setDisable(!canDownload);
    }

    private void updateRequestQueueButtonState(DocumentRequest selected) {
        if (issueFromRequestButton == null) {
            return;
        }
        boolean enable = selected != null && selected.getStatus() == DocumentRequestStatus.APPROVED;
        issueFromRequestButton.setDisable(!enable);
    }

    private Optional<IssueDocumentFormResult> showIssueDocumentDialog(DocumentRequest request) {
        Dialog<IssueDocumentFormResult> dialog = new Dialog<>();
        dialog.setTitle(request == null ? "Issue Document" : "Issue Document from Request");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField residentIdField = new TextField(request == null ? "" : request.getResidentId().getValue());
        residentIdField.setPromptText("Resident ID (e.g. BR-2024-0000000001)");
        residentIdField.setDisable(request != null);

        ChoiceBox<DocumentType> documentTypeChoice = new ChoiceBox<>(
                FXCollections.observableArrayList(DocumentType.values()));
        if (request != null) {
            documentTypeChoice.getSelectionModel().select(request.getDocumentType());
            documentTypeChoice.setDisable(true);
        } else {
            documentTypeChoice.getSelectionModel().selectFirst();
        }

        TextField purposeField = new TextField(request == null ? "" : orEmpty(request.getPurpose()));
        DatePicker validUntilPicker = new DatePicker(request == null ? null : request.getRequestedValidUntil());
        TextArea additionalInfoArea = new TextArea(request == null ? "" : orEmpty(request.getAdditionalInfo()));
        additionalInfoArea.setPrefRowCount(3);

        TextField photoField = new TextField();
        photoField.setPromptText("No file selected");
        photoField.setEditable(false);
        photoField.setPrefWidth(220);
        Button browsePhotoButton = new Button("Select Photo");
        File[] selectedPhoto = new File[1];
        browsePhotoButton.setOnAction(evt -> {
            Window owner = dialog.getDialogPane().getScene() != null
                    ? dialog.getDialogPane().getScene().getWindow()
                    : null;
            File chosen = chooseImageFile(owner);
            if (chosen != null) {
                selectedPhoto[0] = chosen;
                photoField.setText(chosen.getName());
            }
        });
        HBox photoInput = new HBox(8, photoField, browsePhotoButton);

        int row = 0;
        grid.addRow(row++, new Label("Resident ID"), residentIdField);
        grid.addRow(row++, new Label("Document Type"), documentTypeChoice);
        grid.addRow(row++, new Label("Purpose"), purposeField);
        grid.addRow(row++, new Label("Valid Until"), validUntilPicker);
        grid.addRow(row++, new Label("Additional Info"), additionalInfoArea);
        grid.addRow(row, new Label("Document Photo"), photoInput);

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            if (request == null) {
                if (residentIdField.getText() == null || residentIdField.getText().trim().isEmpty()) {
                    return Optional.of("Resident ID is required.");
                }
            } else {
                if (purposeField.getText() == null || purposeField.getText().trim().isEmpty()) {
                    return Optional.of("Purpose is required.");
                }
            }
            return Optional.empty();
        }, dialog.getTitle());

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            IssueDocumentInputDto input = new IssueDocumentInputDto(
                    residentIdField.getText().trim(),
                    documentTypeChoice.getValue(),
                    request == null ? purposeField.getText() : purposeField.getText().trim(),
                    validUntilPicker.getValue(),
                    additionalInfoArea.getText(),
                    request != null ? request.getId() : null);
            return new IssueDocumentFormResult(input, selectedPhoto[0]);
        });

        return dialog.showAndWait();
    }

    private void processDocumentIssuance(IssueDocumentFormResult result) {
        IssueDocumentInputDto baseInput = result.getInput();
        String storedPhotoPath = null;
        if (result.getPhotoFile() != null) {
            try {
                storedPhotoPath = capturePhotoPath(result.getPhotoFile());
            } catch (IOException ex) {
                DialogUtil.showError("Issue Document", "Unable to store document photo: " + ex.getMessage());
                return;
            }
        }

        IssueDocumentInputDto payload = new IssueDocumentInputDto(
                baseInput.getResidentId(),
                baseInput.getDocumentType(),
                baseInput.getPurpose(),
                baseInput.getValidUntil(),
                baseInput.getAdditionalInfo(),
                baseInput.getRequestId(),
                storedPhotoPath);

        try {
            String reference = container.getIssueDocumentUseCase().execute(payload);
            DialogUtil.showInfo("Issue Document", "Document issued with reference: " + reference);
            refresh();
        } catch (Exception ex) {
            DialogUtil.showError("Issue Document", ex.getMessage());
        }
    }

    private File chooseImageFile(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Document Photo");
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        return chooser.showOpenDialog(owner);
    }

    private File choosePhotoDestination(Document document) {
        Path sourcePath = resolvePhotoPath(document.getPhotoPath());
        if (sourcePath == null || !Files.exists(sourcePath)) {
            return null;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Document Photo");
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        String extension = getExtension(sourcePath.getFileName().toString()).orElse(".png");
        chooser.setInitialFileName(document.getReference().getValue() + extension);
        Window owner = documentsTable.getScene() != null ? documentsTable.getScene().getWindow() : null;
        return chooser.showSaveDialog(owner);
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean photoExists(String storedPath) {
        Path path = resolvePhotoPath(storedPath);
        return path != null && Files.exists(path);
    }

    private static void copyPhotoTo(String storedPath, Path destination) throws IOException {
        Path source = resolvePhotoPath(storedPath);
        if (source == null || !Files.exists(source)) {
            throw new FileNotFoundException("Document photo not found");
        }
        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private static Path resolvePhotoPath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        try {
            return Paths.get(storedPath);
        } catch (InvalidPathException ex) {
            return null;
        }
    }

    private static String capturePhotoPath(File sourceFile) throws IOException {
        if (sourceFile == null) {
            return null;
        }
        if (!sourceFile.exists()) {
            throw new FileNotFoundException("Selected photo no longer exists");
        }
        return sourceFile.getAbsolutePath();
    }

    private static Optional<String> getExtension(String fileName) {
        if (fileName == null) {
            return Optional.empty();
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return Optional.empty();
        }
        return Optional.of(fileName.substring(idx));
    }

    private static class IssueDocumentFormResult {
        private final IssueDocumentInputDto input;
        private final File photoFile;

        IssueDocumentFormResult(IssueDocumentInputDto input, File photoFile) {
            this.input = input;
            this.photoFile = photoFile;
        }

        IssueDocumentInputDto getInput() {
            return input;
        }

        File getPhotoFile() {
            return photoFile;
        }
    }
}
