package com.bank.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    // Stat cards
    private long activeCustomers;
    private long totalAccounts;
    private long transactionsToday;
    private long pendingLoans;

    private BigDecimal totalDeposits;
    private BigDecimal totalLoanPortfolio;
    private long blockedCards;
    private long frozenAccounts;

    private List<Map<String, Object>> recentTransactions;

    private List<Map<String, Object>> branchPerformance;

    private Map<String, Long> loanStatusBreakdown;
}