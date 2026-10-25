package com.bank.desktop.service;

import com.bank.desktop.model.User;
import com.bank.desktop.util.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class AuthService {

    private final ApiClient apiClient = ApiClient.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public User login(String username, String password) throws Exception {
        log.info("Attempting login for user: {}", username);
        
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
            .url(apiClient.getBaseUrl() + "/api/auth/login")
            .post(body)
            .build();
        
        try (Response response = apiClient.getClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("Login failed: {}", errorBody);
                throw new Exception("Invalid username or password");
            }
            
            String responseBody = response.body() != null ? response.body().string() : "";
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // Parse user data
            User user = new User();
            user.setUsername(username);
            user.setFullName(jsonNode.get("fullName").asText());
            user.setAccessToken(jsonNode.get("accessToken").asText());
            user.setRefreshToken(jsonNode.get("refreshToken").asText());
            
            // Parse roles
            Set<String> roles = new HashSet<>();
            JsonNode rolesNode = jsonNode.get("roles");
            if (rolesNode != null && rolesNode.isArray()) {
                rolesNode.forEach(role -> roles.add(role.asText()));
            }
            user.setRoles(roles);
            
            // Parse permissions
            Set<String> permissions = new HashSet<>();
            JsonNode permissionsNode = jsonNode.get("permissions");
            if (permissionsNode != null && permissionsNode.isArray()) {
                permissionsNode.forEach(perm -> permissions.add(perm.asText()));
            }
            user.setPermissions(permissions);
            
            // Store token in ApiClient
            apiClient.setAccessToken(user.getAccessToken());
            
            log.info("Login successful for user: {}", username);
            return user;
        }
    }

    public boolean logout() {
        log.info("Logging out");
        apiClient.setAccessToken(null);
        return true;
    }

    public boolean refreshToken(String refreshToken) {
        try {
            String json = String.format("{\"refreshToken\":\"%s\"}", refreshToken);
            
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            
            Request request = new Request.Builder()
                .url(apiClient.getBaseUrl() + "/api/auth/refresh")
                .post(body)
                .build();
            
            try (Response response = apiClient.getClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Token refresh failed");
                    return false;
                }
                
                String responseBody = response.body() != null ? response.body().string() : "";
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String newToken = jsonNode.get("accessToken").asText();
                apiClient.setAccessToken(newToken);
                return true;
            }
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage());
            return false;
        }
    }
}