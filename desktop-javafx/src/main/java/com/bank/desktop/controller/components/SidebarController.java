package com.bank.desktop.controller.components;

import com.bank.desktop.BankingApplication;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class SidebarController {

    @FXML private HBox navDashboard;
    @FXML private HBox navCustomers;
    @FXML private HBox navAccounts;
    @FXML private HBox navTransactions;
    @FXML private HBox navLoans;
    @FXML private HBox navCards;
    @FXML private HBox navReports;
    @FXML private HBox navAudit;
    @FXML private HBox navSettings;
    @FXML private Label userName;
    @FXML private Label userRole;

    public void setActive(String key) {
        clearAll();
        HBox target = switch (key) {
            case "dashboard" -> navDashboard;
            case "customers" -> navCustomers;
            case "accounts" -> navAccounts;
            case "transactions" -> navTransactions;
            case "loans" -> navLoans;
            case "cards" -> navCards;
            case "reports" -> navReports;
            case "audit" -> navAudit;
            case "settings" -> navSettings;
            default -> null;
        };
        if (target != null && !target.getStyleClass().contains("nav-item-active")) {
            target.getStyleClass().add("nav-item-active");
        }
    }

    private void clearAll() {
        for (HBox b : new HBox[]{navDashboard, navCustomers, navAccounts, navTransactions,
                                 navLoans, navCards, navReports, navAudit, navSettings}) {
            b.getStyleClass().remove("nav-item-active");
        }
    }

    public void setUser(String name, String role) {
        userName.setText(name);
        userRole.setText(role);
    }

    @FXML private void goDashboard(MouseEvent e) { BankingApplication.navigateTo("dashboard"); }
    @FXML private void goCustomers(MouseEvent e) { BankingApplication.navigateTo("customers"); }
    @FXML private void goAccounts(MouseEvent e) { BankingApplication.navigateTo("accounts"); }
    @FXML private void goTransactions(MouseEvent e) { BankingApplication.navigateTo("transactions"); }
    @FXML private void goLoans(MouseEvent e) { BankingApplication.navigateTo("loans"); }
    @FXML private void goCards(MouseEvent e) { BankingApplication.navigateTo("cards"); }
    @FXML private void goReports(MouseEvent e) { BankingApplication.navigateTo("reports"); }
    @FXML private void goAudit(MouseEvent e) { BankingApplication.navigateTo("audit"); }
    @FXML private void goSettings(MouseEvent e) { BankingApplication.navigateTo("settings"); }

    @FXML private void signOut(MouseEvent e) {
        BankingApplication.switchToLogin();
    }
}