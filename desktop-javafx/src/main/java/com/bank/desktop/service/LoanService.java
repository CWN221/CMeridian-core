package com.bank.desktop.service;

import com.bank.desktop.model.Loan;
import com.bank.desktop.util.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class LoanService {

    private final ApiClient apiClient = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Loan> list(int page, int size) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/loans?page=" + page + "&size=" + size;
        Request req = apiClient.authenticatedRequest().url(url).get().build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "list loans");
            JsonNode content = mapper.readTree(body(resp)).get("content");
            List<Loan> out = new ArrayList<>();
            if (content != null && content.isArray()) for (JsonNode n : content) out.add(parse(n));
            return out;
        }
    }

    public Loan create(Long customerId, Long accountId, String loanType,
                       BigDecimal principal, BigDecimal rate, Integer term) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/loans";
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customerId", customerId);
        payload.put("accountId", accountId);
        payload.put("loanType", loanType);
        payload.put("principalAmount", principal);
        payload.put("interestRate", rate);
        payload.put("loanTermMonths", term);
        Request req = apiClient.authenticatedRequest().url(url)
                .post(RequestBody.create(mapper.writeValueAsString(payload),
                        MediaType.parse("application/json"))).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "create loan");
            return parse(mapper.readTree(body(resp)));
        }
    }

    public Loan approve(Long id) throws Exception {
        return postSimple("/api/loans/" + id + "/approve", "approve loan");
    }

    public Loan reject(Long id, String reason) throws Exception {
        return postSimple("/api/loans/" + id + "/reject?reason=" + url(reason), "reject loan");
    }

    public Loan disburse(Long id) throws Exception {
        return postSimple("/api/loans/" + id + "/disburse", "disburse loan");
    }

    public Loan close(Long id) throws Exception {
        return postSimple("/api/loans/" + id + "/close", "close loan");
    }

    public Loan repay(Long id, BigDecimal amount, String description) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/loans/" + id + "/repay";
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("amount", amount);
        payload.put("description", description);
        Request req = apiClient.authenticatedRequest().url(url)
                .post(RequestBody.create(mapper.writeValueAsString(payload),
                        MediaType.parse("application/json"))).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "repay loan");
            return parse(mapper.readTree(body(resp)));
        }
    }

    private Loan postSimple(String path, String action) throws Exception {
        Request req = apiClient.authenticatedRequest()
                .url(apiClient.getBaseUrl() + path).post(RequestBody.create("", null)).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, action);
            return parse(mapper.readTree(body(resp)));
        }
    }

    private String body(Response r) throws Exception {
        return r.body() != null ? r.body().string() : "{}";
    }

    private void check(Response r, String action) throws Exception {
        if (r.isSuccessful()) return;
        String b = body(r);
        String msg = extract(b);
        log.error("{} failed: {} {}", action, r.code(), b);
        throw new Exception(msg != null ? msg : action + " failed (" + r.code() + ")");
    }

    private String extract(String b) {
        if (b == null || b.isBlank()) return null;
        try {
            JsonNode n = mapper.readTree(b);
            if (n.hasNonNull("message")) return n.get("message").asText();
        } catch (Exception ignored) {}
        return null;
    }

    private String url(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    private Loan parse(JsonNode n) {
        Loan l = new Loan();
        l.setId(n.hasNonNull("id") ? n.get("id").asLong() : null);
        l.setLoanNumber(n.path("loanNumber").asText(null));
        l.setCustomerId(n.hasNonNull("customerId") ? n.get("customerId").asLong() : null);
        l.setCustomerName(n.path("customerName").asText(null));
        l.setAccountId(n.hasNonNull("accountId") ? n.get("accountId").asLong() : null);
        l.setAccountNumber(n.path("accountNumber").asText(null));
        l.setLoanType(n.path("loanType").asText(null));
        if (n.hasNonNull("principalAmount")) l.setPrincipalAmount(new BigDecimal(n.get("principalAmount").asText()));
        if (n.hasNonNull("interestRate")) l.setInterestRate(new BigDecimal(n.get("interestRate").asText()));
        if (n.hasNonNull("loanTermMonths")) l.setLoanTermMonths(n.get("loanTermMonths").asInt());
        l.setDisbursementDate(n.path("disbursementDate").asText(null));
        l.setMaturityDate(n.path("maturityDate").asText(null));
        if (n.hasNonNull("monthlyInstallment")) l.setMonthlyInstallment(new BigDecimal(n.get("monthlyInstallment").asText()));
        if (n.hasNonNull("outstandingBalance")) l.setOutstandingBalance(new BigDecimal(n.get("outstandingBalance").asText()));
        if (n.hasNonNull("amountPaid")) l.setAmountPaid(new BigDecimal(n.get("amountPaid").asText()));
        l.setStatus(n.path("status").asText(null));
        return l;
    }
}