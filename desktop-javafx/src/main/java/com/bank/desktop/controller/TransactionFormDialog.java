package com.bank.desktop.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.util.Optional;

public class TransactionFormDialog {

    public static Optional<TransactionFormResult> show() {
        Dialog<TransactionFormResult> dialog = new Dialog<>();
        dialog.setTitle("New Transaction");
        dialog.setHeaderText("Process a deposit, withdrawal or transfer");

        ButtonType ok = new ButtonType("Process", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("Deposit", "Withdrawal", "Transfer");
        type.getSelectionModel().selectFirst();

        TextField account = new TextField();
        TextField destination = new TextField();
        destination.setDisable(true);
        TextField amount = new TextField();
        TextField description = new TextField();

        type.valueProperty().addListener((obs, o, n) ->
                destination.setDisable(!"Transfer".equals(n)));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        int r = 0;
        grid.addRow(r++, new Label("Type *"), type);
        grid.addRow(r++, new Label("Account *"), account);
        grid.addRow(r++, new Label("Destination"), destination);
        grid.addRow(r++, new Label("Amount *"), amount);
        grid.addRow(r++, new Label("Description"), description);

        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ok);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (account.getText().isBlank() || amount.getText().isBlank()) {
                evt.consume();
                new Alert(Alert.AlertType.WARNING, "Account and amount are required.").showAndWait();
                return;
            }
            try {
                BigDecimal amt = new BigDecimal(amount.getText().trim());
                if (amt.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                evt.consume();
                new Alert(Alert.AlertType.WARNING, "Amount must be a positive number.").showAndWait();
                return;
            }
            if ("Transfer".equals(type.getValue()) && destination.getText().isBlank()) {
                evt.consume();
                new Alert(Alert.AlertType.WARNING, "Destination account is required for a transfer.").showAndWait();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != ok) return null;
            TransactionFormResult res = new TransactionFormResult();
            res.type = type.getValue();
            res.accountNumber = account.getText().trim();
            res.destinationAccount = destination.getText().isBlank() ? null : destination.getText().trim();
            res.amount = new BigDecimal(amount.getText().trim());
            res.description = description.getText().isBlank() ? null : description.getText().trim();
            return res;
        });

        return dialog.showAndWait();
    }

    public static class TransactionFormResult {
        public String type;
        public String accountNumber;
        public String destinationAccount;
        public BigDecimal amount;
        public String description;
    }
}