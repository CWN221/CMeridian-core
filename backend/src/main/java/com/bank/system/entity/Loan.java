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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans", uniqueConstraints = { @UniqueConstraint(name = "uk_loan_number", columnNames = "loan_number") })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan extends BaseEntity {

    @Column(name = "loan_number", nullable = false, length = 20)
    private String loanNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "loan_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @Column(name = "principal_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "loan_term_months", nullable = false)
    private Integer loanTermMonths;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "monthly_installment", precision = 20, scale = 2)
    private BigDecimal monthlyInstallment;

    @Column(name = "total_interest", precision = 20, scale = 2)
    private BigDecimal totalInterest;

    @Column(name = "total_payable", precision = 20, scale = 2)
    private BigDecimal totalPayable;

    @Builder.Default
    @Column(name = "amount_paid", precision = 20, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "outstanding_balance", precision = 20, scale = 2)
    private BigDecimal outstandingBalance;
    
    @Column(name = "created_by")
    private String createdBy;

    @Builder.Default
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approved_amount", precision = 20, scale = 2)
    private BigDecimal approvedAmount;
    
    @Builder.Default
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanPayment> loanPayments = new ArrayList<>();

    public enum LoanType {
        PERSONAL, MORTGAGE, AUTO, BUSINESS, EDUCATION
    }

    public enum LoanStatus {
        PENDING, APPROVED, DISBURSED, ACTIVE, CLOSED, DEFAULTED
    }

    public BigDecimal calculateOutstandingBalance() {
        if (principalAmount == null || amountPaid == null) {
            return BigDecimal.ZERO;
        }
        return principalAmount.subtract(amountPaid);
    }
}