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
public class Card {

    private Long id;
    private String cardNumber;
    private Long customerId;
    private String customerName;
    private Long accountId;
    private String accountNumber;
    private String cardType;
    private String cardBrand;
    private String cardholderName;
    private String expiryDate;
    private String issueDate;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private String status;
    private String blockedReason;

    public String getMaskedNumber() {
        if (cardNumber == null || cardNumber.length() < 8) return cardNumber;
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    public String getStatusLabel() {
        if (status == null) return "Unknown";
        return switch (status) {
            case "ACTIVE" -> "Active";
            case "BLOCKED" -> "Blocked";
            case "EXPIRED" -> "Expired";
            case "CANCELLED" -> "Cancelled";
            default -> status;
        };
    }
}