package com.bank.desktop.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Queue;

public final class NotificationUtil {

    private static final Queue<Notification> QUEUE = new ArrayDeque<>();

    private static final int DURATION = 4000;
    private static final double WIDTH = 360;

    private static boolean processing;

    private NotificationUtil() {
    }

    public static void showSuccess(String title, String message) {
        showNotification(title, message);
    }

    public static void showError(String title, String message) {
        showNotification(title, message);
    }

    public static void showWarning(String title, String message) {
        showNotification(title, message);
    }

    public static void showInfo(String title, String message) {
        showNotification(title, message);
    }

    private static void showNotification(String title, String message) {
        Platform.runLater(() -> {
            QUEUE.offer(new Notification(title, message));
            processQueue();
        });
    }

    private static Popup createPopup(Notification notification) {
        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(false);
        popup.setHideOnEscape(true);

        VBox container = new VBox(6);
        container.setPrefWidth(WIDTH);
        container.setMaxWidth(WIDTH);
        container.setPadding(new Insets(16));
        container.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: #E5E7EB;" +
                "-fx-border-radius: 6;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);"
        );

        Label titleLabel = new Label(notification.title);
        titleLabel.setWrapText(true);
        titleLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #222222;"
        );

        Label messageLabel = new Label(notification.message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(WIDTH - 50);
        messageLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #666666;" +
                "-fx-line-spacing: 2px;"
        );

        Label closeButton = new Label("×");
        closeButton.setMinSize(24, 24);
        closeButton.setAlignment(Pos.CENTER);
        closeButton.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-text-fill: #888888;" +
                "-fx-cursor: hand;"
        );

        closeButton.setOnMouseEntered(event ->
                closeButton.setStyle(
                        "-fx-font-size: 18px;" +
                        "-fx-text-fill: #222222;" +
                        "-fx-cursor: hand;"
                )
        );

        closeButton.setOnMouseExited(event ->
                closeButton.setStyle(
                        "-fx-font-size: 18px;" +
                        "-fx-text-fill: #888888;" +
                        "-fx-cursor: hand;"
                )
        );

        VBox text = new VBox(5, titleLabel, messageLabel);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox content = new HBox(10, text, closeButton);
        content.setAlignment(Pos.TOP_LEFT);

        container.getChildren().add(content);
        popup.getContent().add(container);

        closeButton.setOnMouseClicked(event -> dismiss(popup));

        return popup;
    }

    private static void processQueue() {
        if (processing || QUEUE.isEmpty()) {
            return;
        }

        Stage stage =
                com.bank.desktop.BankingApplication.getPrimaryStage();

        if (stage == null || !stage.isShowing()) {
            return;
        }

        Notification notification = QUEUE.poll();

        if (notification == null) {
            return;
        }

        processing = true;

        Popup popup = createPopup(notification);

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(DURATION),
                        event -> dismiss(popup)
                )
        );

        popup.setOnHidden(event -> {
            timeline.stop();
            processing = false;
            Platform.runLater(NotificationUtil::processQueue);
        });

        popup.show(stage);

        popup.setX(stage.getX() + stage.getWidth() - WIDTH - 24);
        popup.setY(stage.getY() + stage.getHeight() - 90);

        timeline.play();
    }

    private static void dismiss(Popup popup) {
        if (popup.isShowing()) {
            popup.hide();
        }
    }

    private static final class Notification {

        private final String title;
        private final String message;

        private Notification(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }
}
