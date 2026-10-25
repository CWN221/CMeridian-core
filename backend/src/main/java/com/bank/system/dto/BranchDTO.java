package com.bank.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {
    private Long id;
    private String branchCode;
    private String branchName;
    private String city;
    private String country;
    private String phone;
    private String email;
    private Boolean isActive;
}