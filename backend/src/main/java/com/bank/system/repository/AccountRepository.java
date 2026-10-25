package com.bank.system.repository;

import com.bank.system.entity.Account;
import com.bank.system.entity.Account.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    List<Account> findByCustomerIdAndStatus(Long customerId, AccountStatus status);

    Page<Account> findByBranchId(Long branchId, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.customer.id = :customerId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT a FROM Account a WHERE a.balance < :threshold AND a.status = 'ACTIVE'")
    List<Account> findAccountsBelowBalanceThreshold(@Param("threshold") BigDecimal threshold);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = :status")
    long countByStatus(@Param("status") AccountStatus status);

    @Query("SELECT a FROM Account a WHERE a.branch.id = :branchId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT a.accountType.typeCode, COUNT(a), SUM(a.balance) " +
           "FROM Account a GROUP BY a.accountType.typeCode")
    List<Object[]> getAccountStatisticsByType();

    @Query("SELECT a FROM Account a WHERE a.lastTransactionDate IS NULL OR " +
           "a.lastTransactionDate < :date AND a.status = 'ACTIVE'")
    List<Account> findDormantAccounts(@Param("date") java.time.LocalDateTime date);
}