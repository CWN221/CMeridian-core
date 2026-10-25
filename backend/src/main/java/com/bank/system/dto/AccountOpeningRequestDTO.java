package com.bank.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountOpeningRequestDTO {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Account type code is required")
    @Size(max = 20)
    private String accountTypeCode;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3)
    private String currencyCode;

    @Size(max = 100)
    private String accountName;

    private BigDecimal initialDeposit;

    private Boolean isJointAccount;

    private BigDecimal dailyTransactionLimit;
}