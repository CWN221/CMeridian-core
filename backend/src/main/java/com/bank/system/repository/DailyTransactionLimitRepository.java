package com.bank.system.repository;

import com.bank.system.entity.DailyTransactionLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyTransactionLimitRepository extends JpaRepository<DailyTransactionLimit, Long> {
    Optional<DailyTransactionLimit> findByAccountIdAndTransactionDate(Long accountId, LocalDate date);
}