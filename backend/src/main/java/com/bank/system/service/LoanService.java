package com.bank.system.service;

import com.bank.system.dto.LoanApplicationDTO;
import com.bank.system.dto.LoanDTO;
import com.bank.system.dto.LoanRepaymentDTO;
import com.bank.system.entity.Account;
import com.bank.system.entity.Customer;
import com.bank.system.entity.Loan;
import com.bank.system.exception.BusinessException;
import com.bank.system.exception.ResourceNotFoundException;
//import com.bank.system.mapper.CustomerMapper;
import com.bank.system.mapper.LoanMapper;
import com.bank.system.repository.AccountRepository;
import com.bank.system.repository.CustomerRepository;
import com.bank.system.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoanService {
    
    private final LoanMapper loanMapper;
    //private CustomerMapper customerMapper;
    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    
    @Transactional(readOnly = true)
    public Page<LoanDTO> list(Pageable pageable) {
        return loanRepository.findAll(pageable).map(loanMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public LoanDTO get(Long id) {
        return loanMapper.toDTO(loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> byCustomer(Long customerId) {
        return loanRepository.findByCustomerId(customerId).stream()
                .map(loanMapper::toDTO)
                .toList();
    }

    @Transactional
    public LoanDTO create(LoanApplicationDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Loan loan = Loan.builder()
                .loanNumber("LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(customer)
                .account(account)
                .loanType(dto.getLoanType())
                .principalAmount(dto.getPrincipalAmount())
                .interestRate(dto.getInterestRate())
                .loanTermMonths(dto.getLoanTermMonths())
                .status(Loan.LoanStatus.PENDING)
                .amountPaid(BigDecimal.ZERO)
                .outstandingBalance(dto.getPrincipalAmount())
                .createdBy(currentUsername())
                .build();

        return loanMapper.toDTO(loanRepository.save(loan));
    }

    @Transactional
    public LoanDTO approve(Long id, BigDecimal approvedAmount) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new BusinessException("Only pending loans can be approved");
        }
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDateTime.now());
        loan.setApprovedAmount(approvedAmount != null ? approvedAmount : loan.getPrincipalAmount());
        loan.setUpdatedBy(currentUsername());
        return loanMapper.toDTO(loanRepository.save(loan));
    }

    @Transactional
    public LoanDTO reject(Long id, String reason) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new BusinessException("Only pending loans can be rejected");
        }
        loan.setStatus(Loan.LoanStatus.CLOSED);
        loan.setUpdatedBy(currentUsername());
        return loanMapper.toDTO(loanRepository.save(loan));
    }

    @Transactional
    public LoanDTO disburse(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
        if (loan.getStatus() != Loan.LoanStatus.APPROVED) {
            throw new BusinessException("Only approved loans can be disbursed");
        }
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        loan.setDisbursementDate(LocalDate.now());
        loan.setMaturityDate(LocalDate.now().plusMonths(loan.getLoanTermMonths()));
        loan.setUpdatedBy(currentUsername());
        return loanMapper.toDTO(loanRepository.save(loan));
    }

    @Transactional
    public LoanDTO repay(Long id, LoanRepaymentDTO dto) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new BusinessException("Only active loans can receive repayment");
        }
        BigDecimal paid = loan.getAmountPaid().add(dto.getAmount());
        BigDecimal outstanding = loan.getPrincipalAmount().subtract(paid).max(BigDecimal.ZERO);
        loan.setAmountPaid(paid);
        loan.setOutstandingBalance(outstanding);
        if (outstanding.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(Loan.LoanStatus.CLOSED);
        }
        loan.setUpdatedBy(currentUsername());
        return loanMapper.toDTO(loanRepository.save(loan));
    }

    @Transactional
    public LoanDTO close(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
        loan.setStatus(Loan.LoanStatus.CLOSED);
        loan.setUpdatedBy(currentUsername());
        return loanMapper.toDTO(loanRepository.save(loan));
    }
    
    @Transactional(readOnly = true)
    public LoanDTO getByLoanNumber(String loanNumber) {
        Loan loan = loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanNumber));
        return loanMapper.toDTO(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> byStatus(String status) {
        Loan.LoanStatus statusEnum;
        try {
            statusEnum = Loan.LoanStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid loan status: " + status);
        }
        return loanRepository.findByStatus(statusEnum).stream().map(loanMapper::toDTO).toList();
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}
