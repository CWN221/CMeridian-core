package com.bank.system.service;

import com.bank.system.dto.AuthenticationRequest;
import com.bank.system.dto.AuthenticationResponse;
import com.bank.system.dto.ChangePasswordRequest;
import com.bank.system.dto.RefreshTokenRequest;
import com.bank.system.entity.LoginHistory;
import com.bank.system.entity.User;
import com.bank.system.exception.BusinessException;
import com.bank.system.exception.ResourceNotFoundException;
import com.bank.system.repository.LoginHistoryRepository;
import com.bank.system.repository.UserRepository;
import com.bank.system.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Value("${app.security.account-lock.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.account-lock.lock-duration:900}")
    private int lockDurationSeconds;

    @Transactional
    public AuthenticationResponse login(AuthenticationRequest request, HttpServletRequest httpRequest) {
        log.info("Login attempt for user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Check if account is locked
        if (user.getIsAccountLocked()) {
            if (user.getAccountLockedUntil() != null && 
                user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                throw new BusinessException("Account is locked. Please try again after " + 
                        user.getAccountLockedUntil());
            } else {
                // Unlock account if lock duration has passed
                user.setIsAccountLocked(false);
                user.setAccountLockedUntil(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Reset failed attempts on successful login
            user.setFailedLoginAttempts(0);
            user.setIsAccountLocked(false);
            user.setAccountLockedUntil(null);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(request.getUsername());

            // Log successful login
            logLoginHistory(user, true, null, httpRequest);

            // Audit log
            auditService.logAction("LOGIN_SUCCESS", "USER", user.getId().toString(), 
                    "User logged in successfully", httpRequest);

            // Build response
            return buildAuthenticationResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            // Increment failed attempts
            handleFailedLogin(user, httpRequest);
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new BusinessException("Invalid refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(request.getRefreshToken());
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken = tokenProvider.generateToken(username);
        String newRefreshToken = tokenProvider.generateRefreshToken(username);

        return buildAuthenticationResponse(user, newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Logout for user: {}", username);

        // Log logout
        loginHistoryRepository.findLatestByUsername(username)
                .ifPresent(history -> {
                    history.setLogoutTime(LocalDateTime.now());
                    history.setStatus("LOGOUT");
                    loginHistoryRepository.save(history);
                });

        // Audit log
        auditService.logAction("LOGOUT", "USER", null, "User logged out", request);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String username) {
        log.info("Changing password for user: {}", username);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }

        // Validate new password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", username);
        auditService.logAction("PASSWORD_CHANGE", "USER", user.getId().toString(),"Password changed successfully", null);
    }

    private void handleFailedLogin(User user, HttpServletRequest request) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            // Lock the account
            user.setIsAccountLocked(true);
            user.setAccountLockedUntil(LocalDateTime.now().plusSeconds(lockDurationSeconds));
            log.warn("Account locked for user: {} due to {} failed attempts", user.getUsername(), attempts);
            
            // Audit log for lock
            auditService.logAction("ACCOUNT_LOCKED", "USER", user.getId().toString(), "Account locked due to multiple failed login attempts", request);
        }

        userRepository.save(user);

        // Log failed login
        logLoginHistory(user, false, "Invalid credentials", request);
    }

    private void logLoginHistory(User user, boolean success, String failureReason, HttpServletRequest request) {
        LoginHistory loginHistory = LoginHistory.builder()
                .user(user)
                .username(user.getUsername())
                .loginTime(LocalDateTime.now())
                .ipAddress(getClientIP(request))
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .sessionId(request != null ? request.getSession().getId() : null)
                .status(success ? "SUCCESS" : "FAILURE")
                .failureReason(failureReason)
                .build();
        loginHistoryRepository.save(loginHistory);
    }

    private AuthenticationResponse buildAuthenticationResponse(User user, String accessToken, String refreshToken) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getPermissionName())
                .collect(Collectors.toSet());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getTokenRemainingTime(accessToken))
                .username(user.getUsername())
                .fullName(user.getEmployee().getFullName())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        if (request == null) return null;
        
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