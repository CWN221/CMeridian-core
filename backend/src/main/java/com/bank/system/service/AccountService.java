package com.bank.system.service;

import com.bank.system.dto.AccountDTO;
import com.bank.system.dto.AccountOpeningRequestDTO;
import com.bank.system.entity.Account;
import com.bank.system.entity.Account.AccountStatus;
import com.bank.system.entity.AccountType;
import com.bank.system.entity.Branch;
import com.bank.system.entity.Currency;
import com.bank.system.entity.Customer;
import com.bank.system.exception.BusinessException;
import com.bank.system.exception.ResourceNotFoundException;
import com.bank.system.mapper.AccountMapper;
import com.bank.system.repository.AccountRepository;
import com.bank.system.repository.AccountTypeRepository;
import com.bank.system.repository.BranchRepository;
import com.bank.system.repository.CurrencyRepository;
import com.bank.system.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final BranchRepository branchRepository;
    private final CurrencyRepository currencyRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountDTO openAccount(AccountOpeningRequestDTO requestDTO) {
        log.info("Opening new account for customer: {}", requestDTO.getCustomerId());
        
        Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + requestDTO.getCustomerId()));
        
        AccountType accountType = accountTypeRepository.findByTypeCode(requestDTO.getAccountTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Account type not found: " + requestDTO.getAccountTypeCode()));
        
        Branch branch = branchRepository.findById(requestDTO.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + requestDTO.getBranchId()));
        
        Currency currency = currencyRepository.findByCurrencyCode(requestDTO.getCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + requestDTO.getCurrencyCode()));
        
        // Validate if customer already has this type of account
        List<Account> existingAccounts = accountRepository.findByCustomerId(customer.getId());
        boolean hasSimilarAccount = existingAccounts.stream()
                .anyMatch(a -> a.getAccountType().getId().equals(accountType.getId()) && 
                              a.getStatus() == AccountStatus.ACTIVE);
        if (hasSimilarAccount) {
            throw new BusinessException("Customer already has an active " + accountType.getTypeName() + " account");
        }
        
        // Generate account number
        String accountNumber = generateAccountNumber();
        
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .customer(customer)
                .accountType(accountType)
                .branch(branch)
                .currency(currency)
                .accountName(requestDTO.getAccountName() != null ? 
                            requestDTO.getAccountName() : 
                            customer.getFullName() + " - " + accountType.getTypeName())
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .interestRate(accountType.getInterestRate())
                .status(AccountStatus.ACTIVE)
                .isJointAccount(requestDTO.getIsJointAccount() != null ? requestDTO.getIsJointAccount() : false)
                .openingDate(LocalDate.now())
                .overdraftLimit(accountType.getOverdraftAllowed() ? BigDecimal.valueOf(100000) : BigDecimal.ZERO)
                .dailyTransactionLimit(accountType.getDailyWithdrawalLimit())
                .maintenanceFee(accountType.getMinimumBalance() != null ? 
                               accountType.getMinimumBalance().multiply(BigDecimal.valueOf(0.001)) : 
                               BigDecimal.ZERO)
                .build();
        
        Account savedAccount = accountRepository.save(account);
        log.info("Account opened with number: {}", savedAccount.getAccountNumber());
        
        return accountMapper.toDTO(savedAccount);
    }

    @Cacheable(value = "accounts", key = "#id")
    @Transactional(readOnly = true)
    public AccountDTO getAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        return accountMapper.toDTO(account);
    }

    @Transactional(readOnly = true)
    public AccountDTO getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with number: " + accountNumber));
        return accountMapper.toDTO(account);
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getCustomerAccounts(Long customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        return accountMapper.toDTOList(accounts);
    }
    
    @Transactional(readOnly = true)
    public Page<AccountDTO> listAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(accountMapper::toDTO);
    }

    @CacheEvict(value = "accounts", key = "#id")
    @Transactional
    public AccountDTO closeAccount(Long id) {
        log.warn("Closing account with ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Cannot close account with positive balance. Balance: " + account.getBalance());
        }
        
        account.setStatus(AccountStatus.CLOSED);
        account.setClosingDate(LocalDate.now());
        account.setUpdatedBy(getCurrentUsername());
        
        Account closedAccount = accountRepository.save(account);
        log.info("Account closed: {}", id);
        
        return accountMapper.toDTO(closedAccount);
    }

    @CacheEvict(value = "accounts", key = "#id")
    @Transactional
    public AccountDTO freezeAccount(Long id, String reason) {
        log.warn("Freezing account with ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        
        account.setStatus(AccountStatus.FROZEN);
        account.setUpdatedBy(getCurrentUsername());
        
        Account frozenAccount = accountRepository.save(account);
        log.info("Account frozen: {}", id);
        
        return accountMapper.toDTO(frozenAccount);
    }

    @CacheEvict(value = "accounts", key = "#id")
    @Transactional
    public AccountDTO activateAccount(Long id) {
        log.info("Activating account with ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        
        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedBy(getCurrentUsername());
        
        Account activatedAccount = accountRepository.save(account);
        log.info("Account activated: {}", id);
        
        return accountMapper.toDTO(activatedAccount);
    }

    @Transactional(readOnly = true)
    public Page<AccountDTO> getAccountsByBranch(Long branchId, Pageable pageable) {
        Page<Account> accounts = accountRepository.findByBranchId(branchId, pageable);
        return accounts.map(accountMapper::toDTO);
    }

    private String generateAccountNumber() {
        // Format: Branch Code (2 digits) + Account Type Code (2 chars) + Random (6 digits) + Check digit (1)
        String random = String.format("%06d", (int) (Math.random() * 1000000));
        return "ACC" + random + UUID.randomUUID().toString().substring(0, 2).toUpperCase();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() : "SYSTEM";
    }
}