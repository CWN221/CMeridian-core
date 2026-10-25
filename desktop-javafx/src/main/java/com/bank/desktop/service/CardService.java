package com.bank.desktop.service;

import com.bank.desktop.model.Card;
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
public class CardService {

    private final ApiClient apiClient = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Card> list(int page, int size) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/cards?page=" + page + "&size=" + size;
        Request req = apiClient.authenticatedRequest().url(url).get().build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "list cards");
            JsonNode content = mapper.readTree(body(resp)).get("content");
            List<Card> out = new ArrayList<>();
            if (content != null && content.isArray()) for (JsonNode n : content) out.add(parse(n));
            return out;
        }
    }

    public Card issue(Long customerId, Long accountId, String cardType, String cardBrand,
                      BigDecimal dailyLimit, BigDecimal monthlyLimit, BigDecimal singleLimit) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/cards";
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customerId", customerId);
        payload.put("accountId", accountId);
        payload.put("cardType", cardType);
        payload.put("cardBrand", cardBrand);
        payload.put("dailyLimit", dailyLimit);
        payload.put("monthlyLimit", monthlyLimit);
        payload.put("singleTransactionLimit", singleLimit);
        Request req = apiClient.authenticatedRequest().url(url)
                .post(RequestBody.create(mapper.writeValueAsString(payload),
                        MediaType.parse("application/json"))).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "issue card");
            return parse(mapper.readTree(body(resp)));
        }
    }

    public Card block(Long id, String reason) throws Exception {
        return post("/api/cards/" + id + "/block?reason=" + url(reason), "block card");
    }

    public Card unblock(Long id) throws Exception {
        return post("/api/cards/" + id + "/unblock", "unblock card");
    }

    public Card cancel(Long id) throws Exception {
        return post("/api/cards/" + id + "/cancel", "cancel card");
    }

    private Card post(String path, String action) throws Exception {
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

    private Card parse(JsonNode n) {
        Card c = new Card();
        c.setId(n.hasNonNull("id") ? n.get("id").asLong() : null);
        c.setCardNumber(n.path("cardNumber").asText(null));
        c.setCustomerId(n.hasNonNull("customerId") ? n.get("customerId").asLong() : null);
        c.setCustomerName(n.path("customerName").asText(null));
        c.setAccountId(n.hasNonNull("accountId") ? n.get("accountId").asLong() : null);
        c.setAccountNumber(n.path("accountNumber").asText(null));
        c.setCardType(n.path("cardType").asText(null));
        c.setCardBrand(n.path("cardBrand").asText(null));
        c.setCardholderName(n.path("cardholderName").asText(null));
        c.setExpiryDate(n.path("expiryDate").asText(null));
        c.setIssueDate(n.path("issueDate").asText(null));
        if (n.hasNonNull("dailyLimit")) c.setDailyLimit(new BigDecimal(n.get("dailyLimit").asText()));
        if (n.hasNonNull("monthlyLimit")) c.setMonthlyLimit(new BigDecimal(n.get("monthlyLimit").asText()));
        c.setStatus(n.path("status").asText(null));
        c.setBlockedReason(n.path("blockedReason").asText(null));
        return c;
    }
}