package com.bank.system.repository;

import com.bank.system.entity.Loan;
import com.bank.system.entity.Loan.LoanStatus;
import com.bank.system.entity.Loan.LoanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByLoanNumber(String loanNumber);

    List<Loan> findByCustomerId(Long customerId);

    List<Loan> findByCustomerIdAndStatus(Long customerId, LoanStatus status);

    @Query("SELECT l FROM Loan l WHERE l.status = :status")
    List<Loan> findByStatus(@Param("status") LoanStatus status);

    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.maturityDate < :date")
    List<Loan> findOverdueLoans(@Param("date") LocalDate date);

    @Query("SELECT l FROM Loan l WHERE l.status = 'PENDING' ORDER BY l.createdAt ASC")
    List<Loan> findPendingLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = :status")
    long countByStatus(@Param("status") LoanStatus status);

    @Query("SELECT SUM(l.principalAmount) FROM Loan l WHERE l.status = 'ACTIVE'")
    BigDecimal sumActiveLoanPrincipal();

    @Query("SELECT l FROM Loan l WHERE l.customer.id = :customerId ORDER BY l.createdAt DESC")
    Page<Loan> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT l.loanType, COUNT(l), SUM(l.principalAmount) " +
           "FROM Loan l GROUP BY l.loanType")
    List<Object[]> getLoanStatisticsByType();
}