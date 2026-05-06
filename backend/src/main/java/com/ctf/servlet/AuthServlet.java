package com.ctf.servlet;

import com.ctf.service.AuthService;
import com.ctf.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;

@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo(); // /login ou /register

        try {
            if ("/login".equals(path)) {
            	@SuppressWarnings("unchecked")
            	Map<String, Object> body = JsonUtil.parseBody(req, Map.class);
                String token = authService.login(
                    (String) body.get("username"),
                    (String) body.get("password")
                );
                JsonUtil.sendJson(resp, 200, Map.of("token", token));

            } else if ("/register".equals(path)) {
            	@SuppressWarnings("unchecked")
            	Map<String, Object> body = JsonUtil.parseBody(req, Map.class);
                authService.register(
                    (String) body.get("username"),
                    (String) body.get("email"),
                    (String) body.get("password")
                );
                JsonUtil.sendJson(resp, 201, Map.of("message", "Compte créé avec succès"));

            } else {
                JsonUtil.sendJson(resp, 404, Map.of("error", "Route introuvable"));
            }

        } catch (RuntimeException e) {
            JsonUtil.sendJson(resp, 400, Map.of("error", e.getMessage()));
        }
    }
}