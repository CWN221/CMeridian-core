package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "loan_payments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_loan_payment_reference",
            columnNames = "payment_reference"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class LoanPayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "loan_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_lp_loan")
    )
    @ToString.Exclude
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "transaction_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_lp_transaction")
    )
    @ToString.Exclude
    private Transaction transaction;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    @Column(name = "principal_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal interestAmount;

    @Builder.Default
    @Column(name = "penalty_amount", precision = 15, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column(name = "payment_reference", unique = true, length = 50)
    private String paymentReference;
}
