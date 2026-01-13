package com.fanshuaiko.backage.config;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class SimpleCorsFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_ORIGINS = new HashSet<>();

    static {
        ALLOWED_ORIGINS.add("http://localhost:8084");
        ALLOWED_ORIGINS.add("http://127.0.0.1:8084");
        ALLOWED_ORIGINS.add("http://localhost:8086");
        ALLOWED_ORIGINS.add("http://127.0.0.1:8086");
        ALLOWED_ORIGINS.add("http://localhost:8085");
        ALLOWED_ORIGINS.add("http://127.0.0.1:8085");
        ALLOWED_ORIGINS.add("http://localhost:8105");
        ALLOWED_ORIGINS.add("http://127.0.0.1:8105");
        ALLOWED_ORIGINS.add("http://localhost:8083");
        ALLOWED_ORIGINS.add("http://127.0.0.1:8083");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String origin = request.getHeader("Origin");
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            String reqHeaders = request.getHeader("Access-Control-Request-Headers");
            response.setHeader("Access-Control-Allow-Headers", reqHeaders != null ? reqHeaders : "*");
            response.setHeader("Access-Control-Max-Age", "3600");
        }

        // Preflight request
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
