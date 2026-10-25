package com.bank.system.repository;

import com.bank.system.entity.Transaction;
import com.bank.system.entity.Transaction.TransactionStatus;
import com.bank.system.entity.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId ORDER BY t.transactionDate DESC")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountIdAndDateRange(@Param("accountId") Long accountId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId AND t.status = :status")
    List<Transaction> findByAccountIdAndStatus(@Param("accountId") Long accountId,
                                               @Param("status") TransactionStatus status);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.transactionType = :type AND t.transactionDate >= :startDate")
    BigDecimal sumTransactionsByTypeAndDate(@Param("accountId") Long accountId,
                                           @Param("type") TransactionType type,
                                           @Param("startDate") LocalDateTime startDate);

    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' AND t.transactionDate < :timeout")
    List<Transaction> findPendingTransactionsOlderThan(@Param("timeout") LocalDateTime timeout);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.transactionDate >= :startDate")
    long countTransactionsSince(@Param("accountId") Long accountId,
                               @Param("startDate") LocalDateTime startDate);

    @Query("SELECT DATE(t.transactionDate) as date, COUNT(t), SUM(t.amount) " +
           "FROM Transaction t WHERE t.account.id = :accountId " +
           "GROUP BY DATE(t.transactionDate) ORDER BY DATE(t.transactionDate) DESC")
    List<Object[]> getDailyTransactionSummary(@Param("accountId") Long accountId);
}