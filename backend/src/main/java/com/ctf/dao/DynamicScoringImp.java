package com.ctf.dao;

import com.ctf.model.DynamicScoring;
import com.ctf.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DynamicScoringImp implements IDynamicScoringDAO {

    @Override
    public DynamicScoring findByChallengeId(UUID challengeId) {
        String sql = "SELECT challenge_id, decay_rate FROM dynamic_scoring WHERE challenge_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, challengeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("challenge_id"));
                    double decayRate = rs.getDouble("decay_rate");
                    return new DynamicScoring(id, decayRate);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur findByChallengeId (DynamicScoring) : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void save(DynamicScoring ds) {
        String sql = "INSERT INTO dynamic_scoring (challenge_id, decay_rate) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, ds.getChallengeId());
            ps.setDouble(2, ds.getDecayRate());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur save (DynamicScoring) : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(DynamicScoring ds) {
        String sql = "UPDATE dynamic_scoring SET decay_rate = ? WHERE challenge_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, ds.getDecayRate());
            ps.setObject(2, ds.getChallengeId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur update (DynamicScoring) : " + e.getMessage());
            e.printStackTrace();
        }
    }
}