package com.bank.desktop.controller;

import com.bank.desktop.model.Account;
import com.bank.desktop.model.Card;
import com.bank.desktop.model.Customer;
import com.bank.desktop.service.AccountService;
import com.bank.desktop.service.CardService;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class CardsController implements Initializable {

    @FXML private TableView<Card> cardTable;
    @FXML private TableColumn<Card, String> colCardNumber;
    @FXML private TableColumn<Card, String> colHolder;
    @FXML private TableColumn<Card, String> colType;
    @FXML private TableColumn<Card, String> colBrand;
    @FXML private TableColumn<Card, String> colExpiry;
    @FXML private TableColumn<Card, String> colAccount;
    @FXML private TableColumn<Card, String> colStatus;
    @FXML private TableColumn<Card, String> colActions;
    @FXML private TextField searchField;
    @FXML private Label statsTotal;
    @FXML private Label statsActive;
    @FXML private Label statsBlocked;
    @FXML private Label statsCancelled;

    private final CardService cardService = new CardService();
    private final CustomerService customerService = new CustomerService();
    private final AccountService accountService = new AccountService();
    private final ObservableList<Card> master = FXCollections.observableArrayList();
    private FilteredList<Card> filtered;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        configureActions();
        filtered = new FilteredList<>(master, p -> true);
        cardTable.setItems(filtered);
        if (searchField != null) searchField.textProperty().addListener((obs, o, n) -> applySearch());
        loadFromBackend();
    }

    private void configureColumns() {
        colCardNumber.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaskedNumber()));
        colHolder.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getCardholderName())));
        colType.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getCardType())));
        colBrand.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getCardBrand())));
        colExpiry.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getExpiryDate())));
        colAccount.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getAccountNumber())));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusLabel()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(s);
                b.getStyleClass().add("badge");
                switch (s) {
                    case "Active" -> b.getStyleClass().add("badge-active");
                    case "Blocked" -> b.getStyleClass().add("badge-pending");
                    case "Cancelled", "Expired" -> b.getStyleClass().add("badge-inactive");
                    default -> b.getStyleClass().add("badge-info");
                }
                setGraphic(b); setText(null);
            }
        });
    }

    private void configureActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button block = new Button("Block");
            private final Button unblock = new Button("Unblock");
            private final Button cancel = new Button("Cancel");
            private final HBox box = new HBox(6, block, unblock, cancel);
            {
                block.getStyleClass().addAll("btn-ghost", "btn-sm");
                unblock.getStyleClass().addAll("btn-ghost", "btn-sm");
                cancel.getStyleClass().addAll("btn-ghost", "btn-sm");
                block.setOnAction(e -> onBlock(get()));
                unblock.setOnAction(e -> onUnblock(get()));
                cancel.setOnAction(e -> onCancel(get()));
            }
            private Card get() { return getTableView().getItems().get(getIndex()); }
            @Override protected void updateItem(String i, boolean empty) {
                super.updateItem(i, empty);
                setGraphic(empty ? null : box);
                setText(null);
            }
        });
    }

    private void loadFromBackend() {
        CompletableFuture.supplyAsync(() -> {
            try { return cardService.list(0, 500); }
            catch (Exception e) { log.error("Load cards failed", e); return null; }
        }).thenAccept(list -> Platform.runLater(() -> {
            if (list == null) {
                NotificationUtil.showError("Load Failed", "Could not retrieve cards.");
                return;
            }
            master.setAll(list);
            refreshStats();
        }));
    }

    private void refreshStats() {
        if (statsTotal == null) return;
        long total = master.size();
        long active = master.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count();
        long blocked = master.stream().filter(c -> "BLOCKED".equals(c.getStatus())).count();
        long cancelled = master.stream().filter(c -> "CANCELLED".equals(c.getStatus())).count();
        statsTotal.setText(Long.toString(total));
        statsActive.setText(Long.toString(active));
        statsBlocked.setText(Long.toString(blocked));
        statsCancelled.setText(Long.toString(cancelled));
    }

    private void applySearch() {
        String term = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(c -> term.isEmpty()
                || contains(c.getCardNumber(), term)
                || contains(c.getCardholderName(), term)
                || contains(c.getAccountNumber(), term));
    }

    private void onBlock(Card c) {
        if (c == null || !"ACTIVE".equals(c.getStatus())) {
            NotificationUtil.showInfo("Not Applicable", "Only active cards can be blocked.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("Lost or stolen");
        dialog.setTitle("Block Card");
        dialog.setHeaderText("Block " + c.getMaskedNumber());
        dialog.setContentText("Reason:");
        dialog.showAndWait().ifPresent(reason ->
            CompletableFuture.supplyAsync(() -> {
                try { return cardService.block(c.getId(), reason); }
                catch (Exception e) { return e; }
            }).thenAccept(res -> Platform.runLater(() -> {
                if (res instanceof Exception ex) {
                    NotificationUtil.showError("Block Failed", ex.getMessage());
                    return;
                }
                NotificationUtil.showSuccess("Card Blocked", c.getMaskedNumber() + " blocked.");
                loadFromBackend();
            }))
        );
    }

    private void onUnblock(Card c) {
        if (c == null || !"BLOCKED".equals(c.getStatus())) {
            NotificationUtil.showInfo("Not Applicable", "Only blocked cards can be unblocked.");
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            try { return cardService.unblock(c.getId()); }
            catch (Exception e) { return e; }
        }).thenAccept(res -> Platform.runLater(() -> {
            if (res instanceof Exception ex) {
                NotificationUtil.showError("Unblock Failed", ex.getMessage());
                return;
            }
            NotificationUtil.showSuccess("Card Unblocked", c.getMaskedNumber() + " reactivated.");
            loadFromBackend();
        }));
    }

    private void onCancel(Card c) {
        if (c == null || "CANCELLED".equals(c.getStatus())) {
            NotificationUtil.showInfo("Not Applicable", "Card is already cancelled.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Card");
        confirm.setHeaderText("Permanently cancel " + c.getMaskedNumber() + "?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            CompletableFuture.supplyAsync(() -> {
                try { return cardService.cancel(c.getId()); }
                catch (Exception e) { return e; }
            }).thenAccept(res -> Platform.runLater(() -> {
                if (res instanceof Exception ex) {
                    NotificationUtil.showError("Cancel Failed", ex.getMessage());
                    return;
                }
                NotificationUtil.showSuccess("Card Cancelled", c.getMaskedNumber() + " permanently cancelled.");
                loadFromBackend();
            }));
        });
    }

    @FXML
    private void onIssueCard() {
        CompletableFuture.supplyAsync(() -> {
            try {
                List<Customer> customers = customerService.list(0, 500);
                List<Account> accounts = accountService.list(0, 500);
                return new Object[]{customers, accounts};
            } catch (Exception e) {
                return null;
            }
        }).thenAccept(data -> Platform.runLater(() -> {
            if (data == null) {
                NotificationUtil.showError("Cannot Open Form", "Failed to load data.");
                return;
            }
            @SuppressWarnings("unchecked") List<Customer> customers = (List<Customer>) data[0];
            @SuppressWarnings("unchecked") List<Account> accounts = (List<Account>) data[1];
            if (customers.isEmpty() || accounts.isEmpty()) {
                NotificationUtil.showError("Cannot Open Form", "Register a customer and account first.");
                return;
            }
            CardFormDialog.showIssue(customers, accounts).ifPresent(form ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return cardService.issue(form.customerId, form.accountId,
                                form.cardType, form.cardBrand,
                                form.dailyLimit, form.monthlyLimit, form.singleLimit);
                    } catch (Exception e) { return e; }
                }).thenAccept(res -> Platform.runLater(() -> {
                    if (res instanceof Exception ex) {
                        NotificationUtil.showError("Issue Failed", ex.getMessage());
                        return;
                    }
                    NotificationUtil.showSuccess("Card Issued", "New card created successfully.");
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