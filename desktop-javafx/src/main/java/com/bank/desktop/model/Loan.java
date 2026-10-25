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
public class Loan {

    private Long id;
    private String loanNumber;
    private Long customerId;
    private String customerName;
    private Long accountId;
    private String accountNumber;
    private String loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer loanTermMonths;
    private String disbursementDate;
    private String maturityDate;
    private BigDecimal monthlyInstallment;
    private BigDecimal outstandingBalance;
    private BigDecimal amountPaid;
    private String status;

    public String getTypeLabel() {
        if (loanType == null) return "";
        return switch (loanType) {
            case "PERSONAL" -> "Personal";
            case "MORTGAGE" -> "Mortgage";
            case "AUTO" -> "Auto";
            case "BUSINESS" -> "Business";
            case "EDUCATION" -> "Education";
            default -> loanType;
        };
    }

    public String getStatusLabel() {
        if (status == null) return "Unknown";
        return switch (status) {
            case "PENDING" -> "Pending";
            case "APPROVED" -> "Approved";
            case "DISBURSED" -> "Disbursed";
            case "ACTIVE" -> "Active";
            case "CLOSED" -> "Closed";
            case "DEFAULTED" -> "Defaulted";
            default -> status;
        };
    }

    public String getFormattedPrincipal() {
        return principalAmount == null ? "0.00" : String.format("%,.2f", principalAmount);
    }

    public String getFormattedOutstanding() {
        return outstandingBalance == null ? "0.00" : String.format("%,.2f", outstandingBalance);
    }
}