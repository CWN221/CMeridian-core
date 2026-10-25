package com.bank.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private Long id;
    private String accountNumber;
    private Long customerId;
    private String customerName;
    private String accountTypeCode;
    private String accountTypeName;
    private Long branchId;
    private String branchName;
    private String currencyCode;
    private String accountName;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal interestRate;
    private String status;
    private String openingDate;

    public String getStatusLabel() {
        if (status == null) return "Unknown";
        return switch (status) {
            case "ACTIVE" -> "Active";
            case "FROZEN" -> "Frozen";
            case "DORMANT" -> "Dormant";
            case "CLOSED" -> "Closed";
            default -> status;
        };
    }

    public String getFormattedBalance() {
        if (balance == null) return "0.00";
        return String.format("%,.2f", balance);
    }
}