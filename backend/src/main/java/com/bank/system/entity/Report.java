package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "period_label", length = 100)
    private String periodLabel;

    @Column(name = "generated_on")
    private LocalDateTime generatedOn;

    @Column(name = "generated_by", length = 50)
    private String generatedBy;
    
    @Column(name = "created_by")
    private String createdBy;    

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "payload", columnDefinition = "text")
    private String payload;
}