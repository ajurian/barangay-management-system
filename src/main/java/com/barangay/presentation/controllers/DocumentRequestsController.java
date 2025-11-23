package com.barangay.presentation.controllers;

import com.barangay.application.dto.SubmitDocumentRequestInputDto;
import com.barangay.application.dto.UpdateDocumentRequestStatusInputDto;
import com.barangay.application.usecases.GetDocumentRequestUseCase;
import com.barangay.application.usecases.ListDocumentRequestsUseCase;
import com.barangay.application.usecases.SubmitDocumentRequestUseCase;
import com.barangay.application.usecases.UpdateDocumentRequestStatusUseCase;
import com.barangay.domain.entities.DocumentRequest;
import com.barangay.domain.entities.DocumentRequestStatus;
import com.barangay.domain.entities.DocumentType;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.UserRole;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.util.DialogUtil;
import com.barangay.presentation.util.FormDialogUtil;
import com.barangay.presentation.util.FormFieldIndicator;
import com.barangay.presentation.util.TableCopyUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.util.StringConverter;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Controller for Module 5: Document Requests.
 */
public class DocumentRequestsController implements ModuleController {

    @FXML
    private TableView<DocumentRequest> requestsTable;

    @FXML
    private TableColumn<DocumentRequest, String> requestIdColumn;

    @FXML
    private TableColumn<DocumentRequest, String> residentColumn;

    @FXML
    private TableColumn<DocumentRequest, DocumentType> documentTypeColumn;

    @FXML
    private TableColumn<DocumentRequest, String> purposeColumn;

    @FXML
    private TableColumn<DocumentRequest, DocumentRequestStatus> statusColumn;

    @FXML
    private TableColumn<DocumentRequest, String> createdColumn;

    @FXML
    private TableColumn<DocumentRequest, String> updatedColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<DocumentRequestStatus> statusFilter;

    @FXML
    private Label requestCountLabel;

    @FXML
    private Label residentInfoLabel;

    @FXML
    private Button newRequestButton;

    @FXML
    private Button markUnderReviewButton;

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private HBox staffActionsBox;

    @FXML
    private HBox residentActionsBox;

    @FXML
    private HBox filterBar;

    private final ObservableList<DocumentRequest> backingList = FXCollections.observableArrayList();

    private DIContainer container;
    private boolean residentMode;
    private ResidentId currentResidentId;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    @Override
    public void init(DIContainer container, MainLayoutController mainLayoutController) {
        this.container = container;
        this.residentMode = mainLayoutController.getCurrentUser().getRole() == UserRole.RESIDENT;
        this.currentResidentId = mainLayoutController.getCurrentUser().getLinkedResidentId();
        configureTable();
        configureFilters();
        configureRoleView();
    }

    @Override
    public void refresh() {
        loadRequests();
    }

    @FXML
    private void handleApplyFilters() {
        loadRequests();
    }

    @FXML
    private void handleClearFilters() {
        if (!residentMode && searchField != null) {
            searchField.clear();
        }
        statusFilter.getSelectionModel().selectFirst();
        loadRequests();
    }

    @FXML
    private void handleRefresh() {
        loadRequests();
    }

