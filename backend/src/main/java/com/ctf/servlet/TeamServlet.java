package com.ctf.servlet;

import com.ctf.model.Team;
import com.ctf.service.TeamService;
import com.ctf.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/teams/*")
public class TeamServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TeamService teamService = new TeamService();

    // Classe interne pour éviter problème LocalDateTime
    static class TeamResponse {
        String id;
        String name;
        String createdAt;
        String competitionId;

        TeamResponse(Team team) {
            this.id            = team.getId().toString();
            this.name          = team.getName();
            this.createdAt     = team.getCreatedAt() != null ? team.getCreatedAt().toString() : null;
            this.competitionId = team.getCompetitionId() != null ? team.getCompetitionId().toString() : null;
        }
    }

    // GET /api/teams/{id} → détails d'une équipe
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();

        try {
            if (path == null || "/".equals(path)) {
                JsonUtil.sendJson(resp, 400, Map.of("error", "ID équipe requis"));
                return;
            }
            UUID teamId = UUID.fromString(path.substring(1));
            Team team = teamService.getTeam(teamId);
            JsonUtil.sendJson(resp, 200, new TeamResponse(team));

        } catch (IllegalArgumentException e) {
            JsonUtil.sendJson(resp, 400, Map.of("error", "UUID invalide"));
        } catch (RuntimeException e) {
            JsonUtil.sendJson(resp, 400, Map.of("error", e.getMessage()));
        }
    }

    // POST /api/teams        → créer une équipe
    // POST /api/teams/join   → rejoindre une équipe
    // POST /api/teams/leave  → quitter une équipe
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path   = req.getPathInfo();
        String userId = (String) req.getAttribute("userId");

        try {
            if ("/join".equals(path)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = JsonUtil.parseBody(req, Map.class);
                teamService.joinTeam(
                        UUID.fromString(userId),
                        UUID.fromString((String) body.get("teamId"))
                );
                JsonUtil.sendJson(resp, 200, Map.of("message", "Équipe rejointe avec succès"));

            } else if ("/leave".equals(path)) {
                teamService.leaveTeam(UUID.fromString(userId));
                JsonUtil.sendJson(resp, 200, Map.of("message", "Équipe quittée avec succès"));

            } else {
                // Créer une équipe
                @SuppressWarnings("unchecked")
                Map<String, Object> body = JsonUtil.parseBody(req, Map.class);
                String name          = (String) body.get("name");
                String competitionId = (String) body.get("competitionId");

                Team team = teamService.createTeam(
                        name,
                        competitionId != null ? UUID.fromString(competitionId) : null
                );
                JsonUtil.sendJson(resp, 201, new TeamResponse(team));
            }

        } catch (RuntimeException e) {
            JsonUtil.sendJson(resp, 400, Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/teams/{id} → modifier une équipe (ADMIN)
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        String role = (String) req.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            JsonUtil.sendJson(resp, 403, Map.of("error", "Accès refusé"));
            return;
        }

        try {
            UUID teamId = UUID.fromString(path.substring(1));
            @SuppressWarnings("unchecked")
            Map<String, Object> body = JsonUtil.parseBody(req, Map.class);
            Team team = teamService.updateTeam(teamId, (String) body.get("name"));
            JsonUtil.sendJson(resp, 200, new TeamResponse(team));

        } catch (RuntimeException e) {
            JsonUtil.sendJson(resp, 400, Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/teams/{id} → supprimer une équipe (ADMIN)
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        String role = (String) req.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            JsonUtil.sendJson(resp, 403, Map.of("error", "Accès refusé"));
            return;
        }

        try {
            UUID teamId = UUID.fromString(path.substring(1));
            teamService.deleteTeam(teamId);
            JsonUtil.sendJson(resp, 200, Map.of("message", "Équipe supprimée"));

        } catch (RuntimeException e) {
            JsonUtil.sendJson(resp, 400, Map.of("error", e.getMessage()));
        }
    }
}