package com.bank.system.repository;

import com.bank.system.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByCustomerId(Long customerId);

    List<Card> findByAccountId(Long accountId);

    List<Card> findByCustomerIdAndStatus(Long customerId, Card.CardStatus status);

    List<Card> findByAccountIdAndStatus(Long accountId, Card.CardStatus status);

    @Query("SELECT c FROM Card c WHERE c.status = :status")
    Page<Card> findByStatus(@Param("status") Card.CardStatus status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.status = :status")
    long countByStatus(@Param("status") Card.CardStatus status);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT c FROM Card c WHERE c.expiryDate <= :date AND c.status = 'ACTIVE'")
    List<Card> findExpiringCards(@Param("date") LocalDate date);

    @Query("SELECT c FROM Card c WHERE c.expiryDate < :today AND c.status <> 'CANCELLED'")
    List<Card> findExpiredCards(@Param("today") LocalDate today);

    @Query("SELECT c FROM Card c WHERE " +
           "LOWER(c.cardNumber) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.cardholderName) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<Card> searchByNumberOrHolder(@Param("term") String term, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.customer.id = :customerId " +
           "ORDER BY c.issueDate DESC")
    List<Card> findByCustomerIdOrderedByIssueDate(@Param("customerId") Long customerId);

    boolean existsByCardNumber(String cardNumber);

    @Query("SELECT c.cardBrand, COUNT(c) FROM Card c GROUP BY c.cardBrand")
    List<Object[]> countByBrand();

    @Query("SELECT c.cardType, COUNT(c) FROM Card c GROUP BY c.cardType")
    List<Object[]> countByType();
}