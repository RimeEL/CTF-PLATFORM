package com.ctf.filter;

import com.ctf.util.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebFilter("/api/admin/disabled")
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Récupérer le role injecté par AuthFilter
        String role = (String) req.getAttribute("role");

        if (role == null || !"ADMIN".equals(role)) {
            JsonUtil.sendJson(resp, 403, Map.of("error", "Accès refusé — ADMIN uniquement"));
            return;
        }

        // Role ADMIN confirmé → laisser passer
        chain.doFilter(request, response);
    }
}