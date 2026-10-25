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
public class LoanDTO {
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
    private LocalDate disbursementDate;
    private LocalDate maturityDate;
    private BigDecimal monthlyInstallment;
    private BigDecimal totalInterest;
    private BigDecimal totalPayable;
    private BigDecimal amountPaid;
    private BigDecimal outstandingBalance;
    private String status;
    private LocalDate approvalDate;
    private BigDecimal approvedAmount;
}