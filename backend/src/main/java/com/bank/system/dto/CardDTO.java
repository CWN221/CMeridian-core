package com.bank.system.dto;

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
public class CardDTO {
    private Long id;
    private String cardNumber;
    private Long customerId;
    private String customerName;
    private Long accountId;
    private String accountNumber;
    private String cardType;
    private String cardBrand;
    private String cardholderName;
    private LocalDate expiryDate;
    private LocalDate issueDate;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private BigDecimal singleTransactionLimit;
    private String status;
    private String blockedReason;
}