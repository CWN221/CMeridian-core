package com.bank.desktop.util;

import com.bank.desktop.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class SessionManager {

    private static final String SESSION_FILE = ".banking_session";
    private static final String CREDENTIALS_FILE = ".banking_credentials";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            saveSession(user);
        }
    }

    public static void clearSession() {
        currentUser = null;
        try {
            Files.deleteIfExists(Paths.get(SESSION_FILE));
        } catch (IOException e) {
            log.warn("Failed to delete session file: {}", e.getMessage());
        }
    }

    private static void saveSession(User user) {
        try {
            objectMapper.writeValue(new File(SESSION_FILE), user);
        } catch (IOException e) {
            log.error("Failed to save session: {}", e.getMessage());
        }
    }

    public static User loadSession() {
        try {
            File file = new File(SESSION_FILE);
            if (file.exists()) {
                return objectMapper.readValue(file, User.class);
            }
        } catch (IOException e) {
            log.error("Failed to load session: {}", e.getMessage());
        }
        return null;
    }

    public static void saveCredentials(User user) {
        try {
            objectMapper.writeValue(new File(CREDENTIALS_FILE), user);
        } catch (IOException e) {
            log.error("Failed to save credentials: {}", e.getMessage());
        }
    }

    public static User getSavedCredentials() {
        try {
            File file = new File(CREDENTIALS_FILE);
            if (file.exists()) {
                return objectMapper.readValue(file, User.class);
            }
        } catch (IOException e) {
            log.error("Failed to load credentials: {}", e.getMessage());
        }
        return null;
    }

    public static void clearSavedCredentials() {
        try {
            Files.deleteIfExists(Paths.get(CREDENTIALS_FILE));
        } catch (IOException e) {
            log.warn("Failed to delete credentials file: {}", e.getMessage());
        }
    }

    public static boolean isSessionValid() {
        User user = loadSession();
        if (user != null && user.getAccessToken() != null) {
            currentUser = user;
            return true;
        }
        return false;
    }
}