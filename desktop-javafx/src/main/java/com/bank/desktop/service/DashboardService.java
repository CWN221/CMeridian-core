package com.bank.desktop.service;

import com.bank.desktop.model.DashboardSnapshot;
import com.bank.desktop.util.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DashboardService {

    private final ApiClient apiClient = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public DashboardSnapshot fetch() throws Exception {
        String url = apiClient.getBaseUrl() + "/api/dashboard";
        Request req = apiClient.authenticatedRequest().url(url).get().build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String b = resp.body() != null ? resp.body().string() : "";
                throw new Exception("Dashboard load failed (" + resp.code() + "): " + b);
            }
            String body = resp.body() != null ? resp.body().string() : "{}";
            return parse(mapper.readTree(body));
        }
    }

    private DashboardSnapshot parse(JsonNode n) {
        DashboardSnapshot d = new DashboardSnapshot();
        d.setActiveCustomers(n.path("activeCustomers").asLong());
        d.setTotalAccounts(n.path("totalAccounts").asLong());
        d.setTransactionsToday(n.path("transactionsToday").asLong());
        d.setPendingLoans(n.path("pendingLoans").asLong());
        d.setBlockedCards(n.path("blockedCards").asLong());
        d.setFrozenAccounts(n.path("frozenAccounts").asLong());

        if (n.hasNonNull("totalDeposits"))
            d.setTotalDeposits(new BigDecimal(n.get("totalDeposits").asText()));
        if (n.hasNonNull("totalLoanPortfolio"))
            d.setTotalLoanPortfolio(new BigDecimal(n.get("totalLoanPortfolio").asText()));

        List<Map<String, Object>> recent = new ArrayList<>();
        JsonNode rows = n.get("recentTransactions");
        if (rows != null && rows.isArray()) {
            for (JsonNode row : rows) {
                Map<String, Object> m = new LinkedHashMap<>();
                row.fields().forEachRemaining(e ->
                        m.put(e.getKey(), e.getValue().isNull() ? null : e.getValue().asText()));
                recent.add(m);
            }
        }
        d.setRecentTransactions(recent);

        List<Map<String, Object>> branches = new ArrayList<>();
        JsonNode bp = n.get("branchPerformance");
        if (bp != null && bp.isArray()) {
            for (JsonNode row : bp) {
                Map<String, Object> m = new LinkedHashMap<>();
                row.fields().forEachRemaining(e ->
                        m.put(e.getKey(), e.getValue().isNull() ? null : e.getValue().asText()));
                branches.add(m);
            }
        }
        d.setBranchPerformance(branches);

        Map<String, Long> loanStatus = new LinkedHashMap<>();
        JsonNode ls = n.get("loanStatusBreakdown");
        if (ls != null && ls.isObject()) {
            ls.fields().forEachRemaining(e -> loanStatus.put(e.getKey(), e.getValue().asLong()));
        }
        d.setLoanStatusBreakdown(loanStatus);
        return d;
    }
}