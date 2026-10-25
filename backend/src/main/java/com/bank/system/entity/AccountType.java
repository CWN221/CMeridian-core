package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "account_types",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_type_code", columnNames = "type_code")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AccountType extends BaseEntity {

    @Column(name = "type_code", nullable = false, length = 20)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;

    @Column(name = "description", length = 200)
    private String description;

    @Builder.Default
    @Column(name = "minimum_balance", precision = 15, scale = 2)
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "overdraft_allowed")
    private Boolean overdraftAllowed = false;

    @Column(name = "daily_withdrawal_limit", precision = 15, scale = 2)
    private BigDecimal dailyWithdrawalLimit;

    @Column(name = "monthly_transaction_limit")
    private Integer monthlyTransactionLimit;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
