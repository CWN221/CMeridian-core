package com.bank.system.controller;

import com.bank.system.dto.TransactionDTO;
import com.bank.system.dto.TransferRequestDTO;
import com.bank.system.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for processing and viewing transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @PreAuthorize("hasAuthority('TRANSACTION_DEPOSIT')")
    @Operation(summary = "Process a deposit")
    public ResponseEntity<TransactionDTO> deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        TransactionDTO transaction = transactionService.processDeposit(accountNumber, amount, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/withdrawal")
    @PreAuthorize("hasAuthority('TRANSACTION_WITHDRAW')")
    @Operation(summary = "Process a withdrawal")
    public ResponseEntity<TransactionDTO> withdrawal(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        TransactionDTO transaction = transactionService.processWithdrawal(accountNumber, amount, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('TRANSACTION_TRANSFER')")
    @Operation(summary = "Process a transfer between accounts")
    public ResponseEntity<TransactionDTO> transfer(@Valid @RequestBody TransferRequestDTO transferRequest) {
        TransactionDTO transaction = transactionService.processTransfer(transferRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSACTION_VIEW')")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id) {
        TransactionDTO transaction = transactionService.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasAuthority('TRANSACTION_VIEW')")
    @Operation(summary = "Get transaction by reference number")
    public ResponseEntity<TransactionDTO> getTransactionByReference(@PathVariable String reference) {
        TransactionDTO transaction = transactionService.getTransactionByReference(reference);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAuthority('TRANSACTION_VIEW')")
    @Operation(summary = "Get all transactions for an account")
    public ResponseEntity<Page<TransactionDTO>> getAccountTransactions(
            @PathVariable Long accountId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TransactionDTO> transactions = transactionService.getAccountTransactions(accountId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAuthority('TRANSACTION_REVERSE')")
    @Operation(summary = "Reverse a transaction")
    public ResponseEntity<TransactionDTO> reverseTransaction(
            @PathVariable Long id,
            @RequestParam String reason) {
        TransactionDTO transaction = transactionService.reverseTransaction(id, reason);
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('TRANSACTION_VIEW')")
    public ResponseEntity<Page<TransactionDTO>> listTransactions(
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.listTransactions(pageable));
    }
}