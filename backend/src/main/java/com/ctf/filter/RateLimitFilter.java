package com.ctf.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebFilter("/api/submissions")
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String clientKey = extractKey(httpReq);
        long now = System.currentTimeMillis();

        requestLog.putIfAbsent(clientKey, new ArrayDeque<>());
        Deque<Long> timestamps = requestLog.get(clientKey);

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= MAX_REQUESTS) {
                httpRes.setStatus(429);
                httpRes.setContentType("application/json");
                httpRes.getWriter().write(
                    "{\"error\": \"Too many requests. Please wait before submitting again.\"}"
                );
                return;
            }
            timestamps.addLast(now);
        }

        chain.doFilter(request, response);
    }

    private String extractKey(HttpServletRequest req) {
        Object userId = req.getAttribute("userId");
        if (userId != null) return "user:" + userId.toString();
        return "ip:" + req.getRemoteAddr();
    }

    @Override public void init(FilterConfig config) {}
    @Override public void destroy() {}
}