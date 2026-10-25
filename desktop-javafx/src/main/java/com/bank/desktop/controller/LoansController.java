package com.bank.desktop.controller;

import com.bank.desktop.model.Account;
import com.bank.desktop.model.Customer;
import com.bank.desktop.model.Loan;
import com.bank.desktop.service.AccountService;
import com.bank.desktop.service.CustomerService;
import com.bank.desktop.service.LoanService;
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

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class LoansController implements Initializable {

    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, String> colLoanNumber;
    @FXML private TableColumn<Loan, String> colCustomer;
    @FXML private TableColumn<Loan, String> colType;
    @FXML private TableColumn<Loan, String> colPrincipal;
    @FXML private TableColumn<Loan, String> colOutstanding;
    @FXML private TableColumn<Loan, String> colStatus;
    @FXML private TableColumn<Loan, String> colActions;
    @FXML private TextField searchField;
    @FXML private Label statsTotal;
    @FXML private Label statsPortfolio;
    @FXML private Label statsPending;
    @FXML private Label statsDefaulted;

    private final LoanService loanService = new LoanService();
    private final CustomerService customerService = new CustomerService();
    private final AccountService accountService = new AccountService();
    private final ObservableList<Loan> master = FXCollections.observableArrayList();
    private FilteredList<Loan> filtered;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        configureActions();
        filtered = new FilteredList<>(master, p -> true);
        loanTable.setItems(filtered);
        if (searchField != null) searchField.textProperty().addListener((obs, o, n) -> applySearch());
        loadFromBackend();
    }

    private void configureColumns() {
        colLoanNumber.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getLoanNumber())));
        colCustomer.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getCustomerName())));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeLabel()));
        colPrincipal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedPrincipal()));
        colOutstanding.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedOutstanding()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusLabel()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(s);
                b.getStyleClass().add("badge");
                switch (s) {
                    case "Active", "Disbursed" -> b.getStyleClass().add("badge-active");
                    case "Pending", "Approved" -> b.getStyleClass().add("badge-pending");
                    case "Defaulted" -> b.getStyleClass().add("badge-inactive");
                    default -> b.getStyleClass().add("badge-info");
                }
                setGraphic(b); setText(null);
            }
        });
    }

    private void configureActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button approve = new Button("Approve");
            private final Button disburse = new Button("Disburse");
            private final Button repay = new Button("Repay");
            private final HBox box = new HBox(6, approve, disburse, repay);
            {
                approve.getStyleClass().addAll("btn-ghost", "btn-sm");
                disburse.getStyleClass().addAll("btn-ghost", "btn-sm");
                repay.getStyleClass().addAll("btn-ghost", "btn-sm");
                approve.setOnAction(e -> onApprove(get()));
                disburse.setOnAction(e -> onDisburse(get()));
                repay.setOnAction(e -> onRepay(get()));
            }
            private Loan get() { return getTableView().getItems().get(getIndex()); }
            @Override protected void updateItem(String i, boolean empty) {
                super.updateItem(i, empty);
                setGraphic(empty ? null : box);
                setText(null);
            }
        });
    }

    private void loadFromBackend() {
        CompletableFuture.supplyAsync(() -> {
            try { return loanService.list(0, 500); }
            catch (Exception e) { log.error("Load loans failed", e); return null; }
        }).thenAccept(list -> Platform.runLater(() -> {
            if (list == null) {
                NotificationUtil.showError("Load Failed", "Could not retrieve loans.");
                return;
            }
            master.setAll(list);
            refreshStats();
        }));
    }

    private void refreshStats() {
        if (statsTotal == null) return;
        long total = master.size();
        long pending = master.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        long defaulted = master.stream().filter(l -> "DEFAULTED".equals(l.getStatus())).count();
        BigDecimal sum = master.stream()
                .map(l -> l.getPrincipalAmount() == null ? BigDecimal.ZERO : l.getPrincipalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statsTotal.setText(Long.toString(total));
        statsPortfolio.setText(String.format("%,.0f", sum));
        statsPending.setText(Long.toString(pending));
        statsDefaulted.setText(Long.toString(defaulted));
    }

    private void applySearch() {
        String term = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(l -> term.isEmpty()
                || contains(l.getLoanNumber(), term)
                || contains(l.getCustomerName(), term)
                || contains(l.getTypeLabel(), term));
    }

    private void onApprove(Loan l) {
        if (l == null || !"PENDING".equals(l.getStatus())) {
            NotificationUtil.showInfo("Not Applicable", "Only pending loans can be approved.");
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            try { return loanService.approve(l.getId()); }
            catch (Exception e) { return e; }
        }).thenAccept(res -> Platform.runLater(() -> {
            if (res instanceof Exception ex) {
                NotificationUtil.showError("Approve Failed", ex.getMessage());
                return;
            }
            NotificationUtil.showSuccess("Loan Approved", l.getLoanNumber() + " approved.");
            loadFromBackend();
        }));
    }

    private void onDisburse(Loan l) {
        if (l == null || !"APPROVED".equals(l.getStatus())) {
            NotificationUtil.showInfo("Not Applicable", "Only approved loans can be disbursed.");
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            try { return loanService.disburse(l.getId()); }
            catch (Exception e) { return e; }
        }).thenAccept(res -> Platform.runLater(() -> {
            if (res instanceof Exception ex) {
                NotificationUtil.showError("Disburse Failed", ex.getMessage());
                return;
            }
            NotificationUtil.showSuccess("Loan Disbursed", l.getLoanNumber() + " is now active.");
            loadFromBackend();
        }));
    }

    private void onRepay(Loan l) {
        if (l == null || !"ACTIVE".equals(l.getStatus())) {
            NotificationUtil.showInfo("Not Applicable", "Only active loans can receive repayment.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("5000");
        dialog.setTitle("Loan Repayment");
        dialog.setHeaderText("Repay " + l.getLoanNumber() + " (outstanding " + l.getFormattedOutstanding() + ")");
        dialog.setContentText("Amount:");
        dialog.showAndWait().ifPresent(input -> {
            try {
                BigDecimal amt = new BigDecimal(input.trim());
                if (amt.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
                CompletableFuture.supplyAsync(() -> {
                    try { return loanService.repay(l.getId(), amt, "Repayment via operations console"); }
                    catch (Exception e) { return e; }
                }).thenAccept(res -> Platform.runLater(() -> {
                    if (res instanceof Exception ex) {
                        NotificationUtil.showError("Repay Failed", ex.getMessage());
                        return;
                    }
                    NotificationUtil.showSuccess("Repayment Posted", "Loan balance updated.");
                    loadFromBackend();
                }));
            } catch (NumberFormatException e) {
                NotificationUtil.showError("Invalid Amount", "Enter a positive number.");
            }
        });
    }

    @FXML
    private void onNewLoan() {
        CompletableFuture.supplyAsync(() -> {
            try {
                List<Customer> customers = customerService.list(0, 500);
                List<Account> accounts = accountService.list(0, 500);
                return new Object[]{customers, accounts};
            } catch (Exception e) {
                log.error("Load pre-requisites failed", e);
                return null;
            }
        }).thenAccept(data -> Platform.runLater(() -> {
            if (data == null) {
                NotificationUtil.showError("Cannot Open Form", "Failed to load customers or accounts.");
                return;
            }
            @SuppressWarnings("unchecked")
            List<Customer> customers = (List<Customer>) data[0];
            @SuppressWarnings("unchecked")
            List<Account> accounts = (List<Account>) data[1];
            if (customers.isEmpty() || accounts.isEmpty()) {
                NotificationUtil.showError("Cannot Open Form", "Register a customer and an account first.");
                return;
            }
            LoanFormDialog.showCreate(customers, accounts).ifPresent(form ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return loanService.create(form.customerId, form.accountId,
                                form.loanType, form.principal, form.rate, form.term);
                    } catch (Exception e) { return e; }
                }).thenAccept(res -> Platform.runLater(() -> {
                    if (res instanceof Exception ex) {
                        NotificationUtil.showError("Create Failed", ex.getMessage());
                        return;
                    }
                    NotificationUtil.showSuccess("Loan Application Submitted", "Pending review.");
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