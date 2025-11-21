package com.barangay.presentation.controllers;

import com.barangay.application.dto.RegisterOfficialInputDto;
import com.barangay.application.dto.UpdateOfficialInputDto;
import com.barangay.domain.entities.BarangayOfficial;
import com.barangay.domain.entities.OfficialPosition;
import com.barangay.domain.entities.UserRole;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.util.DialogUtil;
import com.barangay.presentation.util.FormDialogUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Module controller for managing barangay officials.
 */
public class OfficialsController implements ModuleController {

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
        positionFilter.getSelectionModel().clearSelection();
        currentFilter.getSelectionModel().clearSelection();
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
    }

    private void configureFilters() {
        positionFilter.setItems(FXCollections.observableArrayList(OfficialPosition.values()));
        currentFilter.setItems(FXCollections.observableArrayList("Current", "Former"));

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
                    if (currentValue == null) {
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
        grid.addRow(0, new Label("Resident ID"), residentField);
        grid.addRow(1, new Label("Position"), positionChoice);
        grid.addRow(2, new Label("Term Start"), termStartPicker);
        grid.addRow(3, new Label("Term End"), termEndPicker);
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
        dialog.setTitle("Update Official");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        DatePicker termStartPicker = new DatePicker(official.getTermStart());
        DatePicker termEndPicker = new DatePicker(official.getTermEnd());
        CheckBox currentCheck = new CheckBox("Currently serving");
        currentCheck.setSelected(official.isCurrent());
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Term Start"), termStartPicker);
        grid.addRow(1, new Label("Term End"), termEndPicker);
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
            managementActionsBox.setVisible(canManageOfficials);
            managementActionsBox.setManaged(canManageOfficials);
        }
        if (viewerInfoLabel != null) {
            if (canManageOfficials) {
                viewerInfoLabel.setVisible(false);
                viewerInfoLabel.setManaged(false);
            } else {
                viewerInfoLabel.setText("View-only mode. Contact an administrator for updates.");
                viewerInfoLabel.setVisible(true);
                viewerInfoLabel.setManaged(true);
            }
        }
    }
}
