package com.bank.system.controller;

import com.bank.system.dto.CurrencyDTO;
import com.bank.system.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyRepository currencyRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('CURRENCY_VIEW')")
    public ResponseEntity<List<CurrencyDTO>> list() {
        List<CurrencyDTO> list = currencyRepository.findAllActiveCurrencies().stream()
                .map(c -> CurrencyDTO.builder()
                        .id(c.getId())
                        .currencyCode(c.getCurrencyCode())
                        .currencyName(c.getCurrencyName())
                        .symbol(c.getSymbol())
                        .decimalPlaces(c.getDecimalPlaces())
                        .isBaseCurrency(c.getIsBaseCurrency())
                        .isActive(c.getIsActive())
                        .build())
                .toList();
        return ResponseEntity.ok(list);
    }
}