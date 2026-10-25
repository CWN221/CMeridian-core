package com.bank.system.service;

import com.bank.system.entity.AuditLog;
import com.bank.system.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void logAction(String action, String resourceType, String resourceId, 
                          String message, HttpServletRequest request) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                auditLog.setUsername(auth.getName());
                // Try to get user ID from the principal
                if (auth.getPrincipal() instanceof com.bank.system.entity.User) {
                    com.bank.system.entity.User user = (com.bank.system.entity.User) auth.getPrincipal();
                    auditLog.setUserId(user.getId());
                }
            }

            // Set IP and user agent
            if (request != null) {
                auditLog.setIpAddress(getClientIP(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession().getId());
            }

            auditLog.setStatus("SUCCESS");
            auditLog.setNewValue(convertToJson(message));
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} - {}", action, message);
            
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    @Async
    public void logFailure(String action, String resourceType, String resourceId, 
                           String errorMessage, HttpServletRequest request) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setError(errorMessage);
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                auditLog.setUsername(auth.getName());
            }

            // Set IP and user agent
            if (request != null) {
                auditLog.setIpAddress(getClientIP(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession().getId());
            }

            auditLog.setStatus("FAILURE");
            
            auditLogRepository.save(auditLog);
            log.debug("Audit failure logged: {} - {}", action, errorMessage);
            
        } catch (Exception e) {
            log.error("Failed to save audit failure: {}", e.getMessage());
        }
    }

    @Async
    public void logDataChange(String action, String resourceType, String resourceId, 
                              Object oldValue, Object newValue, HttpServletRequest request) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                auditLog.setUsername(auth.getName());
                if (auth.getPrincipal() instanceof com.bank.system.entity.User) {
                    com.bank.system.entity.User user = (com.bank.system.entity.User) auth.getPrincipal();
                    auditLog.setUserId(user.getId());
                }
            }

            // Set IP and user agent
            if (request != null) {
                auditLog.setIpAddress(getClientIP(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession().getId());
            }

            auditLog.setOldValue(convertToJson(oldValue));
            auditLog.setNewValue(convertToJson(newValue));
            auditLog.setStatus("SUCCESS");
            
            auditLogRepository.save(auditLog);
            log.debug("Data change audit logged: {} - {}", action, resourceId);
            
        } catch (Exception e) {
            log.error("Failed to save data change audit: {}", e.getMessage());
        }
    }

    private String convertToJson(Object obj) {
        try {
            if (obj == null) return null;
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert object to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        return request.getRemoteAddr();
    }
}