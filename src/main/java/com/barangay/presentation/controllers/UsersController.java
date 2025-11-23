package com.barangay.presentation.controllers;

import com.barangay.application.dto.CreateUserInputDto;
import com.barangay.application.services.PasswordValidator;
import com.barangay.application.services.SessionManager;
import com.barangay.application.usecases.ListUsersUseCase;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.util.DialogUtil;
import com.barangay.presentation.util.FormDialogUtil;
import com.barangay.presentation.util.FormFieldIndicator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Module controller for managing user accounts.
 */
public class UsersController implements ModuleController {

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, UserRole> roleColumn;

    @FXML
    private TableColumn<User, Boolean> activeColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ChoiceBox<UserRole> roleFilter;

    @FXML
    private Label userCountLabel;

    @FXML
    private Button createButton;

    @FXML
    private Button deactivateButton;

    @FXML
    private Button reactivateButton;

    @FXML
    private Button changeRoleButton;

    @FXML
    private Button resetPasswordButton;

    private final ObservableList<User> backingList = FXCollections.observableArrayList();

    private DIContainer container;

    @Override
    public void init(DIContainer container, MainLayoutController mainLayoutController) {
        this.container = container;
        configureTable();
        configureFilters();
    }

    @Override
    public void refresh() {
        loadUsers();
    }

    @FXML
    private void handleRefreshUsers() {
        refresh();
    }

    @FXML
    private void handleCreateUser() {
        SessionManager sessionManager = container.getSessionManager();
        User currentUser = sessionManager.getCurrentUser();
        List<UserRole> allowedRoles = Arrays.stream(UserRole.values())
                .filter(currentUser::canCreateRole)
                .collect(Collectors.toList());

        if (allowedRoles.isEmpty()) {
            DialogUtil.showWarning("Create User", "You are not allowed to create additional accounts.");
            return;
        }

        PasswordValidator passwordValidator = new PasswordValidator();

        Dialog<CreateUserInputDto> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle("Create User Account");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        ChoiceBox<UserRole> roleChoice = new ChoiceBox<>(FXCollections.observableArrayList(allowedRoles));
        roleChoice.getSelectionModel().selectFirst();
        TextField residentLinkField = new TextField();
        residentLinkField.setPromptText("Linked Resident ID");

        grid.addRow(0, FormFieldIndicator.requiredLabel("Username"), usernameField);
        grid.addRow(1, FormFieldIndicator.requiredLabel("Password"), passwordField);
        grid.addRow(2, FormFieldIndicator.requiredLabel("Role"), roleChoice);
        grid.addRow(3, FormFieldIndicator.requiredLabel("Resident ID"), residentLinkField);

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
                return Optional.of("Username is required.");
            }
            if (!passwordValidator.isValid(passwordField.getText())) {
                return Optional.of(passwordValidator.getValidationMessage());
            }
            if (residentLinkField.getText() == null || residentLinkField.getText().trim().isEmpty()) {
                return Optional.of("Resident ID is required before creating an account.");
            }
            return Optional.empty();
        }, "Create User");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new CreateUserInputDto(
                        usernameField.getText().trim(),
                        passwordField.getText(),
                        roleChoice.getValue(),
                        residentLinkField.getText().trim());
            }
            return null;
        });

        Optional<CreateUserInputDto> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                container.getCreateUserUseCase().execute(input);
                DialogUtil.showInfo("Create User", "User account created successfully.");
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Create User", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleDeactivateUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Deactivate User", "Please select a user first.");
            return;
        }
        if (!selected.isActive()) {
            DialogUtil.showWarning("Deactivate User", "User is already inactive.");
            return;
        }
        if (!DialogUtil.showConfirmation("Deactivate User",
                "Deactivate user " + selected.getUsername() + "?")) {
            return;
        }
        try {
            container.getDeactivateUserUseCase().execute(selected.getId().getValue());
            refresh();
        } catch (Exception ex) {
            DialogUtil.showError("Deactivate User", ex.getMessage());
        }
    }

    @FXML
    private void handleReactivateUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Reactivate User", "Please select a user first.");
            return;
        }
        if (selected.isActive()) {
            DialogUtil.showWarning("Reactivate User", "User is already active.");
            return;
        }
        try {
            container.getReactivateUserUseCase().execute(selected.getId().getValue());
            refresh();
        } catch (Exception ex) {
            DialogUtil.showError("Reactivate User", ex.getMessage());
        }
    }

    @FXML
    private void handleResetPassword() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Reset Password", "Please select a user first.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle("Reset Password");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordValidator passwordValidator = new PasswordValidator();
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        Label hint = new Label(passwordValidator.getValidationMessage());
        hint.getStyleClass().add("hint");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, FormFieldIndicator.requiredLabel("New Password"), newPasswordField);
        grid.add(hint, 1, 1);

        dialog.getDialogPane().setContent(grid);
        FormDialogUtil.keepOpenOnValidationFailure(dialog, () -> {
            if (!passwordValidator.isValid(newPasswordField.getText())) {
                return Optional.of(passwordValidator.getValidationMessage());
            }
            return Optional.empty();
        }, "Reset Password");

        dialog.setResultConverter(button -> button == ButtonType.OK ? newPasswordField.getText() : null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            try {
                container.getResetPasswordUseCase().execute(selected.getId().getValue(), newPassword);
                DialogUtil.showInfo("Reset Password", "Password updated successfully.");
            } catch (Exception ex) {
                DialogUtil.showError("Reset Password", ex.getMessage());
            }
        });
    }

    @FXML
    private void handleChangeRole() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Change Role", "Please select a user first.");
            return;
        }

        SessionManager sessionManager = container.getSessionManager();
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            DialogUtil.showError("Change Role", "No user is currently logged in.");
            return;
        }

        if (!currentUser.canManage(selected)) {
            DialogUtil.showWarning("Change Role", "You can only update accounts with lower roles.");
            return;
        }

        List<UserRole> assignableRoles = Arrays.stream(UserRole.values())
                .filter(currentUser::canCreateRole)
                .filter(role -> role != selected.getRole())
                .collect(Collectors.toList());

        if (assignableRoles.isEmpty()) {
            DialogUtil.showWarning("Change Role", "You are not allowed to assign any roles.");
            return;
        }

        ChoiceBox<UserRole> roleChoice = new ChoiceBox<>(FXCollections.observableArrayList(assignableRoles));
        roleChoice.getSelectionModel().selectFirst();

        Dialog<UserRole> dialog = new Dialog<>();
        FormDialogUtil.applyAppStyles(dialog);
        dialog.setTitle("Change User Role");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, FormFieldIndicator.requiredLabel("New Role"), roleChoice);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == ButtonType.OK ? roleChoice.getValue() : null);

        Optional<UserRole> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            try {
                container.getChangeUserRoleUseCase().execute(selected.getId().getValue(), newRole);
                DialogUtil.showInfo("Change Role",
                        String.format("Updated %s to %s", selected.getUsername(), newRole.name()));
                refresh();
            } catch (Exception ex) {
                DialogUtil.showError("Change Role", ex.getMessage());
            }
        });
    }

    private void configureTable() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        usersTable.setItems(backingList);
        usersTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> updateSelectionDependentActions(newVal));
        updateSelectionDependentActions(null);
    }

    private void configureFilters() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        ObservableList<UserRole> roles = FXCollections.observableArrayList();
        roles.add(null);
        roles.addAll(Arrays.asList(UserRole.values()));
        roleFilter.setItems(roles);
        roleFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserRole role) {
                return role == null ? "All" : role.toString();
            }

            @Override
            public UserRole fromString(String string) {
                return null;
            }
        });
        roleFilter.getSelectionModel().selectFirst();
        roleFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadUsers() {
        ListUsersUseCase listUsersUseCase = container.getListUsersUseCase();
        backingList.setAll(listUsersUseCase.execute());
        applyFilters();
        if (usersTable != null) {
            usersTable.getSelectionModel().clearSelection();
        }
        updateSelectionDependentActions(null);
    }

    private void applyFilters() {
        String term = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        UserRole role = roleFilter.getSelectionModel().getSelectedItem();

        List<User> filtered = backingList.stream()
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> term.isEmpty() || user.getUsername().toLowerCase().contains(term))
                .collect(Collectors.toList());

        usersTable.setItems(FXCollections.observableArrayList(filtered));
        userCountLabel.setText(String.format("Showing %d of %d users", filtered.size(), backingList.size()));
    }

    private void updateSelectionDependentActions(User selected) {
        boolean hasSelection = selected != null;
        if (deactivateButton != null) {
            boolean disable = !hasSelection || (selected != null && !selected.isActive());
            deactivateButton.setDisable(disable);
        }
        if (reactivateButton != null) {
            boolean disable = !hasSelection || (selected != null && selected.isActive());
            reactivateButton.setDisable(disable);
        }
        if (changeRoleButton != null) {
            changeRoleButton.setDisable(!hasSelection);
        }
        if (resetPasswordButton != null) {
            resetPasswordButton.setDisable(!hasSelection);
        }
    }
}
