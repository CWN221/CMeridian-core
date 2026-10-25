package com.bank.system.controller;

import com.bank.system.dto.AccountDTO;
import com.bank.system.dto.AccountOpeningRequestDTO;
import com.bank.system.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "List accounts with pagination")
    public ResponseEntity<Page<AccountDTO>> listAccounts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(accountService.listAccounts(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @GetMapping("/by-number/{accountNumber}")
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public ResponseEntity<AccountDTO> getAccountByNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('ACCOUNT_VIEW')")
    public ResponseEntity<List<AccountDTO>> getCustomerAccounts(@PathVariable Long customerId) {
        return ResponseEntity.ok(accountService.getCustomerAccounts(customerId));
    }

    @PostMapping("/open")
    @PreAuthorize("hasAuthority('ACCOUNT_CREATE')")
    @Operation(summary = "Open a new account")
    public ResponseEntity<AccountDTO> openAccount(@Valid @RequestBody AccountOpeningRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.openAccount(dto));
    }

    @PostMapping("/{id}/freeze")
    @PreAuthorize("hasAuthority('ACCOUNT_FREEZE')")
    public ResponseEntity<AccountDTO> freezeAccount(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Frozen by staff") String reason) {
        return ResponseEntity.ok(accountService.freezeAccount(id, reason));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ACCOUNT_UPDATE')")
    public ResponseEntity<AccountDTO> activateAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.activateAccount(id));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('ACCOUNT_CLOSE')")
    public ResponseEntity<AccountDTO> closeAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.closeAccount(id));
    }
}