    @FXML
    private void handleNewRequest() {
        if (!residentMode) {
            DialogUtil.showInfo("Document Requests", "Only resident accounts can submit new requests.");
            return;
        }
        if (currentResidentId == null) {
            DialogUtil.showWarning("Document Requests", "Your account is not linked to a resident profile.");
            return;
        }

        Dialog<SubmitDocumentRequestInputDto> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle("Request Document");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ChoiceBox<DocumentType> typeChoice = new ChoiceBox<>(FXCollections.observableArrayList(DocumentType.values()));
        typeChoice.getSelectionModel().selectFirst();
        TextArea purposeArea = new TextArea();
        purposeArea.setPromptText("Purpose of the document");
        purposeArea.setPrefRowCount(2);
        DatePicker validityPicker = new DatePicker();
        validityPicker.setPromptText("Requested validity date (optional)");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes for barangay staff (optional)");
        notesArea.setPrefRowCount(2);
        TextArea additionalInfoArea = new TextArea();
        additionalInfoArea.setPromptText("Extra information (emergency contact, etc.)");
        additionalInfoArea.setPrefRowCount(2);

        int row = 0;
        grid.addRow(row++, FormFieldIndicator.requiredLabel("Document Type"), typeChoice);
        grid.addRow(row++, FormFieldIndicator.requiredLabel("Purpose"), purposeArea);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Requested Valid Until"), validityPicker);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Notes"), notesArea);
        grid.addRow(row, FormFieldIndicator.optionalLabel("Additional Info"), additionalInfoArea);

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            if (typeChoice.getValue() == null) {
                return Optional.of("Document type is required.");
            }
            if (purposeArea.getText() == null || purposeArea.getText().trim().isEmpty()) {
                return Optional.of("Purpose is required.");
            }
            return Optional.empty();
        }, "Document Requests");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new SubmitDocumentRequestInputDto(
                        typeChoice.getValue(),
                        purposeArea.getText().trim(),
                        validityPicker.getValue(),
                        notesArea.getText(),
                        additionalInfoArea.getText());
            }
            return null;
        });

        Optional<SubmitDocumentRequestInputDto> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                SubmitDocumentRequestUseCase useCase = container.getSubmitDocumentRequestUseCase();
                String requestId = useCase.execute(input);
                DialogUtil.showInfo("Document Requests", "Request submitted. Tracking ID: " + requestId);
                loadRequests();
            } catch (Exception ex) {
                DialogUtil.showError("Document Requests", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleViewDetails() {
        DocumentRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Document Requests", "Select a request first.");
            return;
        }
        GetDocumentRequestUseCase useCase = container.getDocumentRequestUseCase();
        Optional<DocumentRequest> latest = useCase.execute(selected.getId());
        if (latest.isEmpty()) {
            DialogUtil.showWarning("Document Requests", "The request no longer exists.");
            loadRequests();
            return;
        }
        DocumentRequest request = latest.get();
        StringBuilder details = new StringBuilder()
                .append("Request ID: ").append(request.getId()).append('\n')
                .append("Resident: ").append(request.getResidentId().getValue()).append('\n')
                .append("Document Type: ").append(request.getDocumentType()).append('\n')
                .append("Purpose: ").append(optional(request.getPurpose())).append('\n')
                .append("Resident Notes: ").append(optional(request.getResidentNotes())).append('\n')
                .append("Additional Info: ").append(optional(request.getAdditionalInfo())).append('\n')
                .append("Status: ").append(request.getStatus()).append('\n')
                .append("Staff Notes: ").append(optional(request.getStaffNotes())).append('\n')
                .append("Handled By: ").append(optional(request.getHandledBy())).append('\n')
                .append("Linked Document: ").append(optional(request.getLinkedDocumentReference())).append('\n')
                .append("Created: ").append(formatDateTime(request.getCreatedAt())).append('\n')
                .append("Updated: ").append(formatDateTime(request.getUpdatedAt()));
        DialogUtil.showInfo("Request Details", details.toString());
    }

    @FXML
    private void handleMarkUnderReview() {
        updateStatus(DocumentRequestStatus.UNDER_REVIEW, "Mark as Under Review");
    }

    @FXML
    private void handleApprove() {
        updateStatus(DocumentRequestStatus.APPROVED, "Approve Request");
    }

    @FXML
    private void handleReject() {
        updateStatus(DocumentRequestStatus.REJECTED, "Reject Request");
    }

    private void updateStatus(DocumentRequestStatus status, String title) {
        if (residentMode) {
            DialogUtil.showWarning("Document Requests", "Only staff users can change statuses.");
            return;
        }
        DocumentRequest selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Document Requests", "Select a request first.");
            return;
        }

        Dialog<String> notesDialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(notesDialog);
        notesDialog.setTitle(title);
        notesDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Staff notes (optional)");
        notesArea.setPrefRowCount(3);
        notesDialog.getDialogPane().setContent(notesArea);
        notesDialog.setResultConverter(button -> button == ButtonType.OK ? notesArea.getText() : null);

        Optional<String> notesResult = notesDialog.showAndWait();
        if (notesResult.isEmpty()) {
            return;
        }

        try {
            UpdateDocumentRequestStatusUseCase useCase = container.getUpdateDocumentRequestStatusUseCase();
            useCase.execute(new UpdateDocumentRequestStatusInputDto(
                    selected.getId(),
                    status,
                    notesResult.get()));
            DialogUtil.showInfo("Document Requests", "Request status updated.");
            loadRequests();
        } catch (Exception ex) {
            DialogUtil.showError("Document Requests", ex.getMessage());
        }
    }

    private void configureTable() {
        requestIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        residentColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getResidentId().getValue()));
        documentTypeColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDocumentType()));
        purposeColumn.setCellValueFactory(cell -> new SimpleStringProperty(optional(cell.getValue().getPurpose())));
        statusColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getStatus()));
        createdColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                formatDateTime(cell.getValue().getCreatedAt())));
        updatedColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                formatDateTime(cell.getValue().getUpdatedAt())));
        requestsTable.setItems(backingList);
        requestsTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> updateSelectionDependentActions(newVal));
        updateSelectionDependentActions(null);
        TableCopyUtil.attachCopyContextMenu(requestsTable,
                request -> request != null ? request.getId() : null,
                "Copy Request ID");
    }

    private void configureFilters() {
        if (searchField != null) {
            HBox.setHgrow(searchField, Priority.ALWAYS);
            if (!residentMode) {
                searchField.textProperty().addListener((obs, oldVal, newVal) -> loadRequests());
            }
        }
        ObservableList<DocumentRequestStatus> statuses = FXCollections.observableArrayList();
        statuses.add(null);
        statuses.addAll(Arrays.asList(DocumentRequestStatus.values()));
        statusFilter.setItems(statuses);
        statusFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(DocumentRequestStatus status) {
                return status == null ? "All" : status.toString();
            }

            @Override
            public DocumentRequestStatus fromString(String string) {
                return null;
            }
        });
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> loadRequests());
    }

    private void configureRoleView() {
        if (residentMode) {
            setVisible(staffActionsBox, false);
            setVisible(searchField, false);
            if (residentColumn != null) {
                residentColumn.setVisible(false);
            }
            if (residentInfoLabel != null) {
                residentInfoLabel.setVisible(true);
                residentInfoLabel.setManaged(true);
                residentInfoLabel.setText(currentResidentId == null
                        ? "Your account is not yet linked to a resident record."
                        : "Track your online document requests below.");
            }
            if (currentResidentId == null) {
                newRequestButton.setDisable(true);
            }
        } else {
            setVisible(residentActionsBox, false);
            if (residentInfoLabel != null) {
                residentInfoLabel.setVisible(false);
                residentInfoLabel.setManaged(false);
            }
        }
    }

    private void loadRequests() {
        if (residentMode && currentResidentId == null) {
            backingList.clear();
            requestCountLabel.setText("Link your resident profile to submit requests.");
            return;
        }
        ListDocumentRequestsUseCase useCase = container.getListDocumentRequestsUseCase();
        DocumentRequestStatus status = statusFilter.getSelectionModel().getSelectedItem();
        String searchTerm = (!residentMode && searchField != null) ? searchField.getText() : null;
        List<DocumentRequest> requests = useCase.execute(status, searchTerm);
        backingList.setAll(requests);
        requestCountLabel.setText(String.format(residentMode ? "You have %d request(s)" : "%d request(s) found",
                requests.size()));
        if (requestsTable != null) {
            requestsTable.getSelectionModel().clearSelection();
        }
        updateSelectionDependentActions(null);
    }

    private void setVisible(javafx.scene.Node node, boolean visible) {
        if (node == null) {
            return;
        }
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "--";
        }
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    private void updateSelectionDependentActions(DocumentRequest selected) {
        boolean hasSelection = selected != null;
        if (viewDetailsButton != null) {
            viewDetailsButton.setDisable(!hasSelection);
        }
        if (residentMode) {
            return;
        }
        if (markUnderReviewButton != null) {
            markUnderReviewButton.setDisable(!hasSelection);
        }
        if (approveButton != null) {
            approveButton.setDisable(!hasSelection);
        }
        if (rejectButton != null) {
            rejectButton.setDisable(!hasSelection);
        }
    }
}
