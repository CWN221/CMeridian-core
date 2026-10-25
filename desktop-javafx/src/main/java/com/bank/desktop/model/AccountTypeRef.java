package com.bank.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTypeRef {
    private String code;
    private String name;
    @Override public String toString() { return name; }
}