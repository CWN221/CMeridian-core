package com.bank.system.controller;

import com.bank.system.dto.ReportDTO;
import com.bank.system.dto.ReportRequestDTO;
import com.bank.system.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<ReportDTO> generate(@Valid @RequestBody ReportRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.generate(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<Page<ReportDTO>> history(
            @PageableDefault(size = 20, sort = "generatedOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(reportService.history(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ReportDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.get(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_SCHEDULE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}