package com.bank.desktop.controller;

import com.bank.desktop.model.ReportRecord;
import com.bank.desktop.service.ReportService;
import com.bank.desktop.util.NotificationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ReportsController implements Initializable {

    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TableView<ReportRecord> reportsTable;
    @FXML private TableColumn<ReportRecord, String> colReportName;
    @FXML private TableColumn<ReportRecord, String> colPeriod;
    @FXML private TableColumn<ReportRecord, String> colGeneratedOn;
    @FXML private TableColumn<ReportRecord, String> colGeneratedBy;
    @FXML private TableColumn<ReportRecord, String> colRows;
    @FXML private TableColumn<ReportRecord, String> colActions;

    private final ReportService reportService = new ReportService();
    private final ObservableList<ReportRecord> history = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reportTypeCombo.setItems(FXCollections.observableArrayList(
                "DAILY_TRANSACTIONS",
                "CUSTOMER_PORTFOLIO",
                "ACCOUNT_BALANCES",
                "LOAN_PORTFOLIO"
        ));
        reportTypeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayName(item));
            }
        });
        reportTypeCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayName(item));
            }
        });
        reportTypeCombo.getSelectionModel().selectFirst();

        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        configureColumns();
        configureActions();
        reportsTable.setItems(history);

        loadHistory();
    }

    private void configureColumns() {
        colReportName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayName()));
        colPeriod.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getPeriodLabel())));
        colGeneratedOn.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getGeneratedOn())));
        colGeneratedBy.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getGeneratedBy())));
        colRows.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRowCount() == null ? "0" : c.getValue().getRowCount().toString()));
    }

    private void configureActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button view = new Button("View");
            private final Button delete = new Button("Delete");
            private final HBox box = new HBox(6, view, delete);
            {
                view.getStyleClass().addAll("btn-ghost", "btn-sm");
                delete.getStyleClass().addAll("btn-ghost", "btn-sm");
                view.setOnAction(e -> showReport(getRow()));
                delete.setOnAction(e -> confirmDelete(getRow()));
            }
            private ReportRecord getRow() { return getTableView().getItems().get(getIndex()); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setText(null);
            }
        });
    }

    private void loadHistory() {
        CompletableFuture.supplyAsync(() -> {
            try { return reportService.history(0, 100); }
            catch (Exception e) { log.error("Load report history failed", e); return null; }
        }).thenAccept(list -> Platform.runLater(() -> {
            if (list == null) {
                NotificationUtil.showError("Load Failed", "Could not load report history.");
                return;
            }
            history.setAll(list);
        }));
    }

    @FXML
    private void onGenerate() {
        String type = reportTypeCombo.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (type == null || start == null || end == null) {
            NotificationUtil.showError("Missing Fields", "Select report type and date range.");
            return;
        }
        if (end.isBefore(start)) {
            NotificationUtil.showError("Invalid Range", "End date must be on or after start date.");
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try { return reportService.generate(type, start, end); }
            catch (Exception e) { log.error("Generate report failed", e); return e; }
        }).thenAccept(res -> Platform.runLater(() -> {
            if (res instanceof Exception ex) {
                NotificationUtil.showError("Generate Failed", ex.getMessage());
                return;
            }
            ReportRecord record = (ReportRecord) res;
            NotificationUtil.showSuccess("Report Generated",
                    record.getDisplayName() + " - " + record.getRowCount() + " rows");
            loadHistory();
            showReport(record);
        }));
    }

    @FXML private void onRefresh() { loadHistory(); }

    private void showReport(ReportRecord report) {
        if (report == null) return;

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(report.getDisplayName() + " - " + nz(report.getPeriodLabel()));

        VBox root = new VBox(16);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #fafaf9;");

        Label header = new Label(report.getDisplayName());
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #18181b;");

        Label meta = new Label("Period: " + nz(report.getPeriodLabel())
                + "    Generated: " + nz(report.getGeneratedOn())
                + "    By: " + nz(report.getGeneratedBy())
                + "    Rows: " + (report.getRowCount() == null ? "0" : report.getRowCount()));
        meta.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px;");

        // Summary panel
        if (report.getSummary() != null && !report.getSummary().isEmpty()) {
            HBox summaryBox = new HBox(20);
            summaryBox.setStyle("-fx-background-color: #ffffff; -fx-padding: 16; -fx-background-radius: 8; " +
                    "-fx-border-color: #e7e5e4; -fx-border-radius: 8;");
            for (Map.Entry<String, Object> e : report.getSummary().entrySet()) {
                VBox item = new VBox(4);
                Label k = new Label(prettyKey(e.getKey()));
                k.setStyle("-fx-font-size: 11px; -fx-text-fill: #71717a;");
                Label v = new Label(e.getValue() == null ? "-" : e.getValue().toString());
                v.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #18181b;");
                item.getChildren().addAll(k, v);
                summaryBox.getChildren().add(item);
            }
            root.getChildren().addAll(header, meta, summaryBox);
        } else {
            root.getChildren().addAll(header, meta);
        }

        // Rows table
        TableView<Map<String, Object>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        if (report.getRows() != null && !report.getRows().isEmpty()) {
            Map<String, Object> first = report.getRows().get(0);
            for (String key : first.keySet()) {
                TableColumn<Map<String, Object>, String> col = new TableColumn<>(prettyKey(key));
                col.setCellValueFactory(c -> {
                    Object v = c.getValue().get(key);
                    return new SimpleStringProperty(v == null ? "" : v.toString());
                });
                col.setPrefWidth(150);
                table.getColumns().add(col);
            }
            table.setItems(FXCollections.observableArrayList(report.getRows()));
        }

        ScrollPane scroll = new ScrollPane(table);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(400);
        VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);
        root.getChildren().add(scroll);

        Button close = new Button("Close");
        close.getStyleClass().add("btn-primary");
        close.setOnAction(e -> stage.close());
        HBox footer = new HBox(close);
        footer.setStyle("-fx-alignment: center-right;");
        root.getChildren().add(footer);

        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void confirmDelete(ReportRecord report) {
        if (report == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Report");
        confirm.setHeaderText("Delete " + report.getDisplayName() + "?");
        confirm.setContentText("The report will be removed from history. This cannot be undone.");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            CompletableFuture.supplyAsync(() -> {
                try { reportService.delete(report.getId()); return Boolean.TRUE; }
                catch (Exception e) { return e; }
            }).thenAccept(res -> Platform.runLater(() -> {
                if (res instanceof Exception ex) {
                    NotificationUtil.showError("Delete Failed", ex.getMessage());
                    return;
                }
                NotificationUtil.showSuccess("Report Deleted", "Removed from history.");
                loadHistory();
            }));
        });
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String displayName(String type) {
        return switch (type) {
            case "DAILY_TRANSACTIONS" -> "Daily Transaction Report";
            case "CUSTOMER_PORTFOLIO" -> "Customer Portfolio Report";
            case "ACCOUNT_BALANCES" -> "Account Balance Summary";
            case "LOAN_PORTFOLIO" -> "Loan Portfolio Report";
            default -> type;
        };
    }

    private static String prettyKey(String key) {
        if (key == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) sb.append(' ');
            sb.append(Character.toLowerCase(c));
        }
        String s = sb.toString();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}