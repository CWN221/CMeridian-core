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
public class Transaction {

    private Long id;
    private String transactionReference;
    private Long accountId;
    private String accountNumber;
    private String transactionType;
    private BigDecimal amount;
    private String transactionDate;
    private String description;
    private String status;
    private String currencyCode;

    public String getTypeLabel() {
        if (transactionType == null) return "Unknown";
        return switch (transactionType) {
            case "DEPOSIT" -> "Deposit";
            case "WITHDRAWAL" -> "Withdrawal";
            case "TRANSFER" -> "Transfer";
            case "FEE" -> "Fee";
            case "INTEREST" -> "Interest";
            case "LOAN_DISBURSEMENT" -> "Loan Disbursement";
            case "LOAN_REPAYMENT" -> "Loan Repayment";
            default -> transactionType;
        };
    }

    public String getStatusLabel() {
        if (status == null) return "Unknown";
        return switch (status) {
            case "PENDING" -> "Pending";
            case "COMPLETED" -> "Completed";
            case "FAILED" -> "Failed";
            case "REVERSED" -> "Reversed";
            default -> status;
        };
    }

    public String getFormattedAmount() {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}