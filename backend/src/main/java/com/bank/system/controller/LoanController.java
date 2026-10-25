package com.bank.system.controller;

import com.bank.system.dto.LoanApplicationDTO;
import com.bank.system.dto.LoanDTO;
import com.bank.system.dto.LoanRepaymentDTO;
import com.bank.system.service.LoanService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loan Management", description = "Loan application, approval, disbursement and repayment")
@SecurityRequirement(name = "bearerAuth")
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    @PreAuthorize("hasAuthority('LOAN_VIEW')")
    @Operation(summary = "List all loans with pagination")
    public ResponseEntity<Page<LoanDTO>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(loanService.list(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOAN_VIEW')")
    @Operation(summary = "Get a loan by ID")
    public ResponseEntity<LoanDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.get(id));
    }

    @GetMapping("/by-number/{loanNumber}")
    @PreAuthorize("hasAuthority('LOAN_VIEW')")
    @Operation(summary = "Get a loan by loan number")
    public ResponseEntity<LoanDTO> getByNumber(@PathVariable String loanNumber) {
        return ResponseEntity.ok(loanService.getByLoanNumber(loanNumber));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('LOAN_VIEW')")
    @Operation(summary = "List loans belonging to a customer")
    public ResponseEntity<List<LoanDTO>> byCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(loanService.byCustomer(customerId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('LOAN_VIEW')")
    @Operation(summary = "List loans filtered by status")
    public ResponseEntity<List<LoanDTO>> byStatus(@PathVariable String status) {
        return ResponseEntity.ok(loanService.byStatus(status));
    }


    @PostMapping
    @PreAuthorize("hasAuthority('LOAN_CREATE')")
    @Operation(summary = "Submit a new loan application")
    public ResponseEntity<LoanDTO> create(@Valid @RequestBody LoanApplicationDTO dto) {
        LoanDTO created = loanService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('LOAN_APPROVE')")
    @Operation(summary = "Approve a pending loan")
    public ResponseEntity<LoanDTO> approve(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal approvedAmount) {
        return ResponseEntity.ok(loanService.approve(id, approvedAmount));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('LOAN_REJECT')")
    @Operation(summary = "Reject a pending loan")
    public ResponseEntity<LoanDTO> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(loanService.reject(id, reason));
    }

    @PostMapping("/{id}/disburse")
    @PreAuthorize("hasAuthority('LOAN_DISBURSE')")
    @Operation(summary = "Disburse an approved loan")
    public ResponseEntity<LoanDTO> disburse(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.disburse(id));
    }

    @PostMapping("/{id}/repay")
    @PreAuthorize("hasAuthority('LOAN_REPAY')")
    @Operation(summary = "Post a loan repayment")
    public ResponseEntity<LoanDTO> repay(
            @PathVariable Long id,
            @Valid @RequestBody LoanRepaymentDTO dto) {
        return ResponseEntity.ok(loanService.repay(id, dto));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('LOAN_CLOSE')")
    @Operation(summary = "Close a loan")
    public ResponseEntity<LoanDTO> close(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.close(id));
    }
}