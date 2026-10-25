package com.bank.desktop.controller;

import com.bank.desktop.BankingApplication;
import com.bank.desktop.model.DashboardSnapshot;
import com.bank.desktop.service.DashboardService;
import com.bank.desktop.util.NotificationUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class DashboardController implements Initializable {

    @FXML private Label lblCustomers;
    @FXML private Label lblAccounts;
    @FXML private Label lblTransactions;
    @FXML private Label lblLoans;

    @FXML private Label lblDeposits;
    @FXML private Label lblLoanPortfolio;
    @FXML private Label lblBlocked;
    @FXML private Label lblFrozen;

    @FXML private VBox activityList;
    @FXML private VBox branchList;

    private final DashboardService service = new DashboardService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDashboard();
    }

    @FXML
    private void refresh() {
        loadDashboard();
    }

    private void loadDashboard() {
        CompletableFuture.supplyAsync(() -> {
            try { return service.fetch(); }
            catch (Exception e) {
                log.error("Dashboard load failed", e);
                return e;
            }
        }).thenAccept(result -> Platform.runLater(() -> {
            if (result instanceof Exception ex) {
                NotificationUtil.showError("Dashboard Failed", ex.getMessage());
                return;
            }
            render((DashboardSnapshot) result);
        }));
    }

    private void render(DashboardSnapshot d) {
        lblCustomers.setText(Long.toString(d.getActiveCustomers()));
        lblAccounts.setText(Long.toString(d.getTotalAccounts()));
        lblTransactions.setText(Long.toString(d.getTransactionsToday()));
        lblLoans.setText(Long.toString(d.getPendingLoans()));

        if (lblDeposits != null) lblDeposits.setText(d.formattedDeposits());
        if (lblLoanPortfolio != null) lblLoanPortfolio.setText(d.formattedLoanPortfolio());
        if (lblBlocked != null) lblBlocked.setText(Long.toString(d.getBlockedCards()));
        if (lblFrozen != null) lblFrozen.setText(Long.toString(d.getFrozenAccounts()));

        renderActivity(d);
        renderBranches(d);
    }

    private void renderActivity(DashboardSnapshot d) {
        if (activityList == null) return;
        activityList.getChildren().clear();

        if (d.getRecentTransactions() == null || d.getRecentTransactions().isEmpty()) {
            Label empty = new Label("No transactions yet. Open an account and post a deposit to see activity here.");
            empty.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 12px; -fx-padding: 20;");
            activityList.getChildren().add(empty);
            return;
        }

        for (Map<String, Object> row : d.getRecentTransactions()) {
            HBox item = new HBox(16);
            item.setStyle("-fx-padding: 12 0 12 0; -fx-border-color: transparent transparent #eef1f5 transparent; " +
                    "-fx-border-width: 0 0 1 0; -fx-alignment: center-left;");

            Label ref = new Label(asText(row.get("reference")));
            ref.setStyle("-fx-text-fill: #78909c; -fx-font-size: 12px; -fx-min-width: 160;");

            Label type = new Label(asText(row.get("type")));
            type.setStyle("-fx-text-fill: #263238; -fx-font-size: 13px; -fx-font-weight: 600; -fx-min-width: 130;");

            Label amount = new Label(formatAmount(row.get("amount")));
            amount.setStyle("-fx-text-fill: #1a237e; -fx-font-size: 13px; -fx-font-weight: 700; -fx-min-width: 140;");

            Label customer = new Label(asText(row.get("customer")));
            customer.setStyle("-fx-text-fill: #37474f; -fx-font-size: 13px; -fx-min-width: 180;");

            Label account = new Label(asText(row.get("account")));
            account.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 12px;");

            Label status = new Label(asText(row.get("status")));
            status.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 11px; -fx-font-weight: 600; -fx-min-width: 90;");

            item.getChildren().addAll(ref, type, amount, customer, account, status);
            activityList.getChildren().add(item);
        }
    }

    private void renderBranches(DashboardSnapshot d) {
        if (branchList == null) return;
        branchList.getChildren().clear();

        if (d.getBranchPerformance() == null || d.getBranchPerformance().isEmpty()) {
            Label empty = new Label("No branch data available.");
            empty.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 12px; -fx-padding: 20;");
            branchList.getChildren().add(empty);
            return;
        }

        for (Map<String, Object> row : d.getBranchPerformance()) {
            HBox item = new HBox(16);
            item.setStyle("-fx-padding: 10 0 10 0; -fx-border-color: transparent transparent #eef1f5 transparent; " +
                    "-fx-border-width: 0 0 1 0; -fx-alignment: center-left;");

            Label branch = new Label(asText(row.get("branch")));
            branch.setStyle("-fx-text-fill: #263238; -fx-font-size: 13px; -fx-font-weight: 600; -fx-min-width: 180;");

            Label city = new Label(asText(row.get("city")));
            city.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 12px; -fx-min-width: 120;");

            Label accounts = new Label(asText(row.get("accounts")) + " accounts");
            accounts.setStyle("-fx-text-fill: #37474f; -fx-font-size: 12px; -fx-min-width: 110;");

            Label deposits = new Label(formatAmount(row.get("deposits")));
            deposits.setStyle("-fx-text-fill: #1a237e; -fx-font-size: 13px; -fx-font-weight: 700;");

            item.getChildren().addAll(branch, city, accounts, deposits);
            branchList.getChildren().add(item);
        }
    }

    @FXML private void goNewCustomer() { BankingApplication.navigateTo("customers"); }
    @FXML private void goNewAccount() { BankingApplication.navigateTo("accounts"); }
    @FXML private void goTransactions() { BankingApplication.navigateTo("transactions"); }
    @FXML private void goLoans() { BankingApplication.navigateTo("loans"); }

    private static String asText(Object o) { return o == null ? "" : o.toString(); }

    private static String formatAmount(Object o) {
        if (o == null) return "0.00";
        try {
            return String.format("%,.2f", new java.math.BigDecimal(o.toString()));
        } catch (Exception e) {
            return o.toString();
        }
    }
}