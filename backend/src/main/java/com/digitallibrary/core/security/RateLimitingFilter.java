package com.digitallibrary.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final int MAX_LOGIN_ATTEMPTS_PER_MINUTE = 15;
    private static final int MAX_REGISTER_ATTEMPTS_PER_MINUTE = 10;
    private static final long WINDOW_MS = 60_000L; // 1 minute

    private final Map<String, Deque<Long>> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> registerAttempts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equalsIgnoreCase(method)) {
            String clientIp = extractClientIp(request);
            long now = System.currentTimeMillis();

            if (path.endsWith("/api/v1/auth/login")) {
                if (isRateLimited(loginAttempts, clientIp, MAX_LOGIN_ATTEMPTS_PER_MINUTE, now)) {
                    log.warn("Rate limit exceeded for login attempts from IP: {}", clientIp);
                    rejectRequest(response, "Too many login attempts. Please wait 1 minute before trying again.");
                    return;
                }
            } else if (path.endsWith("/api/v1/auth/register")) {
                if (isRateLimited(registerAttempts, clientIp, MAX_REGISTER_ATTEMPTS_PER_MINUTE, now)) {
                    log.warn("Rate limit exceeded for registration attempts from IP: {}", clientIp);
                    rejectRequest(response, "Too many registration attempts. Please wait 1 minute before trying again.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private synchronized boolean isRateLimited(Map<String, Deque<Long>> map, String ip, int maxRequests, long now) {
        Deque<Long> timestamps = map.computeIfAbsent(ip, k -> new ArrayDeque<>());

        // Evict expired timestamps outside the rolling window
        while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > WINDOW_MS) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxRequests) {
            return true;
        }

        timestamps.addLast(now);
        return false;
    }

    private void rejectRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> errorBody = Map.of(
                "success", false,
                "message", message,
                "data", null,
                "timestamp", Instant.now().toString()
        );
        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }
}
