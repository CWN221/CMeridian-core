package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "beneficiaries",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_customer_beneficiary",
            columnNames = {"customer_id", "beneficiary_account_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Beneficiary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "customer_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_beneficiary_customer")
    )
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "beneficiary_account_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_beneficiary_account")
    )
    private Account beneficiaryAccount;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "relationship", length = 50)
    private String relationship;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
