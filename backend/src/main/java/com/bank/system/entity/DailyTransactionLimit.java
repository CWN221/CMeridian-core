package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "daily_transaction_limits",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_date", columnNames = {"account_id", "transaction_date"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DailyTransactionLimit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "account_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_dtl_account")
    )
    @ToString.Exclude
    private Account account;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Builder.Default
    @Column(name = "total_withdrawals", precision = 20, scale = 2)
    private BigDecimal totalWithdrawals = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_transfers", precision = 20, scale = 2)
    private BigDecimal totalTransfers = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_count")
    private Integer totalCount = 0;
}
