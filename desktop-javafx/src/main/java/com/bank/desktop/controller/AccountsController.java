package com.bank.desktop.controller;

import com.bank.desktop.model.Account;
import com.bank.desktop.model.Customer;
import com.bank.desktop.service.AccountService;
import com.bank.desktop.service.CustomerService;
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
public class AccountsController implements Initializable {

    @FXML private TableView<Account> accountTable;
    @FXML private TableColumn<Account, String> colAccountNumber;
    @FXML private TableColumn<Account, String> colCustomer;
    @FXML private TableColumn<Account, String> colType;
    @FXML private TableColumn<Account, String> colCurrency;
    @FXML private TableColumn<Account, String> colBalance;
    @FXML private TableColumn<Account, String> colOpened;
    @FXML private TableColumn<Account, String> colStatus;
    @FXML private TableColumn<Account, String> colActions;
    @FXML private TextField searchField;
    @FXML private Label statsTotal;
    @FXML private Label statsBalance;
    @FXML private Label statsFrozen;
    @FXML private Label statsActive;

    private final AccountService accountService = new AccountService();
    private final CustomerService customerService = new CustomerService();
    private final ObservableList<Account> master = FXCollections.observableArrayList();
    private FilteredList<Account> filtered;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        configureActions();

        filtered = new FilteredList<>(master, p -> true);
        accountTable.setItems(filtered);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applySearch());
        }

        loadFromBackend();
    }

    private void configureColumns() {
        colAccountNumber.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getAccountNumber())));
        colCustomer.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getCustomerName())));
        colType.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getAccountTypeName())));
        colCurrency.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getCurrencyCode())));
        colBalance.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedBalance()));
        colOpened.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getOpeningDate())));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusLabel()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(s);
                b.getStyleClass().add("badge");
                switch (s) {
                    case "Active" -> b.getStyleClass().add("badge-active");
                    case "Frozen", "Dormant" -> b.getStyleClass().add("badge-pending");
                    case "Closed" -> b.getStyleClass().add("badge-inactive");
                    default -> b.getStyleClass().add("badge-info");
                }
                setGraphic(b); setText(null);
            }
        });
    }

    private void configureActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button view = new Button("View");
            private final Button freeze = new Button("Freeze");
            private final Button activate = new Button("Activate");
            private final HBox box = new HBox(6, view, freeze, activate);
            {
                view.getStyleClass().addAll("btn-ghost", "btn-sm");
                freeze.getStyleClass().addAll("btn-ghost", "btn-sm");
                activate.getStyleClass().addAll("btn-ghost", "btn-sm");
                view.setOnAction(e -> showDetails(get()));
                freeze.setOnAction(e -> onFreeze(get()));
                activate.setOnAction(e -> onActivate(get()));
            }
            private Account get() { return getTableView().getItems().get(getIndex()); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setText(null);
            }
        });
    }

    private void loadFromBackend() {
        CompletableFuture.supplyAsync(() -> {
            try { return accountService.list(0, 500); }
            catch (Exception e) { log.error("Load accounts failed", e); return null; }
        }).thenAccept(list -> Platform.runLater(() -> {
            if (list == null) {
                NotificationUtil.showError("Load Failed", "Could not retrieve accounts.");
                return;
            }
            master.setAll(list);
            refreshStats();
        }));
    }

    private void refreshStats() {
        if (statsTotal == null) return;
        long total = master.size();
        java.math.BigDecimal sum = master.stream()
                .map(a -> a.getBalance() == null ? java.math.BigDecimal.ZERO : a.getBalance())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        long frozen = master.stream().filter(a -> "FROZEN".equals(a.getStatus())).count();
        long active = master.stream().filter(a -> "ACTIVE".equals(a.getStatus())).count();
        statsTotal.setText(Long.toString(total));
        statsBalance.setText(String.format("%,.0f", sum));
        statsFrozen.setText(Long.toString(frozen));
        statsActive.setText(Long.toString(active));
    }

    private void applySearch() {
        String term = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(a -> term.isEmpty()
                || contains(a.getAccountNumber(), term)
                || contains(a.getCustomerName(), term)
                || contains(a.getAccountTypeName(), term));
    }

    private void showDetails(Account a) {
        if (a == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Details");
        alert.setHeaderText(a.getAccountNumber());
        alert.setContentText(
                "Customer:      " + nz(a.getCustomerName()) + "\n"
              + "Type:          " + nz(a.getAccountTypeName()) + "\n"
              + "Branch:        " + nz(a.getBranchName()) + "\n"
              + "Currency:      " + nz(a.getCurrencyCode()) + "\n"
              + "Balance:       " + a.getFormattedBalance() + "\n"
              + "Available:     " + (a.getAvailableBalance() != null ? String.format("%,.2f", a.getAvailableBalance()) : "0.00") + "\n"
              + "Opened:        " + nz(a.getOpeningDate()) + "\n"
              + "Status:        " + a.getStatusLabel());
        alert.showAndWait();
    }

    private void onFreeze(Account a) {
        if (a == null || !"ACTIVE".equals(a.getStatus())) {
            NotificationUtil.showInfo("Not Applicable", "Only active accounts can be frozen.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("Frozen by staff");
        dialog.setTitle("Freeze Account");
        dialog.setHeaderText("Freeze " + a.getAccountNumber() + "?");
        dialog.setContentText("Reason:");
        dialog.showAndWait().ifPresent(reason -> CompletableFuture.supplyAsync(() -> {
            try { accountService.freeze(a.getId(), reason); return Boolean.TRUE; }
            catch (Exception e) { log.error("Freeze failed", e); return e; }
        }).thenAccept(res -> Platform.runLater(() -> {
            if (res instanceof Exception ex) {
                NotificationUtil.showError("Freeze Failed", ex.getMessage());
                return;
            }
            NotificationUtil.showSuccess("Account Frozen", a.getAccountNumber() + " has been frozen.");
            loadFromBackend();
        })));
    }

    private void onActivate(Account a) {
        if (a == null || "ACTIVE".equals(a.getStatus())) {
            NotificationUtil.showInfo("Not Applicable", "Account is already active.");
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            try { accountService.activate(a.getId()); return Boolean.TRUE; }
            catch (Exception e) { log.error("Activate failed", e); return e; }
        }).thenAccept(res -> Platform.runLater(() -> {
            if (res instanceof Exception ex) {
                NotificationUtil.showError("Activate Failed", ex.getMessage());
                return;
            }
            NotificationUtil.showSuccess("Account Activated", a.getAccountNumber() + " is now active.");
            loadFromBackend();
        }));
    }

    @FXML
    private void onOpenAccount() {
        CompletableFuture.supplyAsync(() -> {
            try { return customerService.list(0, 500); }
            catch (Exception e) { log.error("Load customers for dialog failed", e); return null; }
        }).thenAccept(customers -> Platform.runLater(() -> {
            if (customers == null || customers.isEmpty()) {
                NotificationUtil.showError("Cannot Open Account", "Register at least one customer first.");
                return;
            }
            AccountFormDialog.showCreate(customers).ifPresent(result ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return accountService.open(
                                result.customerId, result.accountTypeCode,
                                result.branchId, result.currencyCode,
                                result.accountName, result.initialDeposit);
                    } catch (Exception e) { log.error("Open account failed", e); return e; }
                }).thenAccept(created -> Platform.runLater(() -> {
                    if (created instanceof Exception ex) {
                        NotificationUtil.showError("Open Account Failed", ex.getMessage());
                        return;
                    }
                    NotificationUtil.showSuccess("Account Opened", "New account created successfully.");
                    loadFromBackend();
                }))
            );
        }));
    }

    @FXML private void onRefresh() { loadFromBackend(); }

    private static String nz(String s) { return s == null ? "" : s; }
    private static boolean contains(String v, String t) {
        return v != null && v.toLowerCase().contains(t);
    }
}