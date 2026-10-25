package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "currencies",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_currency_code",
            columnNames = "currency_code"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Currency extends BaseEntity {

    @Column(name = "currency_code", nullable = false, unique = true, length = 3)
    private String currencyCode;

    @Column(name = "currency_name", nullable = false, length = 50)
    private String currencyName;

    @Column(name = "symbol", length = 5)
    private String symbol;

    @Builder.Default
    @Column(name = "decimal_places")
    private Integer decimalPlaces = 2;

    @Builder.Default
    @Column(name = "is_base_currency")
    private Boolean isBaseCurrency = false;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
