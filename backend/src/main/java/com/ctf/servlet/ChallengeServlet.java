package com.ctf.servlet;

import com.ctf.model.Challenge;
import com.ctf.service.ChallengeService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

//✅ Les remplacer par jakarta
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
 * REST API Servlet for Challenge resources.
 *
 * Routes:
 *   GET    /api/challenges?competitionId={uuid}  → challenges d'une compétition
 *   GET    /api/challenges/{id}                  → un seul challenge
 *   POST   /api/challenges                       → créer un challenge
 *   PUT    /api/challenges/{id}                  → modifier un challenge
 *   DELETE /api/challenges/{id}                  → supprimer un challenge
 */
@WebServlet("/api/challenges/*")
public class ChallengeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ChallengeService service;
    private Gson gson;

    // ── Fix : throws ServletException ajouté ─────────────────────
    @Override
    public void init() throws ServletException {
        service = new ChallengeService();
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

    /** Extrait l'id depuis /api/challenges/{id} → retourne null si absent */
    private String extractId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo(); // ex: "/abc-123" ou null
        if (pathInfo == null || pathInfo.equals("/")) return null;
        return pathInfo.substring(1); // retire le '/' initial
    }

    // ── GET ───────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String id             = extractId(req);
        String competitionId  = req.getParameter("competitionId");

        try {
            if (id != null) {
                // GET /api/challenges/{id} → un seul challenge
                Challenge challenge = service.getChallengeById(UUID.fromString(id));
                sendJson(resp, 200, challenge);

            } else if (competitionId != null) {
                // GET /api/challenges?competitionId={uuid} → liste filtrée
                List<Challenge> challenges =
                    service.getChallengesByCompetition(UUID.fromString(competitionId));
                sendJson(resp, 200, challenges);

            } else {
                // Fix : competitionId obligatoire (findAll() absent de IChallengeDAO)
                sendJson(resp, 400, new ErrorResponse(
                    "Le paramètre 'competitionId' est requis. " +
                    "Exemple : /api/challenges?competitionId={uuid}"));
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
            Challenge body    = gson.fromJson(req.getReader(), Challenge.class);
            Challenge created = service.createChallenge(body);
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
            sendJson(resp, 400, new ErrorResponse("L'id du challenge est manquant dans l'URL."));
            return;
        }
        try {
            Challenge body    = gson.fromJson(req.getReader(), Challenge.class);
            Challenge updated = service.updateChallenge(UUID.fromString(id), body);
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
            sendJson(resp, 400, new ErrorResponse("L'id du challenge est manquant dans l'URL."));
            return;
        }
        try {
            service.deleteChallenge(UUID.fromString(id));
            sendJson(resp, 200, new MessageResponse("Challenge supprimé avec succès."));

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
