package com.bank.system.service;

import com.bank.system.dto.TransactionDTO;
import com.bank.system.dto.TransferRequestDTO;
import com.bank.system.entity.Account;
import com.bank.system.entity.Currency;
import com.bank.system.entity.DailyTransactionLimit;
import com.bank.system.entity.Transaction;
import com.bank.system.entity.Transaction.TransactionStatus;
import com.bank.system.entity.Transaction.TransactionType;
import com.bank.system.exception.BusinessException;
import com.bank.system.exception.ResourceNotFoundException;
import com.bank.system.mapper.TransactionMapper;
import com.bank.system.repository.AccountRepository;
import com.bank.system.repository.CurrencyRepository;
import com.bank.system.repository.DailyTransactionLimitRepository;
import com.bank.system.repository.TransactionRepository;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CurrencyRepository currencyRepository;
    private final DailyTransactionLimitRepository dailyLimitRepository;
    private final TransactionMapper transactionMapper;

    private static final BigDecimal MAX_TRANSACTION_AMOUNT = BigDecimal.valueOf(10000000);
    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = BigDecimal.valueOf(500000);
    private static final BigDecimal DAILY_TRANSFER_LIMIT = BigDecimal.valueOf(1000000);

    @Transactional
    public TransactionDTO processDeposit(String accountNumber, BigDecimal amount, String description) {
        log.info("Processing deposit to account: {}, amount: {}", accountNumber, amount);
        
        validateAmount(amount);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        
        validateAccountActive(account);
        
        Currency currency = account.getCurrency();
        
        // Calculate balances
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        
        // Create transaction
        String reference = generateTransactionReference();
        Transaction transaction = Transaction.builder()
                .transactionReference(reference)
                .account(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .transactionDate(LocalDateTime.now())
                .description(description != null ? description : "Cash deposit")
                .status(TransactionStatus.COMPLETED)
                .currency(currency)
                .build();
        
        // Update account balance
        account.setBalance(balanceAfter);
        account.setAvailableBalance(balanceAfter);
        account.setLastTransactionDate(LocalDateTime.now());
        accountRepository.save(account);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Deposit processed successfully. Reference: {}", reference);
        
        return transactionMapper.toDTO(savedTransaction);
    }

    @Transactional
    public TransactionDTO processWithdrawal(String accountNumber, BigDecimal amount, String description) {
        log.info("Processing withdrawal from account: {}, amount: {}", accountNumber, amount);
        
        validateAmount(amount);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        
        validateAccountActive(account);
        validateSufficientBalance(account, amount);
        validateDailyLimit(account, amount, "WITHDRAWAL");
        
        Currency currency = account.getCurrency();
        
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        
        String reference = generateTransactionReference();
        Transaction transaction = Transaction.builder()
                .transactionReference(reference)
                .account(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .transactionDate(LocalDateTime.now())
                .description(description != null ? description : "Cash withdrawal")
                .status(TransactionStatus.COMPLETED)
                .currency(currency)
                .build();
        
        account.setBalance(balanceAfter);
        account.setAvailableBalance(balanceAfter);
        account.setLastTransactionDate(LocalDateTime.now());
        accountRepository.save(account);
        
        // Update daily limit tracking
        updateDailyLimit(account, amount, "WITHDRAWAL");
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Withdrawal processed successfully. Reference: {}", reference);
        
        return transactionMapper.toDTO(savedTransaction);
    }

    @Transactional
    public TransactionDTO processTransfer(TransferRequestDTO transferRequest) {
        log.info("Processing transfer from {} to {}, amount: {}", 
                transferRequest.getSourceAccountNumber(), 
                transferRequest.getDestinationAccountNumber(), 
                transferRequest.getAmount());
        
        BigDecimal amount = transferRequest.getAmount();
        validateAmount(amount);
        
        Account sourceAccount = accountRepository.findByAccountNumber(transferRequest.getSourceAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + 
                        transferRequest.getSourceAccountNumber()));
        
        Account destinationAccount = accountRepository.findByAccountNumber(transferRequest.getDestinationAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found: " + 
                        transferRequest.getDestinationAccountNumber()));
        
        validateAccountActive(sourceAccount);
        validateAccountActive(destinationAccount);
        validateSufficientBalance(sourceAccount, amount);
        validateDailyLimit(sourceAccount, amount, "TRANSFER");
        
        // Check if accounts are the same
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new BusinessException("Cannot transfer to the same account");
        }
        
        Currency sourceCurrency = sourceAccount.getCurrency();
        Currency destCurrency = destinationAccount.getCurrency();
        
        BigDecimal transferAmount = amount;
        BigDecimal exchangeRate = BigDecimal.ONE;
        
        // Handle currency conversion if different currencies
        if (!sourceCurrency.getId().equals(destCurrency.getId())) {
            exchangeRate = BigDecimal.valueOf(1.2);
            transferAmount = amount.multiply(exchangeRate);
        }
        
        // Process debit from source
        BigDecimal sourceBalanceBefore = sourceAccount.getBalance();
        BigDecimal sourceBalanceAfter = sourceBalanceBefore.subtract(amount);
        
        // Process credit to destination
        BigDecimal destBalanceBefore = destinationAccount.getBalance();
        BigDecimal destBalanceAfter = destBalanceBefore.add(transferAmount);
        
        // Create transaction reference
        String reference = generateTransactionReference();
        
        // Create debit transaction
        Transaction debitTransaction = Transaction.builder()
                .transactionReference(reference + "-D")
                .account(sourceAccount)
                .transactionType(TransactionType.TRANSFER)
                .amount(amount)
                .balanceBefore(sourceBalanceBefore)
                .balanceAfter(sourceBalanceAfter)
                .transactionDate(LocalDateTime.now())
                .description(transferRequest.getDescription() != null ? 
                            transferRequest.getDescription() : "Transfer to " + destinationAccount.getAccountNumber())
                .status(TransactionStatus.COMPLETED)
                .counterpartyAccountId(destinationAccount.getId())
                .counterpartyCustomerId(destinationAccount.getCustomer().getId())
                .currency(sourceCurrency)
                .exchangeRate(exchangeRate)
                .build();
        
        // Create credit transaction
        Transaction creditTransaction = Transaction.builder()
                .transactionReference(reference + "-C")
                .account(destinationAccount)
                .transactionType(TransactionType.TRANSFER)
                .amount(transferAmount)
                .balanceBefore(destBalanceBefore)
                .balanceAfter(destBalanceAfter)
                .transactionDate(LocalDateTime.now())
                .description(transferRequest.getDescription() != null ? 
                            transferRequest.getDescription() : "Transfer from " + sourceAccount.getAccountNumber())
                .status(TransactionStatus.COMPLETED)
                .counterpartyAccountId(sourceAccount.getId())
                .counterpartyCustomerId(sourceAccount.getCustomer().getId())
                .currency(destCurrency)
                .exchangeRate(exchangeRate)
                .build();
        
        // Update accounts
        sourceAccount.setBalance(sourceBalanceAfter);
        sourceAccount.setAvailableBalance(sourceBalanceAfter);
        sourceAccount.setLastTransactionDate(LocalDateTime.now());
        
        destinationAccount.setBalance(destBalanceAfter);
        destinationAccount.setAvailableBalance(destBalanceAfter);
        destinationAccount.setLastTransactionDate(LocalDateTime.now());
        
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
        
        // Save transactions
        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);
        
        // Update daily limits
        updateDailyLimit(sourceAccount, amount, "TRANSFER");
        
        log.info("Transfer processed successfully. Reference: {}", reference);
        
        return transactionMapper.toDTO(debitTransaction);
    }

    @Cacheable(value = "transactions", key = "#id")
    @Transactional(readOnly = true)
    public TransactionDTO getTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
        return transactionMapper.toDTO(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionDTO getTransactionByReference(String reference) {
        Transaction transaction = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with reference: " + reference));
        return transactionMapper.toDTO(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDTO> getAccountTransactions(Long accountId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        return transactions.map(transactionMapper::toDTO);
    }

    @CacheEvict(value = "transactions", key = "#id")
    @Transactional
    public TransactionDTO reverseTransaction(Long transactionId, String reason) {
        log.warn("Reversing transaction ID: {}, reason: {}", transactionId, reason);
        
        Transaction originalTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));
        
        if (originalTransaction.getStatus() == TransactionStatus.REVERSED) {
            throw new BusinessException("Transaction already reversed");
        }
        
        if (originalTransaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new BusinessException("Only completed transactions can be reversed");
        }
        
        Account account = originalTransaction.getAccount();
        BigDecimal reversalAmount = originalTransaction.getAmount().abs();
        
        // Create reverse transaction
        TransactionType transactionType = originalTransaction.getTransactionType();

        BigDecimal newBalance = originalTransaction.getBalanceBefore();
        
        // Reverse the transaction
        Transaction reversalTransaction = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .account(account)
                .transactionType(TransactionType.valueOf(transactionType.name()))
                .amount(reversalAmount)
                .balanceBefore(account.getBalance())
                .balanceAfter(newBalance)
                .transactionDate(LocalDateTime.now())
                .description("REVERSAL: " + reason + " (Original ref: " + originalTransaction.getTransactionReference() + ")")
                .status(TransactionStatus.COMPLETED)
                .relatedTransaction(originalTransaction)
                .currency(originalTransaction.getCurrency())
                .build();
        
        // Update account balance
        account.setBalance(newBalance);
        account.setAvailableBalance(newBalance);
        account.setLastTransactionDate(LocalDateTime.now());
        accountRepository.save(account);
        
        // Mark original as reversed
        originalTransaction.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(originalTransaction);
        
        Transaction savedReversal = transactionRepository.save(reversalTransaction);
        log.info("Transaction reversed successfully. Reversal ID: {}", savedReversal.getId());
        
        return transactionMapper.toDTO(savedReversal);
    }
    
    @Transactional(readOnly = true)
    public Page<TransactionDTO> listTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(transactionMapper::toDTO);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new BusinessException("Amount exceeds maximum allowed: " + MAX_TRANSACTION_AMOUNT);
        }
    }

    private void validateAccountActive(Account account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active. Status: " + account.getStatus());
        }
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance. Available: " + 
                    account.getAvailableBalance() + ", Requested: " + amount);
        }
    }

    private void validateDailyLimit(Account account, BigDecimal amount, String transactionType) {
        LocalDate today = LocalDate.now();
        DailyTransactionLimit dailyLimit = dailyLimitRepository.findByAccountIdAndTransactionDate(
                account.getId(), today)
                .orElseGet(() -> {
                    DailyTransactionLimit newLimit = new DailyTransactionLimit();
                    newLimit.setAccount(account);
                    newLimit.setTransactionDate(today);
                    newLimit.setTotalWithdrawals(BigDecimal.ZERO);
                    newLimit.setTotalTransfers(BigDecimal.ZERO);
                    newLimit.setTotalCount(0);
                    return newLimit;
                });
        
        BigDecimal currentTotal = "WITHDRAWAL".equals(transactionType) ? 
                dailyLimit.getTotalWithdrawals() : dailyLimit.getTotalTransfers();
        BigDecimal limit = "WITHDRAWAL".equals(transactionType) ? 
                DAILY_WITHDRAWAL_LIMIT : DAILY_TRANSFER_LIMIT;
        
        if (currentTotal.add(amount).compareTo(limit) > 0) {
            throw new BusinessException("Daily " + transactionType.toLowerCase() + 
                    " limit exceeded. Limit: " + limit + ", Used today: " + currentTotal);
        }
    }

    private void updateDailyLimit(Account account, BigDecimal amount, String transactionType) {
        LocalDate today = LocalDate.now();
        DailyTransactionLimit dailyLimit = dailyLimitRepository.findByAccountIdAndTransactionDate(
                account.getId(), today)
                .orElseGet(() -> {
                    DailyTransactionLimit newLimit = new DailyTransactionLimit();
                    newLimit.setAccount(account);
                    newLimit.setTransactionDate(today);
                    newLimit.setTotalWithdrawals(BigDecimal.ZERO);
                    newLimit.setTotalTransfers(BigDecimal.ZERO);
                    newLimit.setTotalCount(0);
                    return newLimit;
                });
        
        if ("WITHDRAWAL".equals(transactionType)) {
            dailyLimit.setTotalWithdrawals(dailyLimit.getTotalWithdrawals().add(amount));
        } else if ("TRANSFER".equals(transactionType)) {
            dailyLimit.setTotalTransfers(dailyLimit.getTotalTransfers().add(amount));
        }
        dailyLimit.setTotalCount(dailyLimit.getTotalCount() + 1);
        
        dailyLimitRepository.save(dailyLimit);
    }

    private String generateTransactionReference() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() : "SYSTEM";
    }
}