package com.bank.system.security;

import com.bank.system.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${app.security.rate-limit.default-limit:100}")
    private int defaultLimit;

    @Value("${app.security.rate-limit.login-limit:10}")
    private int loginLimit;

    @Value("${app.security.rate-limit.time-window:60}")
    private int timeWindowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIP(request);
        String requestPath = request.getRequestURI();

        int limit = requestPath.equals("/api/auth/login") ? loginLimit : defaultLimit;
        String key = "rate_limit:" + clientIp + ":" + requestPath;

        final Long currentCount;
        try {
            currentCount = redisTemplate.opsForValue().increment(key);

            if (currentCount == null) {
                throw new IllegalStateException("Redis returned a null rate-limit counter");
            }

            if (currentCount == 1) {
                redisTemplate.expire(key, timeWindowSeconds, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("Rate limiting failed - failing open to avoid blocking requests: {}", e.getMessage());

            // FAIL-OPEN: let the request through if Redis is down.
            // (Comment out the next line and uncomment the block below if you prefer fail-closed.)
            filterChain.doFilter(request, response);
            return;

            /*
            // FAIL-CLOSED alternative:
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store");

            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(503)
                    .error("Service Unavailable")
                    .message("Unable to process the request at this time.")
                    .path(requestPath)
                    .build();

            objectMapper.writeValue(response.getWriter(), error);
            return;
            */
        }

        if (currentCount > limit) {
            log.warn("Rate limit exceeded for IP {} on path {} ({}/{})",
                    clientIp, requestPath, currentCount, limit);

            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", String.valueOf(timeWindowSeconds));
            response.setHeader("Cache-Control", "no-store");

            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(429)
                    .error("Too Many Requests")
                    .message("Rate limit exceeded. Try again later.")
                    .path(requestPath)
                    .build();

            objectMapper.writeValue(response.getWriter(), error);
            return;
        }

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining",
                String.valueOf(Math.max(0L, limit - currentCount)));

        filterChain.doFilter(request, response);
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