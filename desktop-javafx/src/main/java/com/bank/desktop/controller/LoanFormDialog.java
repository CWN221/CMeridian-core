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

public class LoanFormDialog {

    public static Optional<LoanFormResult> showCreate(List<Customer> customers, List<Account> accounts) {
        Dialog<LoanFormResult> dialog = new Dialog<>();
        dialog.setTitle("New Loan Application");
        dialog.setHeaderText("Submit a loan application for review");

        ButtonType ok = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        ComboBox<Customer> customerBox = new ComboBox<>();
        customerBox.getItems().setAll(customers);
        customerBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getFullName() + " (" + c.getCustomerNumber() + ")");
            }
        });
        customerBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getFullName());
            }
        });
        customerBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Account> accountBox = new ComboBox<>();
        accountBox.setMaxWidth(Double.MAX_VALUE);
        accountBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Account a, boolean empty) {
                super.updateItem(a, empty);
                setText(empty || a == null ? null : a.getAccountNumber() + " (" + a.getAccountTypeName() + ")");
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
        typeBox.getItems().addAll("PERSONAL", "MORTGAGE", "AUTO", "BUSINESS", "EDUCATION");
        typeBox.getSelectionModel().selectFirst();
        typeBox.setMaxWidth(Double.MAX_VALUE);

        TextField principal = new TextField();
        TextField rate = new TextField("14.5");
        TextField term = new TextField("24");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        int r = 0;
        grid.addRow(r++, new Label("Customer *"), customerBox);
        grid.addRow(r++, new Label("Account *"), accountBox);
        grid.addRow(r++, new Label("Loan Type *"), typeBox);
        grid.addRow(r++, new Label("Principal *"), principal);
        grid.addRow(r++, new Label("Interest Rate % *"), rate);
        grid.addRow(r++, new Label("Term (months) *"), term);

        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ok);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (customerBox.getValue() == null || accountBox.getValue() == null) {
                evt.consume();
                new Alert(Alert.AlertType.WARNING, "Select both customer and account.").showAndWait();
                return;
            }
            try {
                BigDecimal p = new BigDecimal(principal.getText().trim());
                BigDecimal rt = new BigDecimal(rate.getText().trim());
                int tm = Integer.parseInt(term.getText().trim());
                if (p.compareTo(new BigDecimal("1000")) < 0) throw new NumberFormatException();
                if (tm < 1 || tm > 360) throw new NumberFormatException();
            } catch (Exception e) {
                evt.consume();
                new Alert(Alert.AlertType.WARNING,
                        "Enter valid principal (>= 1000), rate, and term (1 to 360).").showAndWait();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != ok) return null;
            LoanFormResult res = new LoanFormResult();
            res.customerId = customerBox.getValue().getId();
            res.accountId = accountBox.getValue().getId();
            res.loanType = typeBox.getValue();
            res.principal = new BigDecimal(principal.getText().trim());
            res.rate = new BigDecimal(rate.getText().trim());
            res.term = Integer.parseInt(term.getText().trim());
            return res;
        });

        return dialog.showAndWait();
    }

    public static class LoanFormResult {
        public Long customerId;
        public Long accountId;
        public String loanType;
        public BigDecimal principal;
        public BigDecimal rate;
        public Integer term;
    }
}