package com.learnfirebase.ecommerce.bootstrap.web;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;
    private static final int AUTH_LIMIT = 20;
    private static final int ORDER_LIMIT = 60;

    private final Map<String, Window> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        boolean isAuth = path.startsWith("/api/auth");
        boolean isOrder = path.startsWith("/api/orders");
        if (isAuth || isOrder) {
            String key = request.getRemoteAddr() + "|" + (isAuth ? "auth" : "order");
            int limit = isAuth ? AUTH_LIMIT : ORDER_LIMIT;
            if (!allow(key, limit)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean allow(String key, int limit) {
        long now = Instant.now().toEpochMilli();
        Window window = buckets.computeIfAbsent(key, k -> new Window(now, 0));
        synchronized (window) {
            if (now - window.start >= WINDOW_MILLIS) {
                window.start = now;
                window.count = 0;
            }
            if (window.count >= limit) {
                return false;
            }
            window.count++;
            return true;
        }
    }

    private static class Window {
        long start;
        int count;

        Window(long start, int count) {
            this.start = start;
            this.count = count;
        }
    }
}
