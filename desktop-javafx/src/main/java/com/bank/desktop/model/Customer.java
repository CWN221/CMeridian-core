package com.bank.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private Long id;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDate dateOfBirth;
    private String nationalId;
    private String passportNumber;
    private String email;
    private String phone;
    private String alternatePhone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String occupation;
    private String employer;
    private BigDecimal annualIncome;
    private Boolean isVerified;
    private Boolean isActive;
    private String verificationRemarks;
    private String createdAt;
    private String updatedAt;

    public String getFullName() {
        return firstName
                + (middleName != null && !middleName.isBlank() ? " " + middleName : "")
                + " " + lastName;
    }

    public String getStatusLabel() {
        if (Boolean.FALSE.equals(isActive)) return "Deactivated";
        if (Boolean.TRUE.equals(isVerified)) return "Verified";
        return "Pending";
    }
}