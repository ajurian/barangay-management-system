package com.barangay.presentation.controllers;

import com.barangay.application.dto.LoginOutputDto;
import com.barangay.domain.entities.BarangayInfo;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.infrastructure.config.DIContainer;
import com.barangay.presentation.MainApp;
import com.barangay.presentation.util.DialogUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the main workspace shell and navigation.
 */
public class MainLayoutController {

    private enum NavigationTarget {
        DASHBOARD("dashboard-view.fxml"),
        USERS("users-view.fxml"),
        RESIDENTS("residents-view.fxml"),
        DOCUMENTS("documents-view.fxml"),
        DOCUMENT_REQUESTS("document-requests-view.fxml"),
        VOTER_APPLICATIONS("voter-applications-view.fxml"),
        OFFICIALS("officials-view.fxml"),
        BARANGAY_INFO("barangay-info-view.fxml"),
        PROFILE("profile-view.fxml");

        private final String fxml;

        NavigationTarget(String fxml) {
            this.fxml = fxml;
        }

        public String getFxml() {
            return fxml;
        }
    }

    private static class LoadedModule {
        private final Node node;
        private final ModuleController controller;

        LoadedModule(Node node, ModuleController controller) {
            this.node = node;
            this.controller = controller;
        }

        Node node() {
            return node;
        }

        ModuleController controller() {
            return controller;
        }
    }

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox navigationBox;

    @FXML
    private Label currentUserLabel;

    @FXML
    private Label currentRoleLabel;

    @FXML
    private Label barangayNameLabel;

    @FXML
    private ImageView barangaySealImage;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button residentsButton;

    @FXML
    private Button documentsButton;

    @FXML
    private Button documentRequestsButton;

    @FXML
    private Button voterApplicationsButton;

    @FXML
    private Button officialsButton;

    @FXML
    private Button barangayInfoButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button logoutButton;

    @FXML
    private StackPane contentPane;

    private final Map<NavigationTarget, LoadedModule> moduleCache = new EnumMap<>(NavigationTarget.class);
    private final Map<NavigationTarget, Button> navigationButtons = new EnumMap<>(NavigationTarget.class);

    private MainApp mainApp;
    private DIContainer container;
    private LoginOutputDto loginInfo;
    private User currentUser;

    @FXML
    private void initialize() {
        navigationButtons.put(NavigationTarget.DASHBOARD, dashboardButton);
        navigationButtons.put(NavigationTarget.USERS, usersButton);
        navigationButtons.put(NavigationTarget.RESIDENTS, residentsButton);
        navigationButtons.put(NavigationTarget.DOCUMENTS, documentsButton);
        navigationButtons.put(NavigationTarget.DOCUMENT_REQUESTS, documentRequestsButton);
        navigationButtons.put(NavigationTarget.VOTER_APPLICATIONS, voterApplicationsButton);
        navigationButtons.put(NavigationTarget.OFFICIALS, officialsButton);
        navigationButtons.put(NavigationTarget.BARANGAY_INFO, barangayInfoButton);
        navigationButtons.put(NavigationTarget.PROFILE, profileButton);
    }

    public void init(MainApp mainApp, DIContainer container, LoginOutputDto loginInfo, User currentUser) {
        this.mainApp = mainApp;
        this.container = container;
        this.loginInfo = loginInfo;
        this.currentUser = currentUser;

        currentUserLabel.setText(currentUser.getUsername());
        currentRoleLabel.setText(currentUser.getRole().toString());
        loadBarangayDetails();
        configureNavigationForRole(currentUser.getRole());
        showModule(defaultTargetForRole(currentUser.getRole()));
    }

    public void refreshBarangayDetails() {
        loadBarangayDetails();
    }

    public MainApp getMainApp() {
        return mainApp;
    }

    public DIContainer getContainer() {
        return container;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public LoginOutputDto getLoginInfo() {
        return loginInfo;
    }

    @FXML
    public void openDashboard() {
        showModule(NavigationTarget.DASHBOARD);
    }

    @FXML
    public void openUsers() {
        showModule(NavigationTarget.USERS);
    }

    @FXML
    public void openResidents() {
        showModule(NavigationTarget.RESIDENTS);
    }

    @FXML
    public void openDocuments() {
        showModule(NavigationTarget.DOCUMENTS);
    }

    @FXML
    public void openDocumentRequests() {
        showModule(NavigationTarget.DOCUMENT_REQUESTS);
    }

    @FXML
    public void openVoterApplications() {
        showModule(NavigationTarget.VOTER_APPLICATIONS);
    }

    @FXML
    public void openOfficials() {
        showModule(NavigationTarget.OFFICIALS);
    }

    @FXML
    public void openBarangayInfo() {
        showModule(NavigationTarget.BARANGAY_INFO);
    }

    @FXML
    public void openProfile() {
        showModule(NavigationTarget.PROFILE);
    }

    @FXML
    private void handleLogout() {
        if (DialogUtil.showConfirmation("Log out", "Are you sure you want to log out?")) {
            mainApp.logout();
        }
    }

    private void showModule(NavigationTarget target) {
        LoadedModule loaded = moduleCache.computeIfAbsent(target, this::loadModule);
        contentPane.getChildren().setAll(loaded.node());
        loaded.controller().refresh();
        highlightNavigation(target);
    }

    private LoadedModule loadModule(NavigationTarget target) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + target.getFxml()));
            Node node = loader.load();
            Object controllerObj = loader.getController();
            if (!(controllerObj instanceof ModuleController)) {
                throw new IllegalStateException("Controller for " + target + " does not implement ModuleController");
            }
            ModuleController controller = (ModuleController) controllerObj;
            controller.init(container, this);
            return new LoadedModule(node, controller);
        } catch (IOException ex) {
            ex.printStackTrace();
            Throwable cause = ex.getCause();
            if (cause != null) {
                System.err.println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
            }
            throw new IllegalStateException("Failed to load module: " + target, ex);
        }
    }

    private void highlightNavigation(NavigationTarget target) {
        navigationButtons.values().forEach(button -> button.getStyleClass().remove("active-nav"));
        Button activeButton = navigationButtons.get(target);
        if (activeButton != null && !activeButton.getStyleClass().contains("active-nav")) {
            activeButton.getStyleClass().add("active-nav");
        }
    }

    private void configureNavigationForRole(UserRole role) {
        boolean isSuperAdmin = role == UserRole.SUPER_ADMIN;
        boolean isAdmin = role == UserRole.ADMIN;
        boolean isClerk = role == UserRole.CLERK;

        setVisible(usersButton, isSuperAdmin || isAdmin);
        setVisible(residentsButton, isSuperAdmin || isAdmin || isClerk);
        setVisible(documentsButton, true);
        setVisible(documentRequestsButton, true);
        setVisible(voterApplicationsButton, true);
        setVisible(officialsButton, true);
        setVisible(barangayInfoButton, true);
        setVisible(profileButton, true);
        setVisible(dashboardButton, true);
    }

    private NavigationTarget defaultTargetForRole(UserRole role) {
        return NavigationTarget.DASHBOARD;
    }

    private void setVisible(Button button, boolean visible) {
        button.setManaged(visible);
        button.setVisible(visible);
    }

    private void loadBarangayDetails() {
        try {
            BarangayInfo info = container.getGetBarangayInfoUseCase().execute();
            barangayNameLabel.setText(Optional.ofNullable(info.getBarangayName()).orElse("Barangay"));
            updateSealImage(info.getSealPath());
        } catch (Exception ex) {
            barangayNameLabel.setText("Barangay Management System");
            setSealVisibility(false);
        }
    }

    private void updateSealImage(String sealPath) {
        if (sealPath == null || sealPath.isBlank()) {
            setSealVisibility(false);
            return;
        }

        Image image = tryLoadImage(sealPath.trim());
        if (image != null && !image.isError()) {
            barangaySealImage.setImage(image);
            setSealVisibility(true);
        } else {
            setSealVisibility(false);
        }
    }

    private Image tryLoadImage(String sealPath) {
        try {
            if (sealPath.startsWith("http://") || sealPath.startsWith("https://")) {
                return new Image(sealPath, true);
            }

            if (sealPath.startsWith("classpath:")) {
                String resourcePath = sealPath.substring("classpath:".length());
                InputStream stream = getClass()
                        .getResourceAsStream(resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath);
                if (stream != null) {
                    return new Image(stream);
                }
            }

            if (sealPath.startsWith("/")) {
                URL resource = getClass().getResource(sealPath);
                if (resource != null) {
                    return new Image(resource.toExternalForm(), true);
                }
            }

            Path filePath = Path.of(sealPath);
            if (Files.exists(filePath)) {
                return new Image(filePath.toUri().toString(), true);
            }

            // Fallback: attempt to treat as URL even if not prefixed
            try {
                URL url = new URL(sealPath);
                return new Image(url.toExternalForm(), true);
            } catch (MalformedURLException ignored) {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private void setSealVisibility(boolean visible) {
        if (barangaySealImage != null) {
            barangaySealImage.setVisible(visible);
            barangaySealImage.setManaged(visible);
            if (!visible) {
                barangaySealImage.setImage(null);
            }
        }
    }

}
