package com.barangay.presentation.controllers;

import com.barangay.application.dto.UpdateBarangayInfoInputDto;
import com.barangay.domain.entities.BarangayInfo;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.util.DialogUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

/**
 * Module controller for viewing static barangay information.
 */
public class BarangayInfoController implements ModuleController {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg");

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
    private Button browseSealButton;

    @FXML
    private Label sealLabel;

    @FXML
    private HBox sealFieldContainer;

    @FXML
    private Label lastUpdatedLabel;

    @FXML
    private Label viewOnlyLabel;

    @FXML
    private Label statusMessageLabel;

    @FXML
    private Button saveButton;

    @FXML
    private TextField dashboardImageField;

    @FXML
    private Button browseDashboardImageButton;

    @FXML
    private Button addDashboardImageButton;

    @FXML
    private Button removeDashboardImageButton;

    @FXML
    private ListView<String> dashboardImagesListView;

    private DIContainer container;
    private MainLayoutController mainLayoutController;
    private boolean canEdit;
    private final ObservableList<String> dashboardImages = FXCollections.observableArrayList();

    @Override
    public void init(DIContainer container, MainLayoutController mainLayoutController) {
        this.container = container;
        this.mainLayoutController = mainLayoutController;
        if (dashboardImagesListView != null) {
            dashboardImagesListView.setItems(dashboardImages);
        }
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
        dashboardImages.setAll(info.getDashboardImages());

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
        dashboardImages.clear();
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
            sealField.getText(),
            new ArrayList<>(dashboardImages));

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
        setSealFieldVisibility(canEdit);
        setDashboardImageControlsEnabled(canEdit);
        if (saveButton != null) {
            saveButton.setDisable(!canEdit);
            saveButton.setVisible(canEdit);
            saveButton.setManaged(canEdit);
        }
        if (browseSealButton != null) {
            browseSealButton.setDisable(!canEdit);
            browseSealButton.setVisible(canEdit);
            browseSealButton.setManaged(canEdit);
        }
        if (viewOnlyLabel != null) {
            viewOnlyLabel.setVisible(!canEdit);
            viewOnlyLabel.setManaged(!canEdit);
        }
    }

    private void setSealFieldVisibility(boolean visible) {
        if (sealLabel != null) {
            sealLabel.setVisible(visible);
            sealLabel.setManaged(visible);
        }
        if (sealFieldContainer != null) {
            sealFieldContainer.setVisible(visible);
            sealFieldContainer.setManaged(visible);
        }
    }

    private void setDashboardImageControlsEnabled(boolean enabled) {
        if (dashboardImageField != null) {
            dashboardImageField.setEditable(enabled);
            dashboardImageField.setDisable(!enabled);
        }
        if (browseDashboardImageButton != null) {
            browseDashboardImageButton.setDisable(!enabled);
            browseDashboardImageButton.setVisible(enabled);
            browseDashboardImageButton.setManaged(enabled);
        }
        if (addDashboardImageButton != null) {
            addDashboardImageButton.setDisable(!enabled);
            addDashboardImageButton.setVisible(enabled);
            addDashboardImageButton.setManaged(enabled);
        }
        if (removeDashboardImageButton != null) {
            removeDashboardImageButton.setDisable(!enabled);
            removeDashboardImageButton.setVisible(enabled);
            removeDashboardImageButton.setManaged(enabled);
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

    @FXML
    private void handleBrowseSeal() {
        if (!canEdit) {
            DialogUtil.showWarning("Barangay Information", "Only administrators can update barangay information.");
            return;
        }

        Window owner = resolveOwnerWindow(browseSealButton);

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Barangay Seal");
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File selected = chooser.showOpenDialog(owner);
        if (selected != null) {
            sealField.setText(selected.getAbsolutePath());
            clearStatusMessage();
        }
    }

    @FXML
    private void handleBrowseDashboardImage() {
        if (!canEdit) {
            DialogUtil.showWarning("Barangay Information", "Only administrators can update barangay information.");
            return;
        }

        Window owner = resolveOwnerWindow(browseDashboardImageButton);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Dashboard Image");
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File selected = chooser.showOpenDialog(owner);
        if (selected != null && dashboardImageField != null) {
            dashboardImageField.setText(selected.getAbsolutePath());
            clearStatusMessage();
        }
    }

    @FXML
    private void handleAddDashboardImage() {
        if (!canEdit) {
            DialogUtil.showWarning("Barangay Information", "Only administrators can update barangay information.");
            return;
        }
        if (dashboardImageField == null) {
            return;
        }
        String normalized = normalizePath(dashboardImageField.getText());
        if (normalized == null) {
            showStatusMessage("Enter a valid image path or URL.", true);
            return;
        }
        if (!isAllowedImageFile(normalized)) {
            showStatusMessage("Select an existing .png, .jpg, or .jpeg file.", true);
            return;
        }
        if (dashboardImages.contains(normalized)) {
            showStatusMessage("Image is already in the carousel.", true);
            return;
        }
        dashboardImages.add(normalized);
        dashboardImageField.clear();
        clearStatusMessage();
    }

    @FXML
    private void handleRemoveDashboardImage() {
        if (!canEdit) {
            DialogUtil.showWarning("Barangay Information", "Only administrators can update barangay information.");
            return;
        }
        if (dashboardImagesListView == null) {
            return;
        }
        String selected = dashboardImagesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatusMessage("Select an image to remove.", true);
            return;
        }
        dashboardImages.remove(selected);
        clearStatusMessage();
    }

    private Window resolveOwnerWindow(Button button) {
        if (button != null && button.getScene() != null) {
            return button.getScene().getWindow();
        }
        return null;
    }

    private String normalizePath(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isAllowedImageFile(String value) {
        try {
            Path path = Path.of(value);
            if (!Files.exists(path) || Files.isDirectory(path)) {
                return false;
            }
            String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
            return ALLOWED_IMAGE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
        } catch (Exception ex) {
            return false;
        }
    }
}
