package com.bank.desktop.service;

import com.bank.desktop.model.ReportRecord;
import com.bank.desktop.util.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ReportService {

    private final ApiClient apiClient = ApiClient.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public ReportRecord generate(String type, LocalDate start, LocalDate end) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reports/generate";
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reportType", type);
        payload.put("startDate", start.toString());
        payload.put("endDate", end.toString());
        Request req = apiClient.authenticatedRequest().url(url)
                .post(RequestBody.create(mapper.writeValueAsString(payload),
                        MediaType.parse("application/json"))).build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "generate report");
            return parse(mapper.readTree(body(resp)));
        }
    }

    public List<ReportRecord> history(int page, int size) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reports?page=" + page + "&size=" + size;
        Request req = apiClient.authenticatedRequest().url(url).get().build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            check(resp, "list reports");
            JsonNode content = mapper.readTree(body(resp)).get("content");
            List<ReportRecord> out = new ArrayList<>();
            if (content != null && content.isArray()) for (JsonNode n : content) out.add(parse(n));
            return out;
        }
    }

    public void delete(Long id) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reports/" + id;
        Request req = apiClient.authenticatedRequest().url(url).delete().build();
        try (Response resp = apiClient.getClient().newCall(req).execute()) {
            if (resp.code() == 204) return;
            check(resp, "delete report");
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

    private ReportRecord parse(JsonNode n) {
        ReportRecord r = new ReportRecord();
        r.setId(n.hasNonNull("id") ? n.get("id").asLong() : null);
        r.setReportType(n.path("reportType").asText(null));
        r.setPeriodStart(n.path("periodStart").asText(null));
        r.setPeriodEnd(n.path("periodEnd").asText(null));
        r.setPeriodLabel(n.path("periodLabel").asText(null));
        r.setGeneratedOn(n.path("generatedOn").asText(null));
        r.setGeneratedBy(n.path("generatedBy").asText(null));
        r.setRowCount(n.hasNonNull("rowCount") ? n.get("rowCount").asInt() : 0);

        List<Map<String, Object>> rows = new ArrayList<>();
        JsonNode rowsNode = n.get("rows");
        if (rowsNode != null && rowsNode.isArray()) {
            for (JsonNode row : rowsNode) {
                Map<String, Object> map = new LinkedHashMap<>();
                row.fields().forEachRemaining(e -> map.put(e.getKey(),
                        e.getValue().isNull() ? null : e.getValue().asText()));
                rows.add(map);
            }
        }
        r.setRows(rows);

        Map<String, Object> summary = new LinkedHashMap<>();
        JsonNode summaryNode = n.get("summary");
        if (summaryNode != null && summaryNode.isObject()) {
            summaryNode.fields().forEachRemaining(e -> summary.put(e.getKey(),
                    e.getValue().isNull() ? null : e.getValue().asText()));
        }
        r.setSummary(summary);
        return r;
    }
}