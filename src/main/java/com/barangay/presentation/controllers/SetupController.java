package com.barangay.presentation.controllers;

import com.barangay.application.dto.SetupInputDto;
import com.barangay.application.services.PasswordValidator;
import com.barangay.presentation.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the first-run setup screen.
 */
public class SetupController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label passwordHintLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Button setupButton;

    private MainApp mainApp;

    @FXML
    private void initialize() {
        PasswordValidator validator = new PasswordValidator();
        passwordHintLabel.setText(validator.getValidationMessage());
        clearMessage();
    }

    public void init(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleSetupAction() {
        clearMessage();
        SetupInputDto input = new SetupInputDto(
                usernameField.getText(),
                passwordField.getText());
        mainApp.handleSetup(input, this);
    }

    public void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll("message-label", "error");
    }

    public void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll("message-label", "success");
        setupButton.setDisable(true);
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.getStyleClass().setAll("message-label");
    }
}
