package com.bank.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

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
    private Boolean isJointAccount;
    private LocalDate openingDate;
    private LocalDate closingDate;
    private LocalDateTime lastTransactionDate;
    private BigDecimal overdraftLimit;
    private BigDecimal dailyTransactionLimit;
    private BigDecimal maintenanceFee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}