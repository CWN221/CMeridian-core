package com.bank.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchRef {
    private Long id;
    private String name;
    @Override public String toString() { return name; }
}