package com.bank.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;
    private String transactionReference;
    private Long accountId;
    private String accountNumber;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime transactionDate;
    private String description;
    private String status;
    private Long relatedTransactionId;
    private Long counterpartyAccountId;
    private String counterpartyAccountNumber;
    private Long counterpartyCustomerId;
    private String counterpartyCustomerName;
    private BigDecimal bankCharges;
    private BigDecimal taxAmount;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private String referenceNumber;
    private LocalDateTime createdAt;
    private String createdBy;
}