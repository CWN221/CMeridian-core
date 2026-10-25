package com.bank.system.repository;

import com.bank.system.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findAllByOrderByGeneratedOnDesc(Pageable pageable);

    List<Report> findTop20ByOrderByGeneratedOnDesc();

    List<Report> findByReportType(String reportType);
}