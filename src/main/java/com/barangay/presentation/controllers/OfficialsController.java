package com.barangay.presentation.controllers;

import com.barangay.application.dto.RegisterOfficialInputDto;
import com.barangay.application.dto.UpdateOfficialInputDto;
import com.barangay.domain.entities.BarangayOfficial;
import com.barangay.domain.entities.OfficialPosition;
import com.barangay.domain.entities.UserRole;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.util.DialogUtil;
import com.barangay.presentation.util.FormDialogUtil;
import com.barangay.presentation.util.FormFieldIndicator;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Module controller for managing barangay officials.
 */
public class OfficialsController implements ModuleController {

    private static final String ALL_LABEL = "All";

    @FXML
    private TableView<BarangayOfficial> officialsTable;

    @FXML
    private TableColumn<BarangayOfficial, String> nameColumn;

    @FXML
    private TableColumn<BarangayOfficial, OfficialPosition> positionColumn;

    @FXML
    private TableColumn<BarangayOfficial, String> residentColumn;

    @FXML
    private TableColumn<BarangayOfficial, LocalDate> termStartColumn;

    @FXML
    private TableColumn<BarangayOfficial, LocalDate> termEndColumn;

    @FXML
    private TableColumn<BarangayOfficial, Boolean> currentColumn;

    @FXML
    private ChoiceBox<OfficialPosition> positionFilter;

    @FXML
    private ChoiceBox<String> currentFilter;

    @FXML
    private TextField searchField;

    @FXML
    private Label officialCountLabel;

    @FXML
    private Label viewerInfoLabel;

    @FXML
    private HBox managementActionsBox;

    @FXML
    private Button viewPhotoButton;

    @FXML
    private Button updatePhotoButton;

    @FXML
    private Button updateTermButton;

    @FXML
    private Button endTermButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button refreshButton;

    private final ObservableList<BarangayOfficial> backingList = FXCollections.observableArrayList();

    private DIContainer container;
    private boolean canManageOfficials;

    @Override
    public void init(DIContainer container, MainLayoutController mainLayoutController) {
        this.container = container;
        UserRole role = mainLayoutController.getCurrentUser().getRole();
        this.canManageOfficials = role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN;
        configureTable();
        configureFilters();
        configureRoleView();
        refresh();
    }

    @Override
    public void refresh() {
        loadOfficials();
        applyFilters();
    }

    @FXML
    private void handleApplyFilters() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        positionFilter.getSelectionModel().selectFirst();
        currentFilter.getSelectionModel().selectFirst();
        applyFilters();
    }

    @FXML
    private void handleRefresh() {
        refresh();
    }

    @FXML
    private void handleRegisterOfficial() {
        if (!canManageOfficials) {
            DialogUtil.showWarning("Register Official", "Only administrators can register officials.");
            return;
        }
        Optional<RegisterOfficialInputDto> result = showRegisterDialog();
        result.ifPresent(input -> {
            try {
                container.getRegisterOfficialUseCase().execute(input);
                DialogUtil.showInfo("Register Official", "Official registered successfully.");
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Register Official", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleUpdateOfficial() {
        if (!canManageOfficials) {
            DialogUtil.showWarning("Update Official", "Only administrators can update terms.");
            return;
        }
        BarangayOfficial selected = getSelectedOfficial();
        if (selected == null) {
            return;
        }
        Optional<UpdateOfficialInputDto> result = showUpdateDialog(selected);
        result.ifPresent(input -> {
            try {
                container.getUpdateOfficialUseCase().execute(input);
                DialogUtil.showInfo("Update Official", "Official updated successfully.");
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Update Official", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleEndTerm() {
        if (!canManageOfficials) {
            DialogUtil.showWarning("End Term", "Only administrators can end terms.");
            return;
        }
        BarangayOfficial selected = getSelectedOfficial();
        if (selected == null) {
            return;
        }
        if (!selected.isCurrent()) {
            DialogUtil.showWarning("End Term", "Selected official is not currently serving.");
            return;
        }
        if (!DialogUtil.showConfirmation("End Term",
                "Mark the term of " + selected.getOfficialName() + " as finished?")) {
            return;
        }
        try {
            container.getEndTermUseCase().execute(selected.getOfficialId());
            DialogUtil.showInfo("End Term", "Official term updated.");
            refresh();
        } catch (Exception ex) {
            DialogUtil.showError("End Term", ex.getMessage());
        }
    }

    @FXML
    private void handleViewPhoto() {
        BarangayOfficial selected = getSelectedOfficial();
        if (selected == null) {
            return;
        }
        Optional<File> photo = resolvePhotoFile(selected);
        if (photo.isEmpty()) {
            DialogUtil.showWarning("View Photo", "No available photo for the selected official. Please upload again.");
            return;
        }
        Image image;
        try {
            image = new Image(photo.get().toURI().toString(), 400, 480, true, true);
        } catch (Exception ex) {
            DialogUtil.showError("View Photo", "Unable to load photo: " + ex.getMessage());
            return;
        }
        ImageView preview = new ImageView(image);
        preview.setPreserveRatio(true);
        preview.setFitWidth(360);
        preview.setSmooth(true);

        Dialog<Void> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle("Official Photo");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setContent(preview);
        dialog.showAndWait();
    }

    @FXML
    private void handleUpdatePhoto() {
        if (!canManageOfficials) {
            DialogUtil.showWarning("Update Photo", "Only administrators can update photos.");
            return;
        }
        BarangayOfficial selected = getSelectedOfficial();
        if (selected == null) {
            return;
        }
        FileChooser chooser = createImageChooser("Select Official Photo");
        Window owner = officialsTable.getScene() != null ? officialsTable.getScene().getWindow() : null;
        File chosen = chooser.showOpenDialog(owner);
        if (chosen == null) {
            return;
        }
        try {
            container.getUpdateOfficialPhotoUseCase().execute(selected.getOfficialId(), chosen.getAbsolutePath());
            selected.setPhotoPath(chosen.getAbsolutePath());
            DialogUtil.showInfo("Update Photo", "Photo updated successfully.");
            updateSelectionDependentActions(selected);
        } catch (Exception ex) {
            DialogUtil.showError("Update Photo", ex.getMessage());
        }
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(Optional.ofNullable(cell.getValue().getOfficialName()).orElse("--")));
        positionColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPosition()));
        residentColumn
                .setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getResidentId().getValue()));
        termStartColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTermStart()));
        termEndColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTermEnd()));
        currentColumn.setCellValueFactory(cell -> new SimpleBooleanProperty(cell.getValue().isCurrent()));

        officialsTable.setItems(FXCollections.observableArrayList());
        officialsTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> updateSelectionDependentActions(newVal));
        updateSelectionDependentActions(null);
    }

    private void configureFilters() {
        ObservableList<OfficialPosition> positions = FXCollections.observableArrayList();
        positions.add(null);
        positions.addAll(Arrays.asList(OfficialPosition.values()));
        positionFilter.setItems(positions);
        positionFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(OfficialPosition position) {
                return position == null ? ALL_LABEL : position.toString();
            }

            @Override
            public OfficialPosition fromString(String string) {
                return null;
            }
        });
        positionFilter.getSelectionModel().selectFirst();

        currentFilter.setItems(FXCollections.observableArrayList(ALL_LABEL, "Current", "Former"));
        currentFilter.getSelectionModel().selectFirst();

        positionFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        currentFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadOfficials() {
        List<BarangayOfficial> officials = container.getListOfficialsUseCase().getAllOfficials()
                .stream()
                .sorted(Comparator.comparing(BarangayOfficial::getTermStart).reversed())
                .collect(Collectors.toList());
        backingList.setAll(officials);
    }

    private void applyFilters() {
        String term = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        OfficialPosition position = positionFilter.getSelectionModel().getSelectedItem();
        String currentValue = currentFilter.getSelectionModel().getSelectedItem();

        List<BarangayOfficial> filtered = backingList.stream()
                .filter(official -> position == null || official.getPosition() == position)
                .filter(official -> {
                    if (currentValue == null || ALL_LABEL.equals(currentValue)) {
                        return true;
                    }
                    if ("Current".equals(currentValue)) {
                        return official.isCurrent();
                    }
                    return !official.isCurrent();
                })
                .filter(official -> term.isEmpty() ||
                        (official.getOfficialName() != null && official.getOfficialName().toLowerCase().contains(term))
                        ||
                        official.getResidentId().getValue().toLowerCase().contains(term))
                .collect(Collectors.toList());

        officialsTable.setItems(FXCollections.observableArrayList(filtered));
        officialCountLabel.setText(String.format("Showing %d of %d officials", filtered.size(), backingList.size()));
        officialsTable.getSelectionModel().clearSelection();
        updateSelectionDependentActions(null);
    }

    private BarangayOfficial getSelectedOfficial() {
        BarangayOfficial selected = officialsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Officials", "Please select an official first.");
        }
        return selected;
    }

    private Optional<RegisterOfficialInputDto> showRegisterDialog() {
        Dialog<RegisterOfficialInputDto> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle("Register Official");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField residentField = new TextField();
        residentField.setPromptText("Resident ID");
        ChoiceBox<OfficialPosition> positionChoice = new ChoiceBox<>(
                FXCollections.observableArrayList(OfficialPosition.values()));
        positionChoice.getSelectionModel().selectFirst();
        DatePicker termStartPicker = new DatePicker();
        termStartPicker.setPromptText("Term start");
        DatePicker termEndPicker = new DatePicker();
        termEndPicker.setPromptText("Term end");
        CheckBox currentCheck = new CheckBox("Currently serving");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, FormFieldIndicator.requiredLabel("Resident ID"), residentField);
        grid.addRow(1, FormFieldIndicator.requiredLabel("Position"), positionChoice);
        grid.addRow(2, FormFieldIndicator.requiredLabel("Term Start"), termStartPicker);
        grid.addRow(3, FormFieldIndicator.requiredLabel("Term End"), termEndPicker);
        grid.add(currentCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            if (residentField.getText() == null || residentField.getText().trim().isEmpty()) {
                return Optional.of("Resident ID is required.");
            }
            if (termStartPicker.getValue() == null || termEndPicker.getValue() == null) {
                return Optional.of("Term dates are required.");
            }
            return Optional.empty();
        }, "Register Official");

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            LocalDate start = termStartPicker.getValue();
            LocalDate end = termEndPicker.getValue();
            return new RegisterOfficialInputDto(
                    residentField.getText().trim(),
                    positionChoice.getValue(),
                    start,
                    end,
                    currentCheck.isSelected());
        });

        return dialog.showAndWait();
    }

    private Optional<UpdateOfficialInputDto> showUpdateDialog(BarangayOfficial official) {
        Dialog<UpdateOfficialInputDto> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle("Update Official");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        DatePicker termStartPicker = new DatePicker(official.getTermStart());
        DatePicker termEndPicker = new DatePicker(official.getTermEnd());
        CheckBox currentCheck = new CheckBox("Currently serving");
        currentCheck.setSelected(official.isCurrent());
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, FormFieldIndicator.requiredLabel("Term Start"), termStartPicker);
        grid.addRow(1, FormFieldIndicator.requiredLabel("Term End"), termEndPicker);
        grid.add(currentCheck, 1, 2);

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            if (termStartPicker.getValue() == null || termEndPicker.getValue() == null) {
                return Optional.of("Term dates are required.");
            }
            return Optional.empty();
        }, "Update Official");

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            LocalDate start = termStartPicker.getValue();
            LocalDate end = termEndPicker.getValue();
            return new UpdateOfficialInputDto(
                    official.getOfficialId(),
                    start,
                    end,
                    currentCheck.isSelected());
        });

        return dialog.showAndWait();
    }

    private void configureRoleView() {
        if (managementActionsBox != null) {
            managementActionsBox.setVisible(true);
            managementActionsBox.setManaged(true);
        }
        if (canManageOfficials) {
            setButtonVisible(registerButton, true);
            setButtonVisible(updatePhotoButton, true);
            setButtonVisible(updateTermButton, true);
            setButtonVisible(endTermButton, true);
            if (viewerInfoLabel != null) {
                viewerInfoLabel.setVisible(false);
                viewerInfoLabel.setManaged(false);
            }
        } else {
            setButtonVisible(registerButton, false);
            setButtonVisible(updatePhotoButton, false);
            setButtonVisible(updateTermButton, false);
            setButtonVisible(endTermButton, false);
            if (viewerInfoLabel != null) {
                viewerInfoLabel.setText("View-only mode. You can preview official photos.");
                viewerInfoLabel.setVisible(true);
                viewerInfoLabel.setManaged(true);
            }
        }
        if (!canManageOfficials && refreshButton != null) {
            refreshButton.setDisable(false);
        }
        if (!canManageOfficials && viewPhotoButton != null) {
            viewPhotoButton.setDisable(true);
        }
    }

    private void setButtonVisible(Button button, boolean visible) {
        if (button == null) {
            return;
        }
        button.setVisible(visible);
        button.setManaged(visible);
    }

    private void updateSelectionDependentActions(BarangayOfficial selected) {
        boolean hasSelection = selected != null;
        if (viewPhotoButton != null) {
            boolean enable = hasSelection && hasExistingPhoto(selected);
            viewPhotoButton.setDisable(!enable);
        }
        if (updatePhotoButton != null) {
            updatePhotoButton.setDisable(!hasSelection);
        }
        if (updateTermButton != null) {
            updateTermButton.setDisable(!hasSelection);
        }
        if (endTermButton != null) {
            boolean enable = hasSelection && selected != null && selected.isCurrent();
            endTermButton.setDisable(!enable);
        }
    }

    private FileChooser createImageChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        return chooser;
    }

    private boolean hasExistingPhoto(BarangayOfficial official) {
        if (official == null || !official.hasPhoto()) {
            return false;
        }
        try {
            Path path = Paths.get(official.getPhotoPath());
            return Files.exists(path);
        } catch (InvalidPathException ex) {
            return false;
        }
    }

    private Optional<File> resolvePhotoFile(BarangayOfficial official) {
        if (!hasExistingPhoto(official)) {
            return Optional.empty();
        }
        return Optional.of(Paths.get(official.getPhotoPath()).toFile());
    }
}
