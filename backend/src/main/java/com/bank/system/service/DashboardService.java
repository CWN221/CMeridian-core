package com.bank.system.service;

import com.bank.system.dto.DashboardDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardDTO snapshot() {
        long activeCustomers = countLong(
                "SELECT COUNT(*) FROM customers WHERE is_active = true");

        long totalAccounts = countLong(
                "SELECT COUNT(*) FROM accounts");

        long transactionsToday = countLong(
                "SELECT COUNT(*) FROM transactions " +
                "WHERE transaction_date >= CURRENT_DATE " +
                "AND transaction_date < CURRENT_DATE + INTERVAL '1 day'");

        long pendingLoans = countLong(
                "SELECT COUNT(*) FROM loans WHERE status = 'PENDING'");

        BigDecimal totalDeposits = sumDecimal(
                "SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE status = 'ACTIVE'");

        BigDecimal totalLoanPortfolio = sumDecimal(
                "SELECT COALESCE(SUM(principal_amount), 0) FROM loans " +
                "WHERE status IN ('ACTIVE', 'DISBURSED')");

        long blockedCards = countLong(
                "SELECT COUNT(*) FROM cards WHERE status = 'BLOCKED'");

        long frozenAccounts = countLong(
                "SELECT COUNT(*) FROM accounts WHERE status = 'FROZEN'");

        List<Map<String, Object>> recentTransactions = jdbcTemplate.queryForList("""
                SELECT t.transaction_reference AS "reference",
                       t.transaction_date     AS "date",
                       t.transaction_type     AS "type",
                       a.account_number       AS "account",
                       c.first_name || ' ' || c.last_name AS "customer",
                       t.amount               AS "amount",
                       t.status               AS "status"
                FROM transactions t
                JOIN accounts a ON a.id = t.account_id
                JOIN customers c ON c.id = a.customer_id
                ORDER BY t.transaction_date DESC
                LIMIT 10
                """);

        List<Map<String, Object>> branchPerformance = jdbcTemplate.queryForList("""
                SELECT b.branch_name AS "branch",
                       b.city        AS "city",
                       COUNT(DISTINCT a.id)             AS "accounts",
                       COALESCE(SUM(a.balance), 0)      AS "deposits"
                FROM branches b
                LEFT JOIN accounts a ON a.branch_id = b.id AND a.status = 'ACTIVE'
                WHERE b.is_active = true
                GROUP BY b.id, b.branch_name, b.city
                ORDER BY deposits DESC
                LIMIT 5
                """);

        Map<String, Long> loanStatusBreakdown = new LinkedHashMap<>();
        jdbcTemplate.query("SELECT status, COUNT(*) AS c FROM loans GROUP BY status",
                rs -> {
                    loanStatusBreakdown.put(rs.getString("status"), rs.getLong("c"));
                });

        return DashboardDTO.builder()
                .activeCustomers(activeCustomers)
                .totalAccounts(totalAccounts)
                .transactionsToday(transactionsToday)
                .pendingLoans(pendingLoans)
                .totalDeposits(totalDeposits)
                .totalLoanPortfolio(totalLoanPortfolio)
                .blockedCards(blockedCards)
                .frozenAccounts(frozenAccounts)
                .recentTransactions(recentTransactions)
                .branchPerformance(branchPerformance)
                .loanStatusBreakdown(loanStatusBreakdown)
                .build();
    }

    private long countLong(String sql) {
        Long v = jdbcTemplate.queryForObject(sql, Long.class);
        return v == null ? 0L : v;
    }

    private BigDecimal sumDecimal(String sql) {
        BigDecimal v = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return v == null ? BigDecimal.ZERO : v;
    }
}