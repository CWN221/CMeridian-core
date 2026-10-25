package com.bank.desktop.controller;

import com.bank.desktop.model.*;
import com.bank.desktop.service.AccountService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class AccountFormDialog {

    public static Optional<AccountFormResult> showCreate(List<Customer> customers) {
        Dialog<AccountFormResult> dialog = new Dialog<>();
        dialog.setTitle("Open Account");
        dialog.setHeaderText("Open a new account for an existing customer");

        ButtonType ok = new ButtonType("Open", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        ComboBox<Customer> customerBox = new ComboBox<>();
        customerBox.getItems().setAll(customers);
        customerBox.setCellFactory(lv -> customerCell());
        customerBox.setButtonCell(customerCell());
        customerBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<AccountTypeRef> typeBox = new ComboBox<>();
        ComboBox<BranchRef> branchBox = new ComboBox<>();
        ComboBox<CurrencyRef> currencyBox = new ComboBox<>();
        typeBox.setMaxWidth(Double.MAX_VALUE);
        branchBox.setMaxWidth(Double.MAX_VALUE);
        currencyBox.setMaxWidth(Double.MAX_VALUE);

        TextField accountName = new TextField();
        TextField initialDeposit = new TextField();

        try {
            AccountService svc = new AccountService();
            typeBox.getItems().setAll(svc.accountTypes());
            branchBox.getItems().setAll(svc.branches());
            currencyBox.getItems().setAll(svc.currencies());
            if (!typeBox.getItems().isEmpty()) typeBox.getSelectionModel().selectFirst();
            if (!branchBox.getItems().isEmpty()) branchBox.getSelectionModel().selectFirst();
            if (!currencyBox.getItems().isEmpty()) {
                currencyBox.getItems().stream()
                        .filter(c -> "KES".equals(c.getCode())).findFirst()
                        .ifPresentOrElse(c -> currencyBox.setValue(c),
                                () -> currencyBox.getSelectionModel().selectFirst());
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load dropdowns: " + e.getMessage()).showAndWait();
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        int r = 0;
        grid.addRow(r++, new Label("Customer *"), customerBox);
        grid.addRow(r++, new Label("Account Type *"), typeBox);
        grid.addRow(r++, new Label("Branch *"), branchBox);
        grid.addRow(r++, new Label("Currency *"), currencyBox);
        grid.addRow(r++, new Label("Account Name"), accountName);
        grid.addRow(r++, new Label("Initial Deposit"), initialDeposit);

        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ok);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (customerBox.getValue() == null || typeBox.getValue() == null
                    || branchBox.getValue() == null || currencyBox.getValue() == null) {
                evt.consume();
                new Alert(Alert.AlertType.WARNING, "Customer, type, branch and currency are required.").showAndWait();
                return;
            }
            String dep = initialDeposit.getText() == null ? "" : initialDeposit.getText().trim();
            if (!dep.isEmpty()) {
                try { new BigDecimal(dep); }
                catch (NumberFormatException ex) {
                    evt.consume();
                    new Alert(Alert.AlertType.WARNING, "Initial deposit must be a valid number.").showAndWait();
                }
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != ok) return null;
            AccountFormResult res = new AccountFormResult();
            res.customerId = customerBox.getValue().getId();
            res.accountTypeCode = typeBox.getValue().getCode();
            res.branchId = branchBox.getValue().getId();
            res.currencyCode = currencyBox.getValue().getCode();
            res.accountName = accountName.getText().isBlank() ? null : accountName.getText().trim();
            String dep = initialDeposit.getText().trim();
            res.initialDeposit = dep.isEmpty() ? null : new BigDecimal(dep);
            return res;
        });

        return dialog.showAndWait();
    }

    private static ListCell<Customer> customerCell() {
        return new ListCell<>() {
            @Override protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getFullName() + " (" + c.getCustomerNumber() + ")");
            }
        };
    }

    public static class AccountFormResult {
        public Long customerId;
        public String accountTypeCode;
        public Long branchId;
        public String currencyCode;
        public String accountName;
        public BigDecimal initialDeposit;
    }
}