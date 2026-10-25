package com.bank.system.controller;

import com.bank.system.dto.BranchDTO;
import com.bank.system.entity.Branch;
import com.bank.system.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchRepository branchRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('BRANCH_VIEW')")
    public ResponseEntity<List<BranchDTO>> list() {
        List<BranchDTO> list = branchRepository.findAllActiveOrdered().stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    private BranchDTO toDTO(Branch b) {
        return BranchDTO.builder()
                .id(b.getId())
                .branchCode(b.getBranchCode())
                .branchName(b.getBranchName())
                .city(b.getCity())
                .country(b.getCountry())
                .phone(b.getPhone())
                .email(b.getEmail())
                .isActive(b.getIsActive())
                .build();
    }
}