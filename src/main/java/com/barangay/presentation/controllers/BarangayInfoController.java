package com.barangay.presentation.controllers;

import com.barangay.application.dto.UpdateBarangayInfoInputDto;
import com.barangay.domain.entities.BarangayInfo;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.util.DialogUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;

/**
 * Module controller for viewing static barangay information.
 */
public class BarangayInfoController implements ModuleController {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    @FXML
    private TextField barangayNameField;

    @FXML
    private TextField cityField;

    @FXML
    private TextField provinceField;

    @FXML
    private TextField regionField;

    @FXML
    private TextArea addressField;

    @FXML
    private TextField contactField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField sealField;

    @FXML
    private Label lastUpdatedLabel;

    @FXML
    private Label viewOnlyLabel;

    @FXML
    private Label statusMessageLabel;

    @FXML
    private Button saveButton;

    private DIContainer container;
    private MainLayoutController mainLayoutController;
    private boolean canEdit;

    @Override
    public void init(DIContainer container, MainLayoutController mainLayoutController) {
        this.container = container;
        this.mainLayoutController = mainLayoutController;
        configureFormAccess();
        refresh();
    }

    @Override
    public void refresh() {
        clearStatusMessage();
        loadBarangayInfo();
    }

    private void loadBarangayInfo() {
        try {
            BarangayInfo info = container.getGetBarangayInfoUseCase().execute();
            if (info == null) {
                renderEmpty("No barangay information configured");
                return;
            }
            populateFields(info);
        } catch (Exception ex) {
            renderEmpty("Unable to load barangay information");
            DialogUtil.showError("Barangay Information", ex.getMessage());
        }
    }

    private void populateFields(BarangayInfo info) {
        setText(barangayNameField, info.getBarangayName());
        setText(cityField, info.getCity());
        setText(provinceField, info.getProvince());
        setText(regionField, info.getRegion());
        setText(addressField, info.getAddress());
        setText(contactField, info.getContactNumber());
        setText(emailField, info.getEmail());
        setText(sealField, info.getSealPath());

        if (lastUpdatedLabel != null) {
            if (info.getUpdatedAt() != null) {
                lastUpdatedLabel.setText("Last updated " + DATE_TIME_FORMAT.format(info.getUpdatedAt()));
            } else {
                lastUpdatedLabel.setText("Updates not yet recorded");
            }
        }
    }

    private void renderEmpty(String statusText) {
        setText(barangayNameField, "");
        setText(cityField, "");
        setText(provinceField, "");
        setText(regionField, "");
        setText(addressField, "");
        setText(contactField, "");
        setText(emailField, "");
        setText(sealField, "");
        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText(statusText);
        }
    }

    private void setText(TextField field, String value) {
        if (field != null) {
            field.setText(value == null ? "" : value);
        }
    }

    private void setText(TextArea area, String value) {
        if (area != null) {
            area.setText(value == null ? "" : value);
        }
    }

    @FXML
    private void handleSave() {
        clearStatusMessage();

        if (!canEdit) {
            DialogUtil.showWarning("Barangay Information", "Only administrators can update barangay information.");
            return;
        }

        UpdateBarangayInfoInputDto input = new UpdateBarangayInfoInputDto(
                barangayNameField.getText(),
                cityField.getText(),
                provinceField.getText(),
                regionField.getText(),
                addressField.getText(),
                contactField.getText(),
                emailField.getText(),
                sealField.getText());

        try {
            BarangayInfo updated = container.getUpdateBarangayInfoUseCase().execute(input);
            populateFields(updated);
            showStatusMessage("Barangay information updated successfully.", false);
            if (mainLayoutController != null) {
                mainLayoutController.refreshBarangayDetails();
            }
        } catch (Exception ex) {
            showStatusMessage("Failed to save changes.", true);
            DialogUtil.showError("Barangay Information", ex.getMessage());
        }
    }

    private void configureFormAccess() {
        User currentUser = mainLayoutController != null ? mainLayoutController.getCurrentUser() : null;
        UserRole role = currentUser != null ? currentUser.getRole() : null;
        this.canEdit = role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN;
        setEditable(barangayNameField, canEdit);
        setEditable(cityField, canEdit);
        setEditable(provinceField, canEdit);
        setEditable(regionField, canEdit);
        setEditable(addressField, canEdit);
        setEditable(contactField, canEdit);
        setEditable(emailField, canEdit);
        setEditable(sealField, canEdit);
        if (saveButton != null) {
            saveButton.setDisable(!canEdit);
            saveButton.setVisible(canEdit);
            saveButton.setManaged(canEdit);
        }
        if (viewOnlyLabel != null) {
            viewOnlyLabel.setVisible(!canEdit);
            viewOnlyLabel.setManaged(!canEdit);
        }
    }

    private void setEditable(TextField field, boolean editable) {
        if (field != null) {
            field.setEditable(editable);
            field.setFocusTraversable(editable);
        }
    }

    private void setEditable(TextArea area, boolean editable) {
        if (area != null) {
            area.setEditable(editable);
            area.setFocusTraversable(editable);
        }
    }

    private void clearStatusMessage() {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText("");
            statusMessageLabel.getStyleClass().removeAll("status-error", "status-success");
        }
    }

    private void showStatusMessage(String message, boolean isError) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
            statusMessageLabel.getStyleClass().removeAll("status-error", "status-success");
            statusMessageLabel.getStyleClass().add(isError ? "status-error" : "status-success");
        }
    }
}
