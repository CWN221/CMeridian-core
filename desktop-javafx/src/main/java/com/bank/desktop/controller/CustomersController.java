package com.bank.desktop.controller;

import com.bank.desktop.model.Customer;
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
public class CustomersController implements Initializable {

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colCustomerNumber;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colNationalId;
    @FXML private TableColumn<Customer, String> colStatus;
    @FXML private TableColumn<Customer, String> colActions;
    @FXML private TextField searchField;
    @FXML private Label statsTotal;
    @FXML private Label statsVerified;
    @FXML private Label statsPending;
    @FXML private Label statsActive;

    private final CustomerService customerService = new CustomerService();
    private final ObservableList<Customer> masterData = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        configureActionsColumn();

        filteredData = new FilteredList<>(masterData, p -> true);
        customerTable.setItems(filteredData);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applySearch());
        }

        loadFromBackend();
    }

    private void configureColumns() {
        colCustomerNumber.setCellValueFactory(c ->
                new SimpleStringProperty(nz(c.getValue().getCustomerNumber())));
        colName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFullName()));
        colEmail.setCellValueFactory(c ->
                new SimpleStringProperty(nz(c.getValue().getEmail())));
        colPhone.setCellValueFactory(c ->
                new SimpleStringProperty(nz(c.getValue().getPhone())));
        colNationalId.setCellValueFactory(c ->
                new SimpleStringProperty(nz(c.getValue().getNationalId())));
        colStatus.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatusLabel()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(status);
                badge.getStyleClass().add("badge");
                switch (status) {
                    case "Verified" -> badge.getStyleClass().add("badge-active");
                    case "Pending" -> badge.getStyleClass().add("badge-pending");
                    case "Deactivated" -> badge.getStyleClass().add("badge-inactive");
                    default -> badge.getStyleClass().add("badge-info");
                }
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void configureActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, viewBtn, editBtn, deleteBtn);

            {
                viewBtn.getStyleClass().addAll("btn-ghost", "btn-sm");
                editBtn.getStyleClass().addAll("btn-ghost", "btn-sm");
                deleteBtn.getStyleClass().addAll("btn-ghost", "btn-sm");
                viewBtn.setOnAction(e -> showDetails(getRow()));
                editBtn.setOnAction(e -> openEdit(getRow()));
                deleteBtn.setOnAction(e -> confirmDelete(getRow()));
            }

            private Customer getRow() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setText(null);
            }
        });
    }

    private void loadFromBackend() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return customerService.list(0, 200);
            } catch (Exception e) {
                log.error("Failed to load customers", e);
                return null;
            }
        }).thenAccept(list -> Platform.runLater(() -> {
            if (list == null) {
                NotificationUtil.showError("Load Failed", "Could not retrieve customers from server.");
                return;
            }
            masterData.setAll(list);
            refreshStats();
        }));
    }

    private void refreshStats() {
        if (statsTotal == null) return;
        long total = masterData.size();
        long verified = masterData.stream().filter(c -> Boolean.TRUE.equals(c.getIsVerified())).count();
        long pending = masterData.stream().filter(c -> !Boolean.TRUE.equals(c.getIsVerified())
                && !Boolean.FALSE.equals(c.getIsActive())).count();
        long active = masterData.stream().filter(c -> !Boolean.FALSE.equals(c.getIsActive())).count();

        statsTotal.setText(Long.toString(total));
        statsVerified.setText(Long.toString(verified));
        statsPending.setText(Long.toString(pending));
        statsActive.setText(Long.toString(active));
    }

    private void applySearch() {
        String term = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filteredData.setPredicate(c -> term.isEmpty()
                || contains(c.getCustomerNumber(), term)
                || contains(c.getFullName(), term)
                || contains(c.getEmail(), term)
                || contains(c.getPhone(), term)
                || contains(c.getNationalId(), term));
    }

    private void showDetails(Customer c) {
        if (c == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Customer Details");
        alert.setHeaderText(c.getFullName() + "  (" + nz(c.getCustomerNumber()) + ")");
        alert.setContentText(
                "Email:         " + nz(c.getEmail()) + "\n"
              + "Phone:         " + nz(c.getPhone()) + "\n"
              + "National ID:   " + nz(c.getNationalId()) + "\n"
              + "Date of Birth: " + (c.getDateOfBirth() != null ? c.getDateOfBirth() : "not set") + "\n"
              + "Address:       " + nz(c.getAddressLine1()) + "\n"
              + "City:          " + nz(c.getCity()) + "\n"
              + "Country:       " + nz(c.getCountry()) + "\n"
              + "Occupation:    " + nz(c.getOccupation()) + "\n"
              + "Status:        " + c.getStatusLabel()
        );
        alert.showAndWait();
    }

    private void openEdit(Customer c) {
        if (c == null) return;
        CustomerFormDialog.showEdit(c).ifPresent(updated -> {
            CompletableFuture.supplyAsync(() -> {
                try {
                    return customerService.update(c.getId(), updated);
                } catch (Exception e) {
                    log.error("Update failed", e);
                    return e;
                }
            }).thenAccept(result -> Platform.runLater(() -> {
                if (result instanceof Exception ex) {
                    NotificationUtil.showError("Update Failed", ex.getMessage());
                    return;
                }
                NotificationUtil.showSuccess("Customer Updated", "Changes saved successfully.");
                loadFromBackend();
            }));
        });
    }

    private void confirmDelete(Customer c) {
        if (c == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Customer");
        confirm.setHeaderText("Deactivate " + c.getFullName() + "?");
        confirm.setContentText("The customer will be marked inactive. Historical data and accounts are preserved.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        CompletableFuture.supplyAsync(() -> {
            try {
                customerService.delete(c.getId());
                return Boolean.TRUE;
            } catch (Exception e) {
                log.error("Delete failed", e);
                return e;
            }
        }).thenAccept(res -> Platform.runLater(() -> {
            if (res instanceof Exception ex) {
                NotificationUtil.showError("Delete Failed", ex.getMessage());
                return;
            }
            NotificationUtil.showSuccess("Customer Deactivated", c.getFullName() + " has been deactivated.");
            loadFromBackend();
        }));
    }

    @FXML
    private void onNewCustomer() {
        CustomerFormDialog.showCreate().ifPresent(customer -> {
            CompletableFuture.supplyAsync(() -> {
                try {
                    return customerService.create(customer);
                } catch (Exception e) {
                    log.error("Create failed", e);
                    return e;
                }
            }).thenAccept(result -> Platform.runLater(() -> {
                if (result instanceof Exception ex) {
                    NotificationUtil.showError("Create Failed", ex.getMessage());
                    return;
                }
                NotificationUtil.showSuccess("Customer Created", "New customer registered successfully.");
                loadFromBackend();
            }));
        });
    }

    @FXML
    private void onRefresh() {
        loadFromBackend();
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }
}