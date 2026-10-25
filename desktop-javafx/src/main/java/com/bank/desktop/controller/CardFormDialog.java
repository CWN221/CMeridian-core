package com.bank.desktop.controller;

import com.bank.desktop.model.Account;
import com.bank.desktop.model.Customer;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CardFormDialog {

    public static Optional<CardFormResult> showIssue(List<Customer> customers, List<Account> accounts) {
        Dialog<CardFormResult> dialog = new Dialog<>();
        dialog.setTitle("Issue Card");
        dialog.setHeaderText("Issue a new card linked to a customer account");

        ButtonType ok = new ButtonType("Issue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        ComboBox<Customer> customerBox = new ComboBox<>();
        customerBox.getItems().setAll(customers);
        customerBox.setMaxWidth(Double.MAX_VALUE);
        customerBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getFullName());
            }
        });
        customerBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getFullName());
            }
        });

        ComboBox<Account> accountBox = new ComboBox<>();
        accountBox.setMaxWidth(Double.MAX_VALUE);
        accountBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Account a, boolean empty) {
                super.updateItem(a, empty);
                setText(empty || a == null ? null : a.getAccountNumber());
            }
        });
        accountBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Account a, boolean empty) {
                super.updateItem(a, empty);
                setText(empty || a == null ? null : a.getAccountNumber());
            }
        });

        customerBox.valueProperty().addListener((obs, o, c) -> {
            if (c == null) { accountBox.getItems().clear(); return; }
            List<Account> owned = accounts.stream()
                    .filter(a -> a.getCustomerId() != null && a.getCustomerId().equals(c.getId()))
                    .collect(Collectors.toList());
            accountBox.getItems().setAll(owned);
            if (!owned.isEmpty()) accountBox.getSelectionModel().selectFirst();
        });

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("DEBIT", "CREDIT", "PREPAID");
        typeBox.getSelectionModel().selectFirst();
        typeBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> brandBox = new ComboBox<>();
        brandBox.getItems().addAll("VISA", "MASTERCARD");
        brandBox.getSelectionModel().selectFirst();
        brandBox.setMaxWidth(Double.MAX_VALUE);

        TextField daily = new TextField("50000");
        TextField monthly = new TextField("500000");
        TextField single = new TextField("20000");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        int r = 0;
        grid.addRow(r++, new Label("Customer *"), customerBox);
        grid.addRow(r++, new Label("Account *"), accountBox);
        grid.addRow(r++, new Label("Card Type *"), typeBox);
        grid.addRow(r++, new Label("Brand *"), brandBox);
        grid.addRow(r++, new Label("Daily Limit"), daily);
        grid.addRow(r++, new Label("Monthly Limit"), monthly);
        grid.addRow(r++, new Label("Single Txn Limit"), single);

        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ok);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (customerBox.getValue() == null || accountBox.getValue() == null) {
                evt.consume();
                new Alert(Alert.AlertType.WARNING, "Select customer and account.").showAndWait();
                return;
            }
            try {
                if (!daily.getText().isBlank()) new BigDecimal(daily.getText().trim());
                if (!monthly.getText().isBlank()) new BigDecimal(monthly.getText().trim());
                if (!single.getText().isBlank()) new BigDecimal(single.getText().trim());
            } catch (NumberFormatException e) {
                evt.consume();
                new Alert(Alert.AlertType.WARNING, "Limits must be valid numbers.").showAndWait();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != ok) return null;
            CardFormResult res = new CardFormResult();
            res.customerId = customerBox.getValue().getId();
            res.accountId = accountBox.getValue().getId();
            res.cardType = typeBox.getValue();
            res.cardBrand = brandBox.getValue();
            res.dailyLimit = daily.getText().isBlank() ? null : new BigDecimal(daily.getText().trim());
            res.monthlyLimit = monthly.getText().isBlank() ? null : new BigDecimal(monthly.getText().trim());
            res.singleLimit = single.getText().isBlank() ? null : new BigDecimal(single.getText().trim());
            return res;
        });

        return dialog.showAndWait();
    }

    public static class CardFormResult {
        public Long customerId;
        public Long accountId;
        public String cardType;
        public String cardBrand;
        public BigDecimal dailyLimit;
        public BigDecimal monthlyLimit;
        public BigDecimal singleLimit;
    }
}