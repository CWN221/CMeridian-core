package com.bank.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class TransferRequestDTO {

    @NotBlank(message = "Source account number is required")
    @Size(min = 5, max = 20, message = "Account number must be between 5 and 20 characters")
    private String sourceAccountNumber;

    @NotBlank(message = "Destination account number is required")
    @Size(min = 5, max = 20, message = "Account number must be between 5 and 20 characters")
    private String destinationAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    private String referenceNumber;

    private String sourceCurrency;

    private String destinationCurrency;

    private BigDecimal exchangeRate;
}