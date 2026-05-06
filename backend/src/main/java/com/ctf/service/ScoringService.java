package com.ctf.service;

import com.ctf.dao.IDynamicScoringDAO;
import com.ctf.dao.ISolveDAO;
import com.ctf.dao.DynamicScoringImp;
import com.ctf.dao.SolveDAOImp;
import com.ctf.model.DynamicScoring;
import com.ctf.model.Solve;
import com.ctf.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ScoringService {

    private final ISolveDAO          solveDAO   = new SolveDAOImp();
    private final IDynamicScoringDAO dynamicDAO = new DynamicScoringImp();

    public int calculatePoints(UUID challengeId) {
        int initialPoints = getInitialPoints(challengeId);
        int minimumPoints = getMinimumPoints(challengeId);

        DynamicScoring ds = dynamicDAO.findByChallengeId(challengeId);
        double decayRate  = (ds != null) ? ds.getDecayRate() : 0.0;

        List<Solve> existingSolves = solveDAO.findByChallengeId(challengeId);
        int n = existingSolves.size();

        double raw   = initialPoints * Math.exp(-decayRate * n);
        int awarded  = (int) Math.round(raw);

        return Math.max(minimumPoints, awarded);
    }

    public Solve registerSolve(UUID userId, UUID challengeId) {
        if (solveDAO.existsByUserAndChallenge(userId, challengeId)) {
            return null;
        }

        int awardedPoints = calculatePoints(challengeId);

        Solve solve = new Solve(
                UUID.randomUUID(),
                LocalDateTime.now(),
                awardedPoints,
                userId,
                challengeId
        );
        solveDAO.save(solve);
        return solve;
    }

    private int getInitialPoints(UUID challengeId) {
        String sql = "SELECT initial_points FROM challenge WHERE id = ?";
        return queryInt(sql, challengeId, 500);
    }

    private int getMinimumPoints(UUID challengeId) {
        String sql = "SELECT minimum_points FROM challenge WHERE id = ?";
        return queryInt(sql, challengeId, 100);
    }

    private int queryInt(String sql, UUID param, int defaultValue) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur queryInt : " + e.getMessage());
            e.printStackTrace();
        }
        return defaultValue;
    }
}