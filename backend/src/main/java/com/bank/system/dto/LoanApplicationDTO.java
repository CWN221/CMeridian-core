package com.bank.system.dto;

import com.bank.system.entity.Loan;
import com.bank.system.entity.Loan.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationDTO {

    @NotNull
    private Long customerId;

    @NotNull
    private Long accountId;

    @NotNull
    private LoanType loanType;

    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal principalAmount;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal interestRate;

    @NotNull
    @Min(1)
    private Integer loanTermMonths;
}