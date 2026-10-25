package com.bank.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRecord {

    private Long id;
    private String reportType;
    private String periodStart;
    private String periodEnd;
    private String periodLabel;
    private String generatedOn;
    private String generatedBy;
    private Integer rowCount;
    @Builder.Default
    private List<Map<String, Object>> rows = new ArrayList<>();
    @Builder.Default
    private Map<String, Object> summary = new LinkedHashMap<>();

    public String getDisplayName() {
        if (reportType == null) return "";
        return switch (reportType) {
            case "DAILY_TRANSACTIONS" -> "Daily Transaction Report";
            case "CUSTOMER_PORTFOLIO" -> "Customer Portfolio Report";
            case "ACCOUNT_BALANCES" -> "Account Balance Summary";
            case "LOAN_PORTFOLIO" -> "Loan Portfolio Report";
            default -> reportType;
        };
    }
}