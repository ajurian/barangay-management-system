package com.barangay.presentation.controllers;

import com.barangay.application.dto.RegisterResidentInputDto;
import com.barangay.domain.entities.BarangayInfo;
import com.barangay.domain.entities.CivilStatus;
import com.barangay.domain.entities.EducationLevel;
import com.barangay.domain.entities.Gender;
import com.barangay.domain.entities.IncomeBracket;
import com.barangay.domain.entities.Resident;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.util.DialogUtil;
import com.barangay.presentation.util.FormDialogUtil;
import com.barangay.presentation.util.FormFieldIndicator;
import com.barangay.presentation.util.TableCopyUtil;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Module controller for resident management operations.
 */
public class ResidentsController implements ModuleController {

    private static final String EMPLOYMENT_OTHER = "Others";
    private static final List<String> EMPLOYMENT_OPTIONS = List.of(
            "Employed (full-time)",
            "Employed (part-time)",
            "Self-employed",
            "Unemployed",
            "Student",
            "Retired",
            "OFW (Overseas Filipino Worker)",
            EMPLOYMENT_OTHER);

    @FXML
    private TableView<Resident> residentsTable;

    @FXML
    private TableColumn<Resident, String> residentIdColumn;

    @FXML
    private TableColumn<Resident, String> fullNameColumn;

    @FXML
    private TableColumn<Resident, Gender> genderColumn;

    @FXML
    private TableColumn<Resident, LocalDate> birthDateColumn;

    @FXML
    private TableColumn<Resident, String> contactColumn;

    @FXML
    private TableColumn<Resident, Boolean> activeColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<Gender> genderFilter;

    @FXML
    private ChoiceBox<String> statusFilter;

    @FXML
    private Label residentCountLabel;

    @FXML
    private Button updateButton;

    @FXML
    private Button deactivateButton;

    @FXML
    private Button reactivateButton;

    private final ObservableList<Resident> backingList = FXCollections.observableArrayList();

    private DIContainer container;

    @Override
    public void init(DIContainer container, MainLayoutController mainLayoutController) {
        this.container = container;
        configureTable();
        configureFilters();
    }

    @Override
    public void refresh() {
        backingList.setAll(container.getResidentRepository().findAll());
        applyFilters();
        if (residentsTable != null) {
            residentsTable.getSelectionModel().clearSelection();
        }
        updateSelectionDependentActions(null);
    }

    @FXML
    private void handleRefreshResidents() {
        refresh();
    }

    @FXML
    private void handleRegisterResident() {
        Optional<RegisterResidentInputDto> result = showResidentDialog(null);
        result.ifPresent(input -> {
            try {
                String residentId = container.getRegisterResidentUseCase().execute(input);
                DialogUtil.showInfo("Register Resident", "Resident registered with ID: " + residentId);
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Register Resident", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleUpdateResident() {
        Resident selected = residentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Update Resident", "Please select a resident first.");
            return;
        }
        Optional<RegisterResidentInputDto> result = showResidentDialog(selected);
        result.ifPresent(input -> {
            try {
                container.getUpdateResidentUseCase().execute(selected.getId().getValue(), input);
                DialogUtil.showInfo("Update Resident", "Resident record updated successfully.");
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Update Resident", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleDeactivateResident() {
        Resident selected = residentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Deactivate Resident", "Please select a resident first.");
            return;
        }
        if (!selected.isActive()) {
            DialogUtil.showWarning("Deactivate Resident", "Resident is already inactive.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deactivate Resident");
        dialog.setHeaderText("Provide reason for deactivation");
        Optional<String> reasonOpt = dialog.showAndWait();
        reasonOpt.ifPresent(reason -> {
            try {
                container.getDeactivateResidentUseCase().execute(selected.getId().getValue(), reason);
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Deactivate Resident", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleReactivateResident() {
        Resident selected = residentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Reactivate Resident", "Please select a resident first.");
            return;
        }
        if (selected.isActive()) {
            DialogUtil.showWarning("Reactivate Resident", "Resident is already active.");
            return;
        }
        try {
            container.getReactivateResidentUseCase().execute(selected.getId().getValue());
            refresh();
        } catch (Exception ex) {
            DialogUtil.showError("Reactivate Resident", ex.getMessage());
        }
    }

    private void configureTable() {
        residentIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        residentsTable.setItems(FXCollections.observableArrayList());
        residentsTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> updateSelectionDependentActions(newVal));
        updateSelectionDependentActions(null);
        TableCopyUtil.attachCopyContextMenu(residentsTable,
                resident -> resident != null && resident.getId() != null ? resident.getId().getValue() : null,
                "Copy Resident ID");
    }

    private void configureFilters() {
        ObservableList<Gender> genders = FXCollections.observableArrayList();
        genders.add(null);
        genders.addAll(Arrays.asList(Gender.values()));
        genderFilter.setItems(genders);
        genderFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(Gender gender) {
                return gender == null ? "All" : gender.toString();
            }

            @Override
            public Gender fromString(String string) {
                return null;
            }
        });
        genderFilter.getSelectionModel().selectFirst();
        genderFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        statusFilter.setItems(FXCollections.observableArrayList("All", "Active", "Inactive"));
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String term = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        Gender gender = genderFilter.getSelectionModel().getSelectedItem();
        String statusValue = statusFilter.getSelectionModel().getSelectedItem();

        List<Resident> filtered = backingList.stream()
                .filter(resident -> gender == null || resident.getGender() == gender)
                .filter(resident -> {
                    if ("Active".equals(statusValue)) {
                        return resident.isActive();
                    }
                    if ("Inactive".equals(statusValue)) {
                        return !resident.isActive();
                    }
                    return true;
                })
                .filter(resident -> term.isEmpty() || resident.getFullName().toLowerCase().contains(term)
                        || (resident.getContact() != null && resident.getContact().toLowerCase().contains(term))
                        || resident.getId().getValue().toLowerCase().contains(term))
                .collect(Collectors.toList());

        residentsTable.setItems(FXCollections.observableArrayList(filtered));
        residentCountLabel.setText(String.format("Showing %d of %d residents", filtered.size(), backingList.size()));
    }

    private Optional<RegisterResidentInputDto> showResidentDialog(Resident existing) {
        BarangayInfo barangayInfo;
        try {
            barangayInfo = container.getGetBarangayInfoUseCase().execute();
        } catch (Exception ex) {
            DialogUtil.showError("Resident Form", "Unable to load barangay information. Please configure it first.");
            return Optional.empty();
        }

        Dialog<RegisterResidentInputDto> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle(existing == null ? "Register Resident" : "Update Resident");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField middleNameField = new TextField();
        middleNameField.setPromptText("Middle Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField suffixField = new TextField();
        suffixField.setPromptText("Suffix");
        DatePicker birthDatePicker = new DatePicker();
        ChoiceBox<Gender> genderChoice = new ChoiceBox<>(FXCollections.observableArrayList(Gender.values()));
        ChoiceBox<CivilStatus> civilStatusChoice = new ChoiceBox<>(
                FXCollections.observableArrayList(CivilStatus.values()));
        TextField nationalityField = new TextField();
        nationalityField.setPromptText("Nationality");
        TextField contactField = new TextField();
        contactField.setPromptText("Contact Number");
        TextField barangayField = new TextField();
        barangayField.setPromptText("Barangay");
        TextField cityField = new TextField();
        cityField.setPromptText("City/Municipality");
        TextField provinceField = new TextField();
        provinceField.setPromptText("Province");
        TextField houseNumberField = new TextField();
        houseNumberField.setPromptText("House Number");
        TextField streetField = new TextField();
        streetField.setPromptText("Street");
        TextField purokField = new TextField();
        purokField.setPromptText("Purok/Zone");
        TextField occupationField = new TextField();
        occupationField.setPromptText("Occupation");
        ChoiceBox<String> employmentChoice = new ChoiceBox<>(FXCollections.observableArrayList(EMPLOYMENT_OPTIONS));
        Label employmentOtherLabel = FormFieldIndicator.optionalLabel("Specify Employment");
        employmentOtherLabel.setVisible(false);
        employmentOtherLabel.setManaged(false);
        TextField employmentOtherField = new TextField();
        employmentOtherField.setPromptText("Specify Employment");
        employmentOtherField.setVisible(false);
        employmentOtherField.setManaged(false);
        employmentChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean showCustom = EMPLOYMENT_OTHER.equals(newVal);
            setEmploymentOtherVisibility(employmentOtherLabel, employmentOtherField, showCustom);
            if (!showCustom) {
                employmentOtherField.clear();
            }
            if (showCustom) {
                FormFieldIndicator.markRequired(employmentOtherLabel);
            } else {
                FormFieldIndicator.markOptional(employmentOtherLabel);
            }
        });
        ChoiceBox<IncomeBracket> incomeChoice = new ChoiceBox<>(
                FXCollections.observableArrayList(IncomeBracket.values()));
        ChoiceBox<EducationLevel> educationChoice = new ChoiceBox<>(
                FXCollections.observableArrayList(EducationLevel.values()));
        TextField birthPlaceField = new TextField();
        birthPlaceField.setPromptText("Birth Place");

        int row = 0;
        grid.addRow(row++, FormFieldIndicator.requiredLabel("First Name"), firstNameField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Middle Name"), middleNameField);
        grid.addRow(row++, FormFieldIndicator.requiredLabel("Last Name"), lastNameField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Suffix"), suffixField);
        grid.addRow(row++, FormFieldIndicator.requiredLabel("Birth Date"), birthDatePicker);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Birth Place"), birthPlaceField);
        grid.addRow(row++, FormFieldIndicator.requiredLabel("Gender"), genderChoice);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Civil Status"), civilStatusChoice);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Nationality"), nationalityField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Contact"), contactField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Barangay"), barangayField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("City"), cityField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Province"), provinceField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("House No."), houseNumberField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Street"), streetField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Purok"), purokField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Occupation"), occupationField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Employment"), employmentChoice);
        grid.addRow(row++, employmentOtherLabel, employmentOtherField);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Income Bracket"), incomeChoice);
        grid.addRow(row++, FormFieldIndicator.optionalLabel("Education"), educationChoice);

        setAddressDefaults(barangayField, barangayInfo.getBarangayName());
        setAddressDefaults(cityField, barangayInfo.getCity());
        setAddressDefaults(provinceField, barangayInfo.getProvince());

        if (existing != null) {
            firstNameField.setText(existing.getFirstName());
            middleNameField.setText(existing.getMiddleName());
            lastNameField.setText(existing.getLastName());
            suffixField.setText(existing.getSuffix());
            birthDatePicker.setValue(existing.getBirthDate());
            birthPlaceField.setText(existing.getBirthPlace());
            genderChoice.setValue(existing.getGender());
            civilStatusChoice.setValue(existing.getCivilStatus());
            nationalityField.setText(existing.getNationality());
            contactField.setText(existing.getContact());
            if (existing.getAddress() != null) {
                houseNumberField.setText(existing.getAddress().getHouseNumber());
                streetField.setText(existing.getAddress().getStreet());
                purokField.setText(existing.getAddress().getPurok());
            }
            occupationField.setText(existing.getOccupation());
            String existingEmployment = existing.getEmployment();
            if (existingEmployment != null && !existingEmployment.trim().isEmpty()) {
                String normalizedEmployment = existingEmployment.trim();
                if (EMPLOYMENT_OPTIONS.contains(normalizedEmployment)) {
                    employmentChoice.setValue(normalizedEmployment);
                } else {
                    employmentChoice.setValue(EMPLOYMENT_OTHER);
                    employmentOtherField.setText(normalizedEmployment);
                    setEmploymentOtherVisibility(employmentOtherLabel, employmentOtherField, true);
                }
            }
            incomeChoice.setValue(existing.getIncomeBracket());
            educationChoice.setValue(existing.getEducationLevel());
        }

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
                return Optional.of("First name is required.");
            }
            if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
                return Optional.of("Last name is required.");
            }
            if (birthDatePicker.getValue() == null || genderChoice.getValue() == null) {
                return Optional.of("Birth date and gender are required.");
            }
            if (EMPLOYMENT_OTHER.equals(employmentChoice.getValue())) {
                String customEmployment = employmentOtherField.getText();
                if (customEmployment == null || customEmployment.trim().isEmpty()) {
                    return Optional.of("Please specify employment details.");
                }
            }
            return Optional.empty();
        }, "Resident Form");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String employmentValue = employmentChoice.getValue();
                if (EMPLOYMENT_OTHER.equals(employmentValue)) {
                    employmentValue = employmentOtherField.getText().trim();
                }

                return new RegisterResidentInputDto(
                        firstNameField.getText(),
                        middleNameField.getText(),
                        lastNameField.getText(),
                        suffixField.getText(),
                        birthDatePicker.getValue(),
                        birthPlaceField.getText(),
                        genderChoice.getValue(),
                        civilStatusChoice.getValue(),
                        nationalityField.getText(),
                        contactField.getText(),
                        houseNumberField.getText(),
                        streetField.getText(),
                        purokField.getText(),
                        barangayField.getText(),
                        cityField.getText(),
                        provinceField.getText(),
                        occupationField.getText(),
                        employmentValue,
                        incomeChoice.getValue(),
                        educationChoice.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void setEmploymentOtherVisibility(Label label, TextField field, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        field.setVisible(visible);
        field.setManaged(visible);
    }

    private void setAddressDefaults(TextField field, String value) {
        field.setText(value != null ? value : "");
        field.setEditable(false);
        field.setFocusTraversable(false);
    }

    private void updateSelectionDependentActions(Resident selected) {
        boolean hasSelection = selected != null;
        if (updateButton != null) {
            updateButton.setDisable(!hasSelection);
        }
        if (deactivateButton != null) {
            boolean disable = !hasSelection || (selected != null && !selected.isActive());
            deactivateButton.setDisable(disable);
        }
        if (reactivateButton != null) {
            boolean disable = !hasSelection || (selected != null && selected.isActive());
            reactivateButton.setDisable(disable);
        }
    }
}
