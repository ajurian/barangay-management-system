package com.barangay.presentation.controllers;

import com.barangay.application.dto.ChangePasswordInputDto;
import com.barangay.application.dto.UpdateProfileInputDto;
import com.barangay.application.services.PasswordValidator;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.util.DialogUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * Module controller for managing the logged-in user's profile.
 */
public class ProfileController implements ModuleController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label linkedResidentLabel;

    @FXML
    private TextField residentIdField;

    @FXML
    private Button saveProfileButton;

    @FXML
    private Label profileMessageLabel;

    @FXML
    private VBox residentInfoSection;

    @FXML
    private Label residentInfoMessageLabel;

    @FXML
    private Label residentIdValueLabel;

    @FXML
    private Label residentNameValueLabel;

    @FXML
    private Label residentBirthDateValueLabel;

    @FXML
    private Label residentGenderValueLabel;

    @FXML
    private Label residentContactValueLabel;

    @FXML
    private Label residentAddressValueLabel;

    @FXML
    private Label residentStatusValueLabel;

    @FXML
    private Label residentVoterStatusValueLabel;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label passwordHintLabel;

    private DIContainer container;
    private SessionManager sessionManager;
    private User currentUser;

    private static final DateTimeFormatter BIRTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @Override
    public void init(DIContainer container, MainLayoutController mainLayoutController) {
        this.container = container;
        this.sessionManager = container.getSessionManager();
        this.currentUser = sessionManager.getCurrentUser();
        passwordHintLabel.setText(new PasswordValidator().getValidationMessage());
        updateResidentLinkVisibility();
        refresh();
    }

    @Override
    public void refresh() {
        if (sessionManager == null || sessionManager.getCurrentUser() == null) {
            DialogUtil.showWarning("Profile", "No active user session found.");
            return;
        }
        currentUser = sessionManager.getCurrentUser();
        usernameLabel.setText(currentUser.getUsername());
        roleLabel.setText(currentUser.getRole().toString());
        if (residentIdField != null) {
            if (currentUser.getLinkedResidentId() != null) {
                residentIdField.setText(currentUser.getLinkedResidentId().getValue());
            } else {
                residentIdField.clear();
            }
        }
        updateResidentLinkVisibility();
        updateResidentInfo();
        profileMessageLabel.setText("");
        clearPasswordFields();
    }

    @FXML
    private void handleSaveProfile() {
        try {
            UpdateProfileInputDto dto = new UpdateProfileInputDto(
                    currentUser.getId().getValue(),
                    resolveLinkedResidentInput());
            container.getUpdateProfileUseCase().execute(dto);
            reloadCurrentUser();
            refresh();
            profileMessageLabel.setText("Profile updated successfully.");
        } catch (Exception ex) {
            DialogUtil.showError("Profile", ex.getMessage());
        }
    }

    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (currentPassword == null || currentPassword.isEmpty() ||
                newPassword == null || newPassword.isEmpty() ||
                confirmPassword == null || confirmPassword.isEmpty()) {
            DialogUtil.showWarning("Change Password", "All password fields are required.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            DialogUtil.showWarning("Change Password", "New password and confirm password do not match.");
            return;
        }

        try {
            ChangePasswordInputDto dto = new ChangePasswordInputDto(
                    currentUser.getId().getValue(),
                    currentPassword,
                    newPassword);
            container.getChangePasswordUseCase().execute(dto);
            clearPasswordFields();
            profileMessageLabel.setText("Password updated successfully.");
        } catch (Exception ex) {
            DialogUtil.showError("Change Password", ex.getMessage());
        }
    }

    private void reloadCurrentUser() {
        sessionManager.setCurrentUser(container.getUserRepository()
                .findById(currentUser.getId())
                .orElse(currentUser));
        currentUser = sessionManager.getCurrentUser();
        usernameLabel.setText(currentUser.getUsername());
    }

    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private String resolveLinkedResidentInput() {
        if (residentIdField == null || !residentIdField.isManaged()) {
            return null;
        }
        String value = residentIdField.getText();
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void updateResidentLinkVisibility() {
        if (residentIdField == null || linkedResidentLabel == null) {
            return;
        }
        boolean show = currentUser != null && currentUser.getRole() == UserRole.SUPER_ADMIN;
        residentIdField.setVisible(show);
        residentIdField.setManaged(show);
        residentIdField.setEditable(show);
        linkedResidentLabel.setVisible(show);
        linkedResidentLabel.setManaged(show);
        if (saveProfileButton != null) {
            saveProfileButton.setVisible(show);
            saveProfileButton.setManaged(show);
        }
    }

    private void updateResidentInfo() {
        if (residentInfoSection == null) {
            return;
        }
        ResidentId linkedResidentId = currentUser != null ? currentUser.getLinkedResidentId() : null;
        if (linkedResidentId == null) {
            residentInfoSection.setVisible(false);
            residentInfoSection.setManaged(false);
            return;
        }

        Resident resident = container.getResidentRepository()
                .findById(linkedResidentId)
                .orElse(null);

        residentInfoSection.setVisible(true);
        residentInfoSection.setManaged(true);

        if (resident == null) {
            residentInfoMessageLabel.setText("Resident record not found. Please contact the system administrator.");
            resetResidentInfoFields();
            return;
        }

        residentInfoMessageLabel.setText("");
        showResidentInfo(resident);
    }

    private void resetResidentInfoFields() {
        if (residentIdValueLabel != null) {
            residentIdValueLabel.setText("--");
        }
        if (residentNameValueLabel != null) {
            residentNameValueLabel.setText("--");
        }
        if (residentBirthDateValueLabel != null) {
            residentBirthDateValueLabel.setText("--");
        }
        if (residentGenderValueLabel != null) {
            residentGenderValueLabel.setText("--");
        }
        if (residentContactValueLabel != null) {
            residentContactValueLabel.setText("--");
        }
        if (residentAddressValueLabel != null) {
            residentAddressValueLabel.setText("--");
        }
        if (residentStatusValueLabel != null) {
            residentStatusValueLabel.setText("--");
        }
        if (residentVoterStatusValueLabel != null) {
            residentVoterStatusValueLabel.setText("--");
        }
    }

    private void showResidentInfo(Resident resident) {
        if (residentIdValueLabel != null) {
            residentIdValueLabel.setText(resident.getId().getValue());
        }
        if (residentNameValueLabel != null) {
            residentNameValueLabel.setText(resident.getFullName());
        }
        if (residentBirthDateValueLabel != null) {
            residentBirthDateValueLabel.setText(resident.getBirthDate() != null
                    ? BIRTH_DATE_FORMATTER.format(resident.getBirthDate())
                    : "Not provided");
        }
        if (residentGenderValueLabel != null) {
            residentGenderValueLabel.setText(resident.getGender() != null
                    ? resident.getGender().toString()
                    : "Not provided");
        }
        if (residentContactValueLabel != null) {
            residentContactValueLabel.setText(optionalValue(resident.getContact()));
        }
        if (residentAddressValueLabel != null) {
            residentAddressValueLabel.setText(resident.getAddress() != null
                    ? resident.getAddress().toString()
                    : "Not provided");
        }
        if (residentStatusValueLabel != null) {
            residentStatusValueLabel.setText(resident.isActive() ? "Active" : "Inactive");
        }
        if (residentVoterStatusValueLabel != null) {
            residentVoterStatusValueLabel.setText(resident.isVoter()
                    ? "Registered voter"
                    : "Not yet a registered voter");
        }
    }

    private String optionalValue(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }
}
