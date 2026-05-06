package com.ctf.dao;

import com.ctf.model.Solve;
import com.ctf.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SolveDAOImp implements ISolveDAO {

    private Solve mapRow(ResultSet rs) throws SQLException {
        UUID id          = UUID.fromString(rs.getString("id"));
        UUID userId      = UUID.fromString(rs.getString("user_id"));
        UUID challengeId = UUID.fromString(rs.getString("challenge_id"));
        int awardedPoints = rs.getInt("awarded_points");
        Timestamp ts     = rs.getTimestamp("solved_at");
        java.time.LocalDateTime solvedAt = (ts != null) ? ts.toLocalDateTime() : null;
        return new Solve(id, solvedAt, awardedPoints, userId, challengeId);
    }

    @Override
    public boolean existsByUserAndChallenge(UUID userId, UUID challengeId) {
        String sql = "SELECT 1 FROM solve WHERE user_id = ? AND challenge_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            ps.setObject(2, challengeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Erreur existsByUserAndChallenge : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Solve> findByUserId(UUID userId) {
        String sql = "SELECT id, solved_at, awarded_points, user_id, challenge_id " +
                     "FROM solve WHERE user_id = ? ORDER BY solved_at DESC";
        List<Solve> solves = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) solves.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findByUserId : " + e.getMessage());
            e.printStackTrace();
        }
        return solves;
    }

    @Override
    public List<Solve> findByChallengeId(UUID challengeId) {
        String sql = "SELECT id, solved_at, awarded_points, user_id, challenge_id " +
                     "FROM solve WHERE challenge_id = ? ORDER BY solved_at ASC";
        List<Solve> solves = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, challengeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) solves.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findByChallengeId (Solve) : " + e.getMessage());
            e.printStackTrace();
        }
        return solves;
    }

    @Override
    public void save(Solve solve) {
        String sql = "INSERT INTO solve (id, solved_at, awarded_points, user_id, challenge_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, solve.getId());
            ps.setTimestamp(2, solve.getSolvedAt() != null
                    ? Timestamp.valueOf(solve.getSolvedAt()) : null);
            ps.setInt(3, solve.getAwardedPoints());
            ps.setObject(4, solve.getUserId());
            ps.setObject(5, solve.getChallengeId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur save (Solve) : " + e.getMessage());
            e.printStackTrace();
        }
    }
}