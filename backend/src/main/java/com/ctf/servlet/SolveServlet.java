package com.ctf.servlet;

import com.ctf.dao.ISolveDAO;
import com.ctf.dao.SolveDAOImp;
import com.ctf.model.Solve;
import com.ctf.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/api/solves")
public class SolveServlet extends HttpServlet {

    private final ISolveDAO solveDAO = new SolveDAOImp();

    /**
     * GET /api/solves
     *   - Sans paramètre         → 400 Bad Request
     *   - ?userId=<uuid>         → liste des solves de l'utilisateur
     *   - ?challengeId=<uuid>    → liste des solves du challenge
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String userIdParam      = req.getParameter("userId");
        String challengeIdParam = req.getParameter("challengeId");

        try {
            if (userIdParam != null) {
                UUID userId = UUID.fromString(userIdParam);
                List<Solve> solves = solveDAO.findByUserId(userId);
                JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, solves);

            } else if (challengeIdParam != null) {
                UUID challengeId = UUID.fromString(challengeIdParam);
                List<Solve> solves = solveDAO.findByChallengeId(challengeId);
                JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, solves);

            } else {
                JsonUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                        new ErrorResponse("Paramètre 'userId' ou 'challengeId' requis."));
            }

        } catch (IllegalArgumentException e) {
            JsonUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    new ErrorResponse("UUID invalide : " + e.getMessage()));
        } catch (Exception e) {
            JsonUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorResponse("Erreur interne : " + e.getMessage()));
        }
    }

    // Classe interne pour les réponses d'erreur JSON
    private static class ErrorResponse {
        public final String error;
        ErrorResponse(String error) { this.error = error; }
    }
}

