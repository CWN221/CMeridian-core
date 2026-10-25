package com.bank.system.controller;

import com.bank.system.dto.AccountTypeDTO;
import com.bank.system.repository.AccountTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/account-types")
@RequiredArgsConstructor
public class AccountTypeController {

    private final AccountTypeRepository accountTypeRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public ResponseEntity<List<AccountTypeDTO>> list() {
        List<AccountTypeDTO> list = accountTypeRepository.findAllActiveOrdered().stream()
                .map(a -> AccountTypeDTO.builder()
                        .id(a.getId())
                        .typeCode(a.getTypeCode())
                        .typeName(a.getTypeName())
                        .description(a.getDescription())
                        .minimumBalance(a.getMinimumBalance())
                        .interestRate(a.getInterestRate())
                        .overdraftAllowed(a.getOverdraftAllowed())
                        .isActive(a.getIsActive())
                        .build())
                .toList();
        return ResponseEntity.ok(list);
    }
}