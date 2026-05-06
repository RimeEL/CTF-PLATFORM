package com.ctf.servlet;

import com.ctf.model.Competition;
import com.ctf.service.CompetitionService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

/**
 * REST API Servlet for Competition resources.
 *
 * Routes:
 *   GET    /api/competitions        → liste toutes les compétitions
 *   GET    /api/competitions/{id}   → une seule compétition
 *   POST   /api/competitions        → créer une compétition
 *   PUT    /api/competitions/{id}   → modifier une compétition
 *   DELETE /api/competitions/{id}   → supprimer une compétition
 */
@WebServlet("/api/competitions/*")
public class CompetitionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private CompetitionService service;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        service = new CompetitionService();
        gson = new GsonBuilder()
        	    .serializeNulls()
        	    .registerTypeAdapter(java.time.LocalDateTime.class,
        	        (com.google.gson.JsonSerializer<java.time.LocalDateTime>)
        	        (src, type, ctx) -> new com.google.gson.JsonPrimitive(src.toString()))
        	    .registerTypeAdapter(java.time.LocalDateTime.class,
        	        (com.google.gson.JsonDeserializer<java.time.LocalDateTime>)
        	        (json, type, ctx) -> java.time.LocalDateTime.parse(json.getAsString()))
        	    .create();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void sendJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(body));
        out.flush();
    }

    /** Extrait l'id depuis /api/competitions/{id} → retourne null si absent */
    private String extractId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) return null;
        return pathInfo.substring(1);
    }

    // ── GET ───────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = extractId(req);
        try {
            if (id == null) {
                // GET /api/competitions → liste toutes
                List<Competition> competitions = service.getAllCompetitions();
                sendJson(resp, 200, competitions);
            } else {
                // GET /api/competitions/{id} → une seule
                Competition competition = service.getCompetitionById(UUID.fromString(id));
                sendJson(resp, 200, competition);
            }
        } catch (IllegalArgumentException e) {
            sendJson(resp, 404, new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            sendJson(resp, 500, new ErrorResponse("Erreur interne : " + e.getMessage()));
        }
    }

    // ── POST ──────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Competition body    = gson.fromJson(req.getReader(), Competition.class);
            Competition created = service.createCompetition(body);
            sendJson(resp, 201, created);
        } catch (IllegalArgumentException e) {
            sendJson(resp, 400, new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            sendJson(resp, 500, new ErrorResponse("Erreur interne : " + e.getMessage()));
        }
    }

    // ── PUT ───────────────────────────────────────────────────────

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = extractId(req);
        if (id == null) {
            sendJson(resp, 400, new ErrorResponse("L'id de la compétition est manquant dans l'URL."));
            return;
        }
        try {
            Competition body    = gson.fromJson(req.getReader(), Competition.class);
            Competition updated = service.updateCompetition(UUID.fromString(id), body);
            sendJson(resp, 200, updated);
        } catch (IllegalArgumentException e) {
            sendJson(resp, 404, new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            sendJson(resp, 500, new ErrorResponse("Erreur interne : " + e.getMessage()));
        }
    }

    // ── DELETE ────────────────────────────────────────────────────

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = extractId(req);
        if (id == null) {
            sendJson(resp, 400, new ErrorResponse("L'id de la compétition est manquant dans l'URL."));
            return;
        }
        try {
            service.deleteCompetition(UUID.fromString(id));
            sendJson(resp, 200, new MessageResponse("Compétition supprimée avec succès."));
        } catch (IllegalArgumentException e) {
            sendJson(resp, 404, new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            sendJson(resp, 500, new ErrorResponse("Erreur interne : " + e.getMessage()));
        }
    }

    // ── Inner response wrappers ───────────────────────────────────

    @SuppressWarnings("unused")
    private static class ErrorResponse {
        String error;
        ErrorResponse(String msg) { this.error = msg; }
    }

    @SuppressWarnings("unused")
    private static class MessageResponse {
        String message;
        MessageResponse(String msg) { this.message = msg; }
    }
}
