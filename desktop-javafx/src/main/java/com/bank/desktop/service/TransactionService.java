package com.bank.desktop.service;

import com.bank.desktop.model.Transaction;
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
public class TransactionService {

    private final ApiClient apiClient = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Transaction> list(int page, int size) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/transactions?page=" + page + "&size=" + size;
        Request req = apiClient.authenticatedRequest().url(url).get().build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "list transactions");
            JsonNode content = mapper.readTree(body(resp)).get("content");
            List<Transaction> out = new ArrayList<>();
            if (content != null && content.isArray()) for (JsonNode n : content) out.add(parse(n));
            return out;
        }
    }

    public Transaction deposit(String accountNumber, BigDecimal amount, String description) throws Exception {
        String url = apiClient.getBaseUrl()
                + "/api/transactions/deposit?accountNumber=" + accountNumber
                + "&amount=" + amount
                + (description != null ? "&description=" + url(description) : "");
        Request req = apiClient.authenticatedRequest().url(url).post(RequestBody.create("", null)).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "deposit");
            return parse(mapper.readTree(body(resp)));
        }
    }

    public Transaction withdrawal(String accountNumber, BigDecimal amount, String description) throws Exception {
        String url = apiClient.getBaseUrl()
                + "/api/transactions/withdrawal?accountNumber=" + accountNumber
                + "&amount=" + amount
                + (description != null ? "&description=" + url(description) : "");
        Request req = apiClient.authenticatedRequest().url(url).post(RequestBody.create("", null)).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "withdrawal");
            return parse(mapper.readTree(body(resp)));
        }
    }

    public Transaction transfer(String source, String dest, BigDecimal amount, String description) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/transactions/transfer";
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sourceAccountNumber", source);
        payload.put("destinationAccountNumber", dest);
        payload.put("amount", amount);
        payload.put("description", description);
        String json = mapper.writeValueAsString(payload);
        Request req = apiClient.authenticatedRequest().url(url)
                .post(RequestBody.create(json, MediaType.parse("application/json"))).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "transfer");
            return parse(mapper.readTree(body(resp)));
        }
    }

    public void reverse(Long transactionId, String reason) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/transactions/" + transactionId
                + "/reverse?reason=" + url(reason);
        Request req = apiClient.authenticatedRequest().url(url).post(RequestBody.create("", null)).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "reverse");
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

    private Transaction parse(JsonNode n) {
        Transaction t = new Transaction();
        t.setId(n.hasNonNull("id") ? n.get("id").asLong() : null);
        t.setTransactionReference(n.path("transactionReference").asText(null));
        t.setAccountId(n.hasNonNull("accountId") ? n.get("accountId").asLong() : null);
        t.setAccountNumber(n.path("accountNumber").asText(null));
        t.setTransactionType(n.path("transactionType").asText(null));
        if (n.hasNonNull("amount")) t.setAmount(new BigDecimal(n.get("amount").asText()));
        t.setTransactionDate(n.path("transactionDate").asText(null));
        t.setDescription(n.path("description").asText(null));
        t.setStatus(n.path("status").asText(null));
        t.setCurrencyCode(n.path("currencyCode").asText(null));
        return t;
    }
}