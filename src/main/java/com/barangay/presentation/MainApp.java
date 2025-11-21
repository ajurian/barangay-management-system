package com.barangay.presentation;

import com.barangay.application.dto.LoginInputDto;
import com.barangay.application.dto.LoginOutputDto;
import com.barangay.application.dto.SetupInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.User;
import com.barangay.domain.valueobjects.UserId;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.controllers.LoginController;
import com.barangay.presentation.controllers.MainLayoutController;
import com.barangay.presentation.controllers.SetupController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * JavaFX application entry point responsible for wiring presentation layer
 * views.
 */
public class MainApp extends Application {

    private static MainApp instance;

    private Stage primaryStage;
    private DIContainer container;

    public MainApp() {
        instance = this;
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Barangay Management System");
        this.primaryStage.setMinWidth(1100);
        this.primaryStage.setMinHeight(700);
        this.primaryStage.setWidth(this.primaryStage.getMinWidth());
        this.primaryStage.setHeight(this.primaryStage.getMinHeight());
        this.primaryStage.setOnCloseRequest(event -> Platform.exit());

        this.container = new DIContainer();

        if (container.getSystemSetupUseCase().needsSetup()) {
            showSetupView();
        } else {
            showLoginView(Optional.empty());
        }
    }

    /**
     * Display the initial setup view for creating the first Super Admin account.
     */
    public void showSetupView() {
        try {
            FXMLLoader loader = loadFXML("setup-view.fxml");
            Parent root = loader.load();

            SetupController controller = loader.getController();
            controller.init(this);

            setScene(root);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load setup view", ex);
        }
    }

    /**
     * Display the login view. Optionally show an informational banner message.
     */
    public void showLoginView(Optional<String> bannerMessage) {
        try {
            FXMLLoader loader = loadFXML("login-view.fxml");
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.init(this, container, bannerMessage.orElse(null));

            setScene(root);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load login view", ex);
        }
    }

    /**
     * Authenticate user and redirect to main workspace.
     */
    public void handleLogin(String username, String password, LoginController loginController) {
        try {
            LoginInputDto input = new LoginInputDto(username, password);
            LoginOutputDto result = container.getLoginUseCase().execute(input);

            User user = container.getUserRepository()
                    .findById(UserId.fromString(result.getUserId()))
                    .orElseThrow(() -> new IllegalStateException("User not found after login"));

            SessionManager sessionManager = container.getSessionManager();
            sessionManager.setCurrentUser(user);

            showMainWorkspace(result, user);
        } catch (Exception ex) {
            loginController.showError(ex.getMessage());
        }
    }

    /**
     * Persist the first Super Admin account then return to login screen.
     */
    public void handleSetup(SetupInputDto input, SetupController setupController) {
        try {
            container.getSystemSetupUseCase().execute(input);
            setupController.showSuccess("Setup completed. You can now log in.");
            Platform.runLater(() -> showLoginView(Optional.of("Initial setup completed successfully.")));
        } catch (Exception ex) {
            setupController.showError(ex.getMessage());
        }
    }

    /**
     * Log out current session and return to login view.
     */
    public void logout() {
        container.getSessionManager().logout();
        showLoginView(Optional.of("You have been logged out."));
    }

    private void showMainWorkspace(LoginOutputDto loginResult, User currentUser) {
        try {
            FXMLLoader loader = loadFXML("main-layout.fxml");
            Parent root = loader.load();

            MainLayoutController controller = loader.getController();
            controller.init(this, container, loginResult, currentUser);

            setScene(root);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load main layout", ex);
        }
    }

    private FXMLLoader loadFXML(String fileName) {
        return new FXMLLoader(getClass().getResource("/fxml/" + fileName));
    }

    private void setScene(Parent root) {
        Scene scene = new Scene(root);
        String stylesheet = getClass().getResource("/styles/app.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public DIContainer getContainer() {
        return container;
    }

    public static MainApp getInstance() {
        return instance;
    }
}
