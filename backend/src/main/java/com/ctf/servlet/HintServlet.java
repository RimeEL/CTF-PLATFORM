package com.ctf.servlet;

import com.ctf.model.Hint;
import com.ctf.service.HintService;
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
 * REST API Servlet for Hint resources.
 * Expose uniquement GET (lecture seule) selon les specs du projet.
 *
 * Routes:
 *   GET /api/hints                        → tous les hints
 *   GET /api/hints/{id}                   → un seul hint
 *   GET /api/hints?challengeId={uuid}     → hints d'un challenge
 */
@WebServlet("/api/hints/*")
public class HintServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private HintService service;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        service = new HintService();
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

    /** Extrait l'id depuis /api/hints/{id} → retourne null si absent */
    private String extractId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) return null;
        return pathInfo.substring(1);
    }

    // ── GET ───────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id             = extractId(req);
        String challengeId    = req.getParameter("challengeId");

        try {
            if (id != null) {
                // GET /api/hints/{id} → un seul hint
                Hint hint = service.getHintById(UUID.fromString(id));
                sendJson(resp, 200, hint);

            } else if (challengeId != null) {
                // GET /api/hints?challengeId={uuid} → hints d'un challenge
                List<Hint> hints = service.getHintsByChallenge(UUID.fromString(challengeId));
                sendJson(resp, 200, hints);

            } else {
                // GET /api/hints → tous les hints
                List<Hint> hints = service.getAllHints();
                sendJson(resp, 200, hints);
            }

        } catch (IllegalArgumentException e) {
            sendJson(resp, 404, new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            sendJson(resp, 500, new ErrorResponse("Erreur interne : " + e.getMessage()));
        }
    }

    // ── POST / PUT / DELETE → 405 Method Not Allowed ──────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendJson(resp, 405, new ErrorResponse("Méthode non autorisée. Les hints sont en lecture seule."));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendJson(resp, 405, new ErrorResponse("Méthode non autorisée. Les hints sont en lecture seule."));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendJson(resp, 405, new ErrorResponse("Méthode non autorisée. Les hints sont en lecture seule."));
    }

    // ── Inner response wrapper ────────────────────────────────────

    @SuppressWarnings("unused")
    private static class ErrorResponse {
        String error;
        ErrorResponse(String msg) { this.error = msg; }
    }
}
