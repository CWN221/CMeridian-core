package com.bank.system.security;

import com.bank.system.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator {

    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return hasPermission(authentication, targetDomainObject.toString(), permission.toString());
    }

    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, targetType + ":" + targetId, permission.toString());
    }

    private boolean hasPermission(Authentication authentication, String resource, String permission) {
        User user = (User) authentication.getPrincipal();
        
        // Admin has all permissions
        if (user.getRoles().stream().anyMatch(role -> role.getRoleName().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Check if user has the required permission
        return user.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(permission));
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(granted -> granted.getAuthority().equals("ROLE_ADMIN"));
    }

    public static boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        for (String role : roles) {
            if (auth.getAuthorities().stream().anyMatch(granted -> granted.getAuthority().equals(role))) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnyPermission(String... permissions) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        for (String permission : permissions) {
            if (auth.getAuthorities().stream().anyMatch(granted -> granted.getAuthority().equals(permission))) {
                return true;
            }
        }
        return false;
    }
}