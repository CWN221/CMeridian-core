package com.bank.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private Long id;
    private String reportType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String periodLabel;
    private LocalDateTime generatedOn;
    private String generatedBy;
    private Integer rowCount;

    private List<Map<String, Object>> rows;

    private Map<String, Object> summary;
}