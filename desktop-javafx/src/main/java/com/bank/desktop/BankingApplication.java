package com.bank.desktop;

import com.bank.desktop.controller.components.SidebarController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BankingApplication extends Application {

    private static Stage primaryStage;
    private static Scene loginScene;
    private static BorderPane shell;
    private static SidebarController sidebarController;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent loginRoot = loginLoader.load();
        loginScene = new Scene(loginRoot);
        loginScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setTitle("CMeridian Bank");
        stage.setScene(loginScene);
        stage.setMinWidth(1024);
        stage.setMinHeight(700);
        stage.show();

        log.info("Application started");
    }

    public static void switchToDashboard() {
        try {
            FXMLLoader sidebarLoader = new FXMLLoader(BankingApplication.class.getResource("/fxml/components/sidebar.fxml"));
            Parent sidebar = sidebarLoader.load();
            sidebarController = sidebarLoader.getController();

            shell = new BorderPane();
            shell.setLeft(sidebar);

            Scene mainScene = new Scene(shell);
            mainScene.getStylesheets().add(BankingApplication.class.getResource("/css/style.css").toExternalForm());

            primaryStage.setScene(mainScene);
            primaryStage.setTitle("CMeridian Bank");
            primaryStage.setMinWidth(1280);
            primaryStage.setMinHeight(800);
            primaryStage.setMaximized(true);

            navigateTo("dashboard");
        } catch (Exception e) {
            log.error("Failed to load dashboard shell: {}", e.getMessage(), e);
        }
    }

    public static void navigateTo(String screen) {
        try {
            String fxmlPath = "/fxml/" + screen + ".fxml";
            FXMLLoader loader = new FXMLLoader(BankingApplication.class.getResource(fxmlPath));
            Parent root = loader.load();
            shell.setCenter(root);

            if (sidebarController != null) {
                sidebarController.setActive(screen);
            }

        } catch (Exception e) {
            log.error("Navigation failed to {}: {}", screen, e.getMessage(), e);
        }
    }

    public static void switchToLogin() {
        primaryStage.setScene(loginScene);
        primaryStage.setMaximized(false);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        log.info("Returned to login");
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}