package com.ctf.filter;

import com.ctf.util.JsonUtil;
import com.ctf.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebFilter("/api/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        
        
        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            JsonUtil.sendJson(resp, 401, Map.of("error", "Token manquant"));
            return;
        }

        try {
            String token = authHeader.substring(7);
            var decoded = JwtUtil.validateToken(token);
            // Injecter userId et role dans la requête pour les Servlets
            req.setAttribute("userId", decoded.getSubject());
            req.setAttribute("role", decoded.getClaim("role").asString());
            chain.doFilter(request, response);
        } catch (Exception e) {
            JsonUtil.sendJson(resp, 401, Map.of("error", "Token invalide ou expiré"));
        }
    }
}