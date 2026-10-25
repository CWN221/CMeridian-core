package com.bank.desktop.service;

import com.bank.desktop.model.Customer;
import com.bank.desktop.util.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CustomerService {

    private final ApiClient apiClient = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Customer> list(int page, int size) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/customers?page=" + page + "&size=" + size;
        Request request = apiClient.authenticatedRequest().url(url).get().build();

        try (Response response = apiClient.getClient().newCall(request).execute()) {
            checkResponse(response, "list customers");
            JsonNode body = readBody(response);
            JsonNode content = body.get("content");
            List<Customer> list = new ArrayList<>();
            if (content != null && content.isArray()) {
                for (JsonNode node : content) list.add(parse(node));
            }
            return list;
        }
    }

    public Customer getById(Long id) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/customers/" + id;
        Request request = apiClient.authenticatedRequest().url(url).get().build();

        try (Response response = apiClient.getClient().newCall(request).execute()) {
            checkResponse(response, "get customer");
            return parse(readBody(response));
        }
    }

    public Customer create(Customer customer) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/customers";
        String json = mapper.writeValueAsString(toRequestPayload(customer));
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = apiClient.authenticatedRequest().url(url).post(body).build();

        try (Response response = apiClient.getClient().newCall(request).execute()) {
            checkResponse(response, "create customer");
            return parse(readBody(response));
        }
    }

    public Customer update(Long id, Customer customer) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/customers/" + id;
        String json = mapper.writeValueAsString(toRequestPayload(customer));
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = apiClient.authenticatedRequest().url(url).put(body).build();

        try (Response response = apiClient.getClient().newCall(request).execute()) {
            checkResponse(response, "update customer");
            return parse(readBody(response));
        }
    }

    public void delete(Long id) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/customers/" + id;
        Request request = apiClient.authenticatedRequest().url(url).delete().build();

        try (Response response = apiClient.getClient().newCall(request).execute()) {
            if (response.code() == 204) return;
            checkResponse(response, "delete customer");
        }
    }

    public long countActive() throws Exception {
        String url = apiClient.getBaseUrl() + "/api/customers/count/active";
        Request request = apiClient.authenticatedRequest().url(url).get().build();

        try (Response response = apiClient.getClient().newCall(request).execute()) {
            checkResponse(response, "count customers");
            String body = response.body() != null ? response.body().string() : "0";
            return Long.parseLong(body.trim());
        }
    }

    private JsonNode readBody(Response response) throws Exception {
        String body = response.body() != null ? response.body().string() : "{}";
        return mapper.readTree(body);
    }

    private void checkResponse(Response response, String action) throws Exception {
        if (response.isSuccessful()) return;
        String body = response.body() != null ? response.body().string() : "";
        String message = extractErrorMessage(body);
        log.error("{} failed: {} {}", action, response.code(), body);
        throw new Exception(message != null ? message : action + " failed (" + response.code() + ")");
    }

    private String extractErrorMessage(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            JsonNode node = mapper.readTree(body);
            if (node.hasNonNull("message")) return node.get("message").asText();
            if (node.hasNonNull("error")) return node.get("error").asText();
        } catch (Exception ignored) {}
        return null;
    }

    private Customer parse(JsonNode node) {
        Customer c = new Customer();
        c.setId(node.hasNonNull("id") ? node.get("id").asLong() : null);
        c.setCustomerNumber(text(node, "customerNumber"));
        c.setFirstName(text(node, "firstName"));
        c.setLastName(text(node, "lastName"));
        c.setMiddleName(text(node, "middleName"));
        c.setNationalId(text(node, "nationalId"));
        c.setPassportNumber(text(node, "passportNumber"));
        c.setEmail(text(node, "email"));
        c.setPhone(text(node, "phone"));
        c.setAlternatePhone(text(node, "alternatePhone"));
        c.setAddressLine1(text(node, "addressLine1"));
        c.setAddressLine2(text(node, "addressLine2"));
        c.setCity(text(node, "city"));
        c.setState(text(node, "state"));
        c.setPostalCode(text(node, "postalCode"));
        c.setCountry(text(node, "country"));
        c.setOccupation(text(node, "occupation"));
        c.setEmployer(text(node, "employer"));
        c.setVerificationRemarks(text(node, "verificationRemarks"));
        c.setCreatedAt(text(node, "createdAt"));
        c.setUpdatedAt(text(node, "updatedAt"));
        c.setIsVerified(node.hasNonNull("isVerified") && node.get("isVerified").asBoolean());
        c.setIsActive(!node.hasNonNull("isActive") || node.get("isActive").asBoolean());
        if (node.hasNonNull("dateOfBirth")) {
            try { c.setDateOfBirth(LocalDate.parse(node.get("dateOfBirth").asText())); } catch (Exception ignored) {}
        }
        if (node.hasNonNull("annualIncome") && !node.get("annualIncome").isNull()) {
            try { c.setAnnualIncome(new BigDecimal(node.get("annualIncome").asText())); } catch (Exception ignored) {}
        }
        return c;
    }

    private String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private Map<String, Object> toRequestPayload(Customer c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("firstName", c.getFirstName());
        m.put("lastName", c.getLastName());
        m.put("middleName", c.getMiddleName());
        m.put("dateOfBirth", c.getDateOfBirth() != null ? c.getDateOfBirth().toString() : null);
        m.put("nationalId", c.getNationalId());
        m.put("passportNumber", c.getPassportNumber());
        m.put("email", c.getEmail());
        m.put("phone", c.getPhone());
        m.put("alternatePhone", c.getAlternatePhone());
        m.put("addressLine1", c.getAddressLine1());
        m.put("addressLine2", c.getAddressLine2());
        m.put("city", c.getCity());
        m.put("state", c.getState());
        m.put("postalCode", c.getPostalCode());
        m.put("country", c.getCountry());
        m.put("occupation", c.getOccupation());
        m.put("employer", c.getEmployer());
        m.put("annualIncome", c.getAnnualIncome());
        return m;
    }
}