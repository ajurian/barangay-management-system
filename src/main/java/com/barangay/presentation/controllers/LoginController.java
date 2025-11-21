package com.barangay.presentation.controllers;

import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the authentication screen.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    @FXML
    private Label bannerLabel;

    private MainApp mainApp;
    private DIContainer container;

    public void init(MainApp mainApp, DIContainer container, String bannerMessage) {
        this.mainApp = mainApp;
        this.container = container;
        if (bannerMessage != null && !bannerMessage.isEmpty()) {
            bannerLabel.setText(bannerMessage);
            bannerLabel.setManaged(true);
        } else {
            bannerLabel.setText("");
            bannerLabel.setManaged(false);
        }
        clearMessage();
    }

    @FXML
    private void handleLoginAction() {
        clearMessage();
        loginButton.setDisable(true);
        mainApp.handleLogin(usernameField.getText(), passwordField.getText(), this);
        loginButton.setDisable(false);
    }

    public void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll("message-label", "error");
    }

    public void showInfo(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll("message-label", "success");
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.getStyleClass().setAll("message-label");
    }
}
