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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private javafx.scene.layout.HBox filterBar;

    @FXML
    private Label residentInfoLabel;

    @FXML
    private javafx.scene.control.Button issueDocumentButton;

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
    private javafx.scene.control.Button issueFromRequestButton;

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
        typeFilter.getSelectionModel().clearSelection();
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
        Dialog<IssueDocumentInputDto> dialog = new Dialog<>();
        dialog.setTitle("Issue Document");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField residentIdField = new TextField();
        residentIdField.setPromptText("Resident ID (e.g. BR-2024-0000000001)");
        ChoiceBox<DocumentType> documentTypeChoice = new ChoiceBox<>(
                FXCollections.observableArrayList(DocumentType.values()));
        documentTypeChoice.getSelectionModel().selectFirst();
        TextField purposeField = new TextField();
        purposeField.setPromptText("Purpose");
        DatePicker validUntilPicker = new DatePicker();
        validUntilPicker.setPromptText("Valid Until (optional)");
        TextArea additionalInfoArea = new TextArea();
        additionalInfoArea.setPromptText("Additional info (JSON or notes, optional)");
        additionalInfoArea.setPrefRowCount(3);

        int row = 0;
        grid.addRow(row++, new Label("Resident ID"), residentIdField);
        grid.addRow(row++, new Label("Document Type"), documentTypeChoice);
        grid.addRow(row++, new Label("Purpose"), purposeField);
        grid.addRow(row++, new Label("Valid Until"), validUntilPicker);
        grid.addRow(row, new Label("Additional Info"), additionalInfoArea);

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            if (residentIdField.getText() == null || residentIdField.getText().trim().isEmpty()) {
                return Optional.of("Resident ID is required.");
            }
            return Optional.empty();
        }, "Issue Document");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new IssueDocumentInputDto(
                        residentIdField.getText().trim(),
                        documentTypeChoice.getValue(),
                        purposeField.getText(),
                        validUntilPicker.getValue(),
                        additionalInfoArea.getText());
            }
            return null;
        });

        Optional<IssueDocumentInputDto> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                String reference = container.getIssueDocumentUseCase().execute(input);
                DialogUtil.showInfo("Issue Document", "Document issued with reference: " + reference);
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Issue Document", ex.getMessage());
            }
        });
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
        TableCopyUtil.attachCopyContextMenu(documentsTable,
                document -> document != null && document.getReference() != null
                        ? document.getReference().getValue()
                        : null,
                "Copy Document Reference");
    }

    private void configureFilters() {
        typeFilter.setItems(FXCollections.observableArrayList(DocumentType.values()));
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

        Dialog<IssueDocumentInputDto> dialog = new Dialog<>();
        dialog.setTitle("Issue Document from Request");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField residentIdField = new TextField(selected.getResidentId().getValue());
        residentIdField.setDisable(true);
        ChoiceBox<DocumentType> documentTypeChoice = new ChoiceBox<>(
                FXCollections.observableArrayList(DocumentType.values()));
        documentTypeChoice.getSelectionModel().select(selected.getDocumentType());
        documentTypeChoice.setDisable(true);
        TextField purposeField = new TextField(optionalString(selected.getPurpose()));
        DatePicker validUntilPicker = new DatePicker(selected.getRequestedValidUntil());
        TextArea additionalInfoArea = new TextArea(optionalString(selected.getAdditionalInfo()));
        additionalInfoArea.setPrefRowCount(3);

        int row = 0;
        grid.addRow(row++, new Label("Resident ID"), residentIdField);
        grid.addRow(row++, new Label("Document Type"), documentTypeChoice);
        grid.addRow(row++, new Label("Purpose"), purposeField);
        grid.addRow(row++, new Label("Valid Until"), validUntilPicker);
        grid.addRow(row, new Label("Additional Info"), additionalInfoArea);

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            if (purposeField.getText() == null || purposeField.getText().trim().isEmpty()) {
                return Optional.of("Purpose is required.");
            }
            return Optional.empty();
        }, "Issue Document");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new IssueDocumentInputDto(
                        residentIdField.getText().trim(),
                        documentTypeChoice.getValue(),
                        purposeField.getText().trim(),
                        validUntilPicker.getValue(),
                        additionalInfoArea.getText(),
                        selected.getId());
            }
            return null;
        });

        Optional<IssueDocumentInputDto> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                String reference = container.getIssueDocumentUseCase().execute(input);
                DialogUtil.showInfo("Issue Document", "Document issued with reference: " + reference);
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Issue Document", ex.getMessage());
            }
        });
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
    }

    private void setNodeVisible(javafx.scene.Node node, boolean visible) {
        if (node == null) {
            return;
        }
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
