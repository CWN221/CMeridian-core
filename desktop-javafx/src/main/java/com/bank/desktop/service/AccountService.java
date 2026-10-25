package com.bank.desktop.service;

import com.bank.desktop.model.Account;
import com.bank.desktop.model.AccountTypeRef;
import com.bank.desktop.model.BranchRef;
import com.bank.desktop.model.CurrencyRef;
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
public class AccountService {

    private final ApiClient apiClient = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Account> list(int page, int size) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/accounts?page=" + page + "&size=" + size;
        Request req = apiClient.authenticatedRequest().url(url).get().build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "list accounts");
            JsonNode content = mapper.readTree(body(resp)).get("content");
            List<Account> out = new ArrayList<>();
            if (content != null && content.isArray()) for (JsonNode n : content) out.add(parse(n));
            return out;
        }
    }

    public Account open(Long customerId, String accountTypeCode, Long branchId,
                        String currencyCode, String accountName, BigDecimal initialDeposit) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/accounts/open";
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customerId", customerId);
        payload.put("accountTypeCode", accountTypeCode);
        payload.put("branchId", branchId);
        payload.put("currencyCode", currencyCode);
        payload.put("accountName", accountName);
        payload.put("initialDeposit", initialDeposit);
        String json = mapper.writeValueAsString(payload);
        Request req = apiClient.authenticatedRequest().url(url)
                .post(RequestBody.create(json, MediaType.parse("application/json"))).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "open account");
            return parse(mapper.readTree(body(resp)));
        }
    }

    public void freeze(Long id, String reason) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/accounts/" + id + "/freeze?reason=" + reason;
        Request req = apiClient.authenticatedRequest().url(url).post(RequestBody.create("", null)).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "freeze account");
        }
    }

    public void activate(Long id) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/accounts/" + id + "/activate";
        Request req = apiClient.authenticatedRequest().url(url).post(RequestBody.create("", null)).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "activate account");
        }
    }

    public void close(Long id) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/accounts/" + id + "/close";
        Request req = apiClient.authenticatedRequest().url(url).post(RequestBody.create("", null)).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "close account");
        }
    }

    public List<AccountTypeRef> accountTypes() throws Exception {
        Request req = apiClient.authenticatedRequest()
                .url(apiClient.getBaseUrl() + "/api/account-types").get().build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "account types");
            JsonNode arr = mapper.readTree(body(resp));
            List<AccountTypeRef> out = new ArrayList<>();
            if (arr.isArray()) for (JsonNode n : arr)
                out.add(new AccountTypeRef(n.path("typeCode").asText(), n.path("typeName").asText()));
            return out;
        }
    }

    public List<BranchRef> branches() throws Exception {
        Request req = apiClient.authenticatedRequest()
                .url(apiClient.getBaseUrl() + "/api/branches").get().build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "branches");
            JsonNode arr = mapper.readTree(body(resp));
            List<BranchRef> out = new ArrayList<>();
            if (arr.isArray()) for (JsonNode n : arr)
                out.add(new BranchRef(n.path("id").asLong(), n.path("branchName").asText()));
            return out;
        }
    }

    public List<CurrencyRef> currencies() throws Exception {
        Request req = apiClient.authenticatedRequest()
                .url(apiClient.getBaseUrl() + "/api/currencies").get().build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "currencies");
            JsonNode arr = mapper.readTree(body(resp));
            List<CurrencyRef> out = new ArrayList<>();
            if (arr.isArray()) for (JsonNode n : arr)
                out.add(new CurrencyRef(n.path("currencyCode").asText(), n.path("currencyName").asText()));
            return out;
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

    private Account parse(JsonNode n) {
        Account a = new Account();
        a.setId(n.hasNonNull("id") ? n.get("id").asLong() : null);
        a.setAccountNumber(n.path("accountNumber").asText(null));
        a.setCustomerId(n.hasNonNull("customerId") ? n.get("customerId").asLong() : null);
        a.setCustomerName(n.path("customerName").asText(null));
        a.setAccountTypeCode(n.path("accountTypeCode").asText(null));
        a.setAccountTypeName(n.path("accountTypeName").asText(null));
        a.setBranchId(n.hasNonNull("branchId") ? n.get("branchId").asLong() : null);
        a.setBranchName(n.path("branchName").asText(null));
        a.setCurrencyCode(n.path("currencyCode").asText(null));
        a.setAccountName(n.path("accountName").asText(null));
        if (n.hasNonNull("balance")) a.setBalance(new BigDecimal(n.get("balance").asText()));
        if (n.hasNonNull("availableBalance")) a.setAvailableBalance(new BigDecimal(n.get("availableBalance").asText()));
        a.setStatus(n.path("status").asText(null));
        a.setOpeningDate(n.path("openingDate").asText(null));
        return a;
    }
}