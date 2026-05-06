package com.ctf.servlet;

import com.ctf.model.User;
import com.ctf.service.UserService;
import com.ctf.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/api/users/*", "/api/admin/*"})
public class UserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private UserService userService = new UserService();

    // Classe interne pour éviter le problème LocalDateTime avec Gson
    static class UserResponse {
        String id;
        String username;
        String email;
        String role;
        boolean isActive;
        String createdAt;
        String teamId;

        UserResponse(User user) {
            this.id        = user.getId().toString();
            this.username  = user.getUsername();
            this.email     = user.getEmail();
            this.role      = user.getRole().name();
            this.isActive  = user.isActive();
            this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
            this.teamId    = user.getTeamId() != null ? user.getTeamId().toString() : null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role   = (String) req.getAttribute("role");
        String userId = (String) req.getAttribute("userId");
        String path   = req.getPathInfo();

        try {
            if (path == null) {
                JsonUtil.sendJson(resp, 404, Map.of("error", "Route introuvable"));
                return;
            }

            if ("/me".equals(path)) {
                User user = userService.getProfile(UUID.fromString(userId));
                JsonUtil.sendJson(resp, 200, new UserResponse(user));

            } else if ("/users".equals(path) || "/".equals(path)) {
                if (!"ADMIN".equals(role)) {
                    JsonUtil.sendJson(resp, 403, Map.of("error", "Accès refusé"));
                    return;
                }
                List<UserResponse> list = userService.getAllUsers()
                        .stream()
                        .map(UserResponse::new)
                        .collect(Collectors.toList());
                JsonUtil.sendJson(resp, 200, list);

            } else {
                JsonUtil.sendJson(resp, 404, Map.of("error", "Route introuvable"));
            }

        } catch (RuntimeException e) {
            JsonUtil.sendJson(resp, 400, Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userId = (String) req.getAttribute("userId");
        String path   = req.getPathInfo();

        try {
            if ("/me".equals(path)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = JsonUtil.parseBody(req, Map.class);
                User updated = userService.updateProfile(
                        UUID.fromString(userId),
                        (String) body.get("username"),
                        (String) body.get("email")
                );
                JsonUtil.sendJson(resp, 200, new UserResponse(updated));
            } else {
                JsonUtil.sendJson(resp, 404, Map.of("error", "Route introuvable"));
            }
        } catch (RuntimeException e) {
            JsonUtil.sendJson(resp, 400, Map.of("error", e.getMessage()));
        }
    }
}