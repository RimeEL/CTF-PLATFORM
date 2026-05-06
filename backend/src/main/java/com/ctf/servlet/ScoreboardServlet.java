package com.ctf.servlet;
import com.ctf.dao.ISolveDAO;
import com.ctf.dao.IDynamicScoringDAO;
import com.ctf.dao.SolveDAOImp;
import com.ctf.dao.DynamicScoringImp;
import com.ctf.model.Solve;
import com.ctf.util.DBConnection;
import com.ctf.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/api/scoreboard")
public class ScoreboardServlet extends HttpServlet {

    private final ISolveDAO solveDAO               = new SolveDAOImp();
    private final IDynamicScoringDAO dynamicDAO    = new DynamicScoringImp();

    /**
     * GET /api/scoreboard
     *   - Sans paramètre   → classement individuel (tous les users)
     *   - ?type=team       → classement par équipe
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String type = req.getParameter("type");

        try {
            if ("team".equalsIgnoreCase(type)) {
                List<TeamScore> scoreboard = buildTeamScoreboard();
                JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, scoreboard);
            } else {
                List<UserScore> scoreboard = buildUserScoreboard();
                JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, scoreboard);
            }
        } catch (Exception e) {
            JsonUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    new ErrorResponse("Erreur interne : " + e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // Classement individuel
    // -------------------------------------------------------------------------
    private List<UserScore> buildUserScoreboard() throws SQLException {
        // Récupère tous les solves groupés par user avec le total des points
        String sql = "SELECT u.id, u.username, COALESCE(SUM(s.awarded_points), 0) AS total_points " +
                     "FROM \"user\" u " +
                     "LEFT JOIN solve s ON s.user_id = u.id " +
                     "WHERE u.is_active = TRUE " +
                     "GROUP BY u.id, u.username " +
                     "ORDER BY total_points DESC";

        List<UserScore> result = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int rank = 1;
            while (rs.next()) {
                UserScore entry = new UserScore();
                entry.rank        = rank++;
                entry.userId      = rs.getString("id");
                entry.username    = rs.getString("username");
                entry.totalPoints = rs.getInt("total_points");
                result.add(entry);
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Classement par équipe
    // -------------------------------------------------------------------------
    private List<TeamScore> buildTeamScoreboard() throws SQLException {
        // Somme des points de tous les membres d'une équipe
        String sql = "SELECT t.id, t.name, COALESCE(SUM(s.awarded_points), 0) AS total_points " +
                     "FROM team t " +
                     "JOIN \"user\" u ON u.team_id = t.id " +
                     "LEFT JOIN solve s ON s.user_id = u.id " +
                     "WHERE u.is_active = TRUE " +
                     "GROUP BY t.id, t.name " +
                     "ORDER BY total_points DESC";

        List<TeamScore> result = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int rank = 1;
            while (rs.next()) {
                TeamScore entry = new TeamScore();
                entry.rank        = rank++;
                entry.teamId      = rs.getString("id");
                entry.teamName    = rs.getString("name");
                entry.totalPoints = rs.getInt("total_points");
                result.add(entry);
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // DTOs de réponse JSON
    // -------------------------------------------------------------------------
    private static class UserScore {
        public int    rank;
        public String userId;
        public String username;
        public int    totalPoints;
    }

    private static class TeamScore {
        public int    rank;
        public String teamId;
        public String teamName;
        public int    totalPoints;
    }

    private static class ErrorResponse {
        public final String error;
        ErrorResponse(String error) { this.error = error; }
    }
}

