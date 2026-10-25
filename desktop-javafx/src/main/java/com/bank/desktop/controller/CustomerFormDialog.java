package com.bank.desktop.controller;

import com.bank.desktop.model.Customer;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Optional;

public class CustomerFormDialog {

    public static Optional<Customer> showCreate() {
        return show(null);
    }

    public static Optional<Customer> showEdit(Customer existing) {
        return show(existing);
    }

    private static Optional<Customer> show(Customer existing) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "New Customer" : "Edit Customer");
        dialog.setHeaderText(existing == null
                ? "Register a new customer"
                : "Update details for " + existing.getFullName());

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField firstName = new TextField();
        TextField lastName = new TextField();
        TextField middleName = new TextField();
        DatePicker dob = new DatePicker();
        TextField nationalId = new TextField();
        TextField email = new TextField();
        TextField phone = new TextField();
        TextField altPhone = new TextField();
        TextField addressLine = new TextField();
        TextField city = new TextField();
        TextField state = new TextField();
        TextField postalCode = new TextField();
        TextField country = new TextField();
        TextField occupation = new TextField();
        TextField employer = new TextField();

        if (existing != null) {
            firstName.setText(nz(existing.getFirstName()));
            lastName.setText(nz(existing.getLastName()));
            middleName.setText(nz(existing.getMiddleName()));
            if (existing.getDateOfBirth() != null) dob.setValue(existing.getDateOfBirth());
            nationalId.setText(nz(existing.getNationalId()));
            email.setText(nz(existing.getEmail()));
            phone.setText(nz(existing.getPhone()));
            altPhone.setText(nz(existing.getAlternatePhone()));
            addressLine.setText(nz(existing.getAddressLine1()));
            city.setText(nz(existing.getCity()));
            state.setText(nz(existing.getState()));
            postalCode.setText(nz(existing.getPostalCode()));
            country.setText(nz(existing.getCountry()));
            occupation.setText(nz(existing.getOccupation()));
            employer.setText(nz(existing.getEmployer()));
        }

        int row = 0;
        addRow(grid, row++, "First Name *", firstName);
        addRow(grid, row++, "Last Name *", lastName);
        addRow(grid, row++, "Middle Name", middleName);
        addRow(grid, row++, "Date of Birth *", dob);
        addRow(grid, row++, "National ID *", nationalId);
        addRow(grid, row++, "Email *", email);
        addRow(grid, row++, "Phone *", phone);
        addRow(grid, row++, "Alternate Phone", altPhone);
        addRow(grid, row++, "Address *", addressLine);
        addRow(grid, row++, "City *", city);
        addRow(grid, row++, "State", state);
        addRow(grid, row++, "Postal Code", postalCode);
        addRow(grid, row++, "Country *", country);
        addRow(grid, row++, "Occupation", occupation);
        addRow(grid, row++, "Employer", employer);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(500);
        dialog.getDialogPane().setContent(scroll);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            String error = validate(firstName, lastName, dob, nationalId, email, phone,
                    addressLine, city, country);
            if (error != null) {
                evt.consume();
                new Alert(Alert.AlertType.WARNING, error).showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != saveType) return null;

            Customer c = existing != null ? existing : new Customer();
            c.setFirstName(firstName.getText().trim());
            c.setLastName(lastName.getText().trim());
            c.setMiddleName(blankToNull(middleName.getText()));
            c.setDateOfBirth(dob.getValue());
            c.setNationalId(nationalId.getText().trim());
            c.setEmail(email.getText().trim());
            c.setPhone(phone.getText().trim());
            c.setAlternatePhone(blankToNull(altPhone.getText()));
            c.setAddressLine1(addressLine.getText().trim());
            c.setCity(city.getText().trim());
            c.setState(blankToNull(state.getText()));
            c.setPostalCode(blankToNull(postalCode.getText()));
            c.setCountry(country.getText().trim());
            c.setOccupation(blankToNull(occupation.getText()));
            c.setEmployer(blankToNull(employer.getText()));
            return c;
        });

        return dialog.showAndWait();
    }

    private static void addRow(GridPane grid, int row, String label, javafx.scene.Node field) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #3f3f46;");
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private static String validate(TextField firstName, TextField lastName, DatePicker dob,
                                   TextField nationalId, TextField email, TextField phone,
                                   TextField addressLine, TextField city, TextField country) {
        if (isBlank(firstName.getText())) return "First name is required.";
        if (isBlank(lastName.getText())) return "Last name is required.";
        if (dob.getValue() == null) return "Date of birth is required.";
        if (isBlank(nationalId.getText())) return "National ID is required.";
        if (isBlank(email.getText()) || !email.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            return "A valid email address is required.";
        if (isBlank(phone.getText())) return "Phone number is required.";
        if (isBlank(addressLine.getText())) return "Address is required.";
        if (isBlank(city.getText())) return "City is required.";
        if (isBlank(country.getText())) return "Country is required.";
        return null;
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String nz(String s) { return s == null ? "" : s; }
    private static String blankToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}