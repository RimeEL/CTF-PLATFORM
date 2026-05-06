package com.ctf.servlet;

import com.ctf.service.SubmissionService;
import com.ctf.service.SubmissionService.SubmitResult;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet("/api/submissions")
public class SubmissionServlet extends HttpServlet {

    private final SubmissionService submissionService = new SubmissionService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        // 1. Extraire userId depuis le filtre JWT
        Object userAttr = req.getAttribute("userId");
        if (userAttr == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }
        UUID userId = UUID.fromString(userAttr.toString());

        // 2. Parser le body JSON
        String body = req.getReader().lines().collect(Collectors.joining());
        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\": \"Invalid JSON body\"}");
            return;
        }

        if (!json.has("challengeId") || !json.has("flag")) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\": \"Missing challengeId or flag\"}");
            return;
        }

        UUID challengeId;
        String rawFlag;
        try {
            challengeId = UUID.fromString(json.getString("challengeId"));
            rawFlag = json.getString("flag").trim();
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\": \"Invalid challengeId format\"}");
            return;
        }

        // 3. Appeler le service
        SubmitResult result = submissionService.submit(userId, challengeId, rawFlag);

        // 4. Réponse
        JSONObject responseJson = new JSONObject();
        switch (result) {
            case CORRECT -> {
                resp.setStatus(200);
                responseJson.put("correct", true);
                responseJson.put("message", "Correct flag! Points awarded.");
            }
            case INCORRECT -> {
                resp.setStatus(200);
                responseJson.put("correct", false);
                responseJson.put("message", "Wrong flag. Try again.");
            }
            case ALREADY_SOLVED -> {
                resp.setStatus(409);
                responseJson.put("correct", false);
                responseJson.put("message", "You have already solved this challenge.");
            }
            case CHALLENGE_NOT_FOUND -> {
                resp.setStatus(404);
                responseJson.put("error", "Challenge not found.");
            }
        }

        resp.getWriter().write(responseJson.toString());
    }
}