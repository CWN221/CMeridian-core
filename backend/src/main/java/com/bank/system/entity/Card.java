package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards", uniqueConstraints = {
    @UniqueConstraint(name = "uk_card_number", columnNames = "card_number")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card extends BaseEntity {

    @Column(name = "card_number", nullable = false, length = 19)
    private String cardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "card_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Column(name = "card_brand", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CardBrand cardBrand;

    @Column(name = "cardholder_name", nullable = false, length = 100)
    private String cardholderName;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "cvv_hash", nullable = false, length = 255)
    private String cvvHash;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "last_used_date")
    private LocalDateTime lastUsedDate;

    @Column(name = "daily_limit", precision = 15, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 15, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "single_transaction_limit", precision = 15, scale = 2)
    private BigDecimal singleTransactionLimit;
    
    @Column(name = "created_by")
    private String createdBy;

    @Builder.Default
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.ACTIVE;

    @Builder.Default
    @Column(name = "pin_attempts")
    private Integer pinAttempts = 0;

    @Column(name = "blocked_reason", length = 200)
    private String blockedReason;

    public enum CardType {
        DEBIT, CREDIT, PREPAID
    }

    public enum CardBrand {
        VISA, MASTERCARD, AMERICAN_EXPRESS, DISCOVER
    }

    public enum CardStatus {
        ACTIVE, BLOCKED, EXPIRED, CANCELLED
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
}