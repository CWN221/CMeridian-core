package com.bank.desktop.controller;

import com.bank.desktop.BankingApplication;
import com.bank.desktop.model.User;
import com.bank.desktop.service.AuthService;
import com.bank.desktop.util.NotificationUtil;
import com.bank.desktop.util.SessionManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class LoginController implements Initializable {

    @FXML private StackPane loginContainer;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheck;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final AuthService authService = new AuthService();
    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupEventHandlers();
        loadRememberedCredentials();
        
        log.info("Login controller initialized");
    }

    private void setupUI() {
        // Make login button primary by default
        loginButton.setDefaultButton(true);
        
        // Add loading indicator (hidden by default)
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        
        // Error label hidden initially
        errorLabel.setVisible(false);
    }

    private void setupEventHandlers() {
        // Enter key submits login
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });
        
        // Login button action
        loginButton.setOnAction(event -> handleLogin());        
        
        // Remember me checkbox
        rememberMeCheck.setOnAction(event -> {
            if (!rememberMeCheck.isSelected()) {
                // Clear saved credentials
                SessionManager.clearSavedCredentials();
            }
        });
    }

    private void loadRememberedCredentials() {
        User savedUser = SessionManager.getSavedCredentials();
        if (savedUser != null) {
            usernameField.setText(savedUser.getUsername());
            rememberMeCheck.setSelected(true);
            passwordField.requestFocus();
            log.info("Loaded remembered credentials for user: {}", savedUser.getUsername());
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validate input
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            showError("Please enter both username and password");
            return;
        }
        
        // Check if account is locked
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            showError("Account temporarily locked. Please try again later.");
            return;
        }
        
        // Show loading indicator
        setLoading(true);
        
        // Perform login in background thread
        new Thread(() -> {
            try {
                User user = authService.login(username, password);
                
                Platform.runLater(() -> {
                    setLoading(false);
                    if (user != null) {
                        // Save session
                        SessionManager.setCurrentUser(user);
                        
                        // Save credentials if remember me is checked
                        if (rememberMeCheck.isSelected()) {
                            SessionManager.saveCredentials(user);
                        }
                        
                        // Reset failed attempts
                        failedAttempts = 0;
                        
                        // Show success notification
                        NotificationUtil.showSuccess(
                            "Welcome back, " + user.getFullName() + "!",
                            "You have successfully logged in."
                        );
                        
                        // Switch to dashboard
                        BankingApplication.switchToDashboard();
                    } else {
                        failedAttempts++;
                        showError("Invalid username or password");
                    }
                });
            } catch (Exception e) {
                log.error("Login error: {}", e.getMessage(), e);
                Platform.runLater(() -> {
                    setLoading(false);
                    failedAttempts++;
                    showError(e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);
        usernameField.requestFocus();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        
        // Vibrate effect on error
        loginContainer.setStyle("-fx-border-color: #c62828; -fx-border-width: 2px;");
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> loginContainer.setStyle("-fx-border-color: transparent;"));
        pause.play();
        
        // Clear after 5 seconds
        PauseTransition clearPause = new PauseTransition(Duration.seconds(5));
        clearPause.setOnFinished(e -> errorLabel.setVisible(false));
        clearPause.play();
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
        
        if (loading) {
            loginButton.setText("Logging in...");
        } else {
            loginButton.setText("Login");
        }
    }

    @FXML
    private void handleForgotPassword() {
        // Show password reset dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText("Password Reset");
        alert.setContentText("Please contact your system administrator to reset your password.");
        alert.showAndWait();
    }

    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) loginContainer.getScene().getWindow();
        stage.setIconified(true);
    }
}