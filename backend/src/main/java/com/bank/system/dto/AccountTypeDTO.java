package com.bank.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTypeDTO {
    private Long id;
    private String typeCode;
    private String typeName;
    private String description;
    private BigDecimal minimumBalance;
    private BigDecimal interestRate;
    private Boolean overdraftAllowed;
    private Boolean isActive;
}