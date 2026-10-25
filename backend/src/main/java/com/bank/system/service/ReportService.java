package com.bank.system.service;

import com.bank.system.dto.ReportDTO;
import com.bank.system.dto.ReportRequestDTO;
import com.bank.system.entity.Report;
import com.bank.system.exception.BusinessException;
import com.bank.system.exception.ResourceNotFoundException;
import com.bank.system.repository.ReportRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReportDTO generate(ReportRequestDTO request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date must be on or after start date");
        }

        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();
        String type = request.getReportType();

        List<Map<String, Object>> rows;
        Map<String, Object> summary;

        switch (type) {
            case "DAILY_TRANSACTIONS" -> {
                rows = dailyTransactions(start, end);
                summary = dailyTransactionsSummary(start, end);
            }
            case "CUSTOMER_PORTFOLIO" -> {
                rows = customerPortfolio(start, end);
                summary = customerPortfolioSummary();
            }
            case "ACCOUNT_BALANCES" -> {
                rows = accountBalances(start, end);
                summary = accountBalancesSummary();
            }
            case "LOAN_PORTFOLIO" -> {
                rows = loanPortfolio(start, end);
                summary = loanPortfolioSummary();
            }
            default -> throw new BusinessException("Unknown report type: " + type);
        }

        Report entity = Report.builder()
                .reportType(type)
                .periodStart(start)
                .periodEnd(end)
                .periodLabel(label(start, end))
                .generatedOn(LocalDateTime.now())
                .generatedBy(currentUsername())
                .rowCount(rows.size())
                .payload(toJson(Map.of("rows", rows, "summary", summary)))
                .createdBy(currentUsername())
                .build();

        Report saved = reportRepository.save(entity);
        log.info("Report generated: type={}, rows={}, by={}", type, rows.size(), saved.getGeneratedBy());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReportDTO> history(Pageable pageable) {
        return reportRepository.findAllByOrderByGeneratedOnDesc(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ReportDTO get(Long id) {
        return toDTO(reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + id)));
    }

    @Transactional
    public void delete(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report not found: " + id);
        }
        reportRepository.deleteById(id);
    }


    private List<Map<String, Object>> dailyTransactions(LocalDate start, LocalDate end) {
        String sql = """
                SELECT t.transaction_reference AS "reference",
                       t.transaction_date AS "date",
                       t.transaction_type AS "type",
                       a.account_number AS "account",
                       c.first_name || ' ' || c.last_name AS "customer",
                       t.amount AS "amount",
                       t.status AS "status"
                FROM transactions t
                JOIN accounts a ON a.id = t.account_id
                JOIN customers c ON c.id = a.customer_id
                WHERE t.transaction_date >= ? AND t.transaction_date < ?
                ORDER BY t.transaction_date DESC
                """;
        return jdbcTemplate.queryForList(sql, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private Map<String, Object> dailyTransactionsSummary(LocalDate start, LocalDate end) {
        String sql = """
                SELECT COUNT(*) AS "count",
                       COALESCE(SUM(amount), 0) AS "totalVolume",
                       COALESCE(AVG(amount), 0) AS "averageAmount"
                FROM transactions
                WHERE transaction_date >= ? AND transaction_date < ?
                """;
        return jdbcTemplate.queryForMap(sql, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private List<Map<String, Object>> customerPortfolio(LocalDate start, LocalDate end) {
        String sql = """
                SELECT c.customer_number AS "customerNumber",
                       c.first_name || ' ' || c.last_name AS "name",
                       c.email AS "email",
                       c.phone AS "phone",
                       c.is_verified AS "verified",
                       c.is_active AS "active",
                       (SELECT COUNT(*) FROM accounts a WHERE a.customer_id = c.id) AS "accounts",
                       (SELECT COALESCE(SUM(balance), 0) FROM accounts a WHERE a.customer_id = c.id) AS "totalBalance"
                FROM customers c
                WHERE c.created_at >= ? AND c.created_at < ?
                ORDER BY c.created_at DESC
                """;
        return jdbcTemplate.queryForList(sql, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private Map<String, Object> customerPortfolioSummary() {
        String sql = """
                SELECT COUNT(*) AS "total",
                       COUNT(*) FILTER (WHERE is_verified = true) AS "verified",
                       COUNT(*) FILTER (WHERE is_active = false) AS "deactivated"
                FROM customers
                """;
        return jdbcTemplate.queryForMap(sql);
    }

    private List<Map<String, Object>> accountBalances(LocalDate start, LocalDate end) {
        String sql = """
                SELECT a.account_number AS "accountNumber",
                       c.first_name || ' ' || c.last_name AS "customer",
                       at.type_name AS "type",
                       a.currency_id AS "currency",
                       a.balance AS "balance",
                       a.available_balance AS "availableBalance",
                       a.status AS "status"
                FROM accounts a
                JOIN customers c ON c.id = a.customer_id
                JOIN account_types at ON at.id = a.account_type_id
                WHERE a.opening_date >= ? AND a.opening_date <= ?
                ORDER BY a.balance DESC
                """;
        return jdbcTemplate.queryForList(sql, start, end);
    }

    private Map<String, Object> accountBalancesSummary() {
        String sql = """
                SELECT COUNT(*) AS "count",
                       COALESCE(SUM(balance), 0) AS "totalBalance",
                       COUNT(*) FILTER (WHERE status = 'FROZEN') AS "frozen",
                       COUNT(*) FILTER (WHERE status = 'CLOSED') AS "closed"
                FROM accounts
                """;
        return jdbcTemplate.queryForMap(sql);
    }

    private List<Map<String, Object>> loanPortfolio(LocalDate start, LocalDate end) {
        String sql = """
                SELECT l.loan_number AS "loanNumber",
                       c.first_name || ' ' || c.last_name AS "customer",
                       l.loan_type AS "type",
                       l.principal_amount AS "principal",
                       l.outstanding_balance AS "outstanding",
                       l.interest_rate AS "interestRate",
                       l.status AS "status"
                FROM loans l
                JOIN customers c ON c.id = l.customer_id
                WHERE l.created_at >= ? AND l.created_at < ?
                ORDER BY l.principal_amount DESC
                """;
        return jdbcTemplate.queryForList(sql, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private Map<String, Object> loanPortfolioSummary() {
        String sql = """
                SELECT COUNT(*) AS "count",
                       COALESCE(SUM(principal_amount), 0) AS "totalPrincipal",
                       COALESCE(SUM(outstanding_balance), 0) AS "totalOutstanding",
                       COUNT(*) FILTER (WHERE status = 'DEFAULTED') AS "defaulted"
                FROM loans
                """;
        return jdbcTemplate.queryForMap(sql);
    }

    private List<Map<String, Object>> branchPerformance(LocalDate start, LocalDate end) {
        String sql = """
                SELECT b.branch_name AS "branch",
                       b.city AS "city",
                       COUNT(DISTINCT a.id) AS "accounts",
                       COALESCE(SUM(a.balance), 0) AS "totalDeposits"
                FROM branches b
                LEFT JOIN accounts a ON a.branch_id = b.id
                WHERE b.is_active = true
                GROUP BY b.id, b.branch_name, b.city
                ORDER BY totalDeposits DESC
                """;
        return jdbcTemplate.queryForList(sql);
    }

    private Map<String, Object> branchPerformanceSummary(LocalDate start, LocalDate end) {
        String sql = """
                SELECT COUNT(*) AS "branches",
                       COALESCE(SUM(a.balance), 0) AS "totalDeposits"
                FROM branches b
                LEFT JOIN accounts a ON a.branch_id = b.id
                WHERE b.is_active = true
                """;
        return jdbcTemplate.queryForMap(sql);
    }


    private ReportDTO toDTO(Report r) {
        List<Map<String, Object>> rows = List.of();
        Map<String, Object> summary = Map.of();
        if (r.getPayload() != null && !r.getPayload().isBlank()) {
            try {
                var node = objectMapper.readTree(r.getPayload());
                if (node.has("rows")) {
                    rows = objectMapper.convertValue(node.get("rows"),
                            new TypeReference<List<Map<String, Object>>>() {});
                }
                if (node.has("summary")) {
                    summary = objectMapper.convertValue(node.get("summary"),
                            new TypeReference<Map<String, Object>>() {});
                }
            } catch (Exception e) {
                log.warn("Failed to parse report payload: {}", e.getMessage());
            }
        }
        return ReportDTO.builder()
                .id(r.getId())
                .reportType(r.getReportType())
                .periodStart(r.getPeriodStart())
                .periodEnd(r.getPeriodEnd())
                .periodLabel(r.getPeriodLabel())
                .generatedOn(r.getGeneratedOn())
                .generatedBy(r.getGeneratedBy())
                .rowCount(r.getRowCount())
                .rows(rows)
                .summary(summary)
                .build();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BusinessException("Failed to serialize report: " + e.getMessage());
        }
    }

    private String label(LocalDate start, LocalDate end) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return start.equals(end) ? start.format(f) : start.format(f) + " - " + end.format(f);
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}