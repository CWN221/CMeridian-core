package com.bank.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDTO {
    private Long id;
    private String currencyCode;
    private String currencyName;
    private String symbol;
    private Integer decimalPlaces;
    private Boolean isBaseCurrency;
    private Boolean isActive;
}