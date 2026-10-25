package com.bank.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSnapshot {

    private long activeCustomers;
    private long totalAccounts;
    private long transactionsToday;
    private long pendingLoans;
    private long blockedCards;
    private long frozenAccounts;
    private BigDecimal totalDeposits;
    private BigDecimal totalLoanPortfolio;

    @Builder.Default
    private List<Map<String, Object>> recentTransactions = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> branchPerformance = new ArrayList<>();

    @Builder.Default
    private Map<String, Long> loanStatusBreakdown = new LinkedHashMap<>();

    public String formattedDeposits() {
        return totalDeposits == null ? "0" : String.format("%,.0f", totalDeposits);
    }

    public String formattedLoanPortfolio() {
        return totalLoanPortfolio == null ? "0" : String.format("%,.0f", totalLoanPortfolio);
    }
}