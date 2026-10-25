package com.bank.desktop.controller;

import com.bank.desktop.model.Transaction;
import com.bank.desktop.service.TransactionService;
import com.bank.desktop.util.NotificationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class TransactionsController implements Initializable {

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> colReference;
    @FXML private TableColumn<Transaction, String> colTime;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colAccount;
    @FXML private TableColumn<Transaction, String> colAmount;
    @FXML private TableColumn<Transaction, String> colStatus;
    @FXML private TableColumn<Transaction, String> colActions;
    @FXML private TextField searchField;
    @FXML private Label statsTotal;
    @FXML private Label statsVolume;
    @FXML private Label statsPending;
    @FXML private Label statsReversed;

    private final TransactionService service = new TransactionService();
    private final ObservableList<Transaction> master = FXCollections.observableArrayList();
    private FilteredList<Transaction> filtered;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        configureActions();
        filtered = new FilteredList<>(master, p -> true);
        transactionTable.setItems(filtered);
        if (searchField != null) searchField.textProperty().addListener((obs, o, n) -> applySearch());
        loadFromBackend();
    }

    private void configureColumns() {
        colReference.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getTransactionReference())));
        colTime.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getTransactionDate())));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeLabel()));
        colAccount.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getAccountNumber())));
        colAmount.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedAmount()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusLabel()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(s);
                b.getStyleClass().add("badge");
                switch (s) {
                    case "Completed" -> b.getStyleClass().add("badge-active");
                    case "Pending" -> b.getStyleClass().add("badge-pending");
                    case "Failed", "Reversed" -> b.getStyleClass().add("badge-inactive");
                    default -> b.getStyleClass().add("badge-info");
                }
                setGraphic(b); setText(null);
            }
        });
    }

    private void configureActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button view = new Button("View");
            private final Button reverse = new Button("Reverse");
            private final HBox box = new HBox(6, view, reverse);
            {
                view.getStyleClass().addAll("btn-ghost", "btn-sm");
                reverse.getStyleClass().addAll("btn-ghost", "btn-sm");
                view.setOnAction(e -> showDetails(get()));
                reverse.setOnAction(e -> confirmReverse(get()));
            }
            private Transaction get() { return getTableView().getItems().get(getIndex()); }
            @Override protected void updateItem(String i, boolean empty) {
                super.updateItem(i, empty);
                setGraphic(empty ? null : box);
                setText(null);
            }
        });
    }

    private void loadFromBackend() {
        CompletableFuture.supplyAsync(() -> {
            try { return service.list(0, 500); }
            catch (Exception e) { log.error("Load tx failed", e); return null; }
        }).thenAccept(list -> Platform.runLater(() -> {
            if (list == null) {
                NotificationUtil.showError("Load Failed", "Could not retrieve transactions.");
                return;
            }
            master.setAll(list);
            refreshStats();
        }));
    }

    private void refreshStats() {
        if (statsTotal == null) return;
        long total = master.size();
        long pending = master.stream().filter(t -> "PENDING".equals(t.getStatus())).count();
        long reversed = master.stream().filter(t -> "REVERSED".equals(t.getStatus())).count();
        java.math.BigDecimal sum = master.stream()
                .map(t -> t.getAmount() == null ? java.math.BigDecimal.ZERO : t.getAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        statsTotal.setText(Long.toString(total));
        statsVolume.setText(String.format("%,.0f", sum));
        statsPending.setText(Long.toString(pending));
        statsReversed.setText(Long.toString(reversed));
    }

    private void applySearch() {
        String term = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(t -> term.isEmpty()
                || contains(t.getTransactionReference(), term)
                || contains(t.getAccountNumber(), term)
                || contains(t.getDescription(), term)
                || contains(t.getTypeLabel(), term));
    }

    private void showDetails(Transaction t) {
        if (t == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction Details");
        alert.setHeaderText(t.getTransactionReference());
        alert.setContentText(
                "Type:      " + t.getTypeLabel() + "\n"
              + "Account:   " + nz(t.getAccountNumber()) + "\n"
              + "Amount:    " + t.getFormattedAmount() + " " + nz(t.getCurrencyCode()) + "\n"
              + "Date:      " + nz(t.getTransactionDate()) + "\n"
              + "Status:    " + t.getStatusLabel() + "\n"
              + "Description: " + nz(t.getDescription()));
        alert.showAndWait();
    }

    private void confirmReverse(Transaction t) {
        if (t == null) return;
        if (!"COMPLETED".equals(t.getStatus())) {
            NotificationUtil.showInfo("Not Applicable", "Only completed transactions can be reversed.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("Reversed by staff");
        dialog.setTitle("Reverse Transaction");
        dialog.setHeaderText("Reverse " + t.getTransactionReference() + "?");
        dialog.setContentText("Reason:");
        dialog.showAndWait().ifPresent(reason ->
            CompletableFuture.supplyAsync(() -> {
                try { service.reverse(t.getId(), reason); return Boolean.TRUE; }
                catch (Exception e) { log.error("Reverse failed", e); return e; }
            }).thenAccept(res -> Platform.runLater(() -> {
                if (res instanceof Exception ex) {
                    NotificationUtil.showError("Reverse Failed", ex.getMessage());
                    return;
                }
                NotificationUtil.showSuccess("Transaction Reversed", "The transaction has been reversed.");
                loadFromBackend();
            }))
        );
    }

    @FXML
    private void onNewTransaction() {
        TransactionFormDialog.show().ifPresent(form ->
            CompletableFuture.supplyAsync(() -> {
                try {
                    if ("Deposit".equals(form.type))
                        return service.deposit(form.accountNumber, form.amount, form.description);
                    if ("Withdrawal".equals(form.type))
                        return service.withdrawal(form.accountNumber, form.amount, form.description);
                    return service.transfer(form.accountNumber, form.destinationAccount, form.amount, form.description);
                } catch (Exception e) { log.error("Transaction failed", e); return e; }
            }).thenAccept(res -> Platform.runLater(() -> {
                if (res instanceof Exception ex) {
                    NotificationUtil.showError("Transaction Failed", ex.getMessage());
                    return;
                }
                NotificationUtil.showSuccess("Transaction Processed", "The transaction completed successfully.");
                loadFromBackend();
            }))
        );
    }

    @FXML private void onRefresh() { loadFromBackend(); }

    private static String nz(String s) { return s == null ? "" : s; }
    private static boolean contains(String v, String t) {
        return v != null && v.toLowerCase().contains(t);
    }
}