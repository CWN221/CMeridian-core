package com.bank.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String accessToken;
    private String refreshToken;
    private Set<String> roles = new HashSet<>();
    private Set<String> permissions = new HashSet<>();
    
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
    
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
    
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
    
    public boolean isManager() {
        return hasRole("ROLE_MANAGER") || isAdmin();
    }
    
    public boolean isTeller() {
        return hasRole("ROLE_TELLER") || isManager();
    }
}