package com.ctf.dao;

import com.ctf.model.Hint;
import com.ctf.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC implementation of HintDAO.
 * Handles all SQL operations against the 'hint' table.
 */
public class HintDAOImpl implements IHintDAO {

    // ── SQL Queries ───────────────────────────────────────────────

    private static final String INSERT =
        "INSERT INTO hint (id, content, challenge_id) VALUES (?::uuid, ?, ?::uuid)";

    private static final String SELECT_BY_ID =
        "SELECT id, content, challenge_id FROM hint WHERE id = ?::uuid";

    private static final String SELECT_ALL =
        "SELECT id, content, challenge_id FROM hint";

    private static final String SELECT_BY_CHALLENGE =
        "SELECT id, content, challenge_id FROM hint WHERE challenge_id = ?::uuid";

    private static final String DELETE =
        "DELETE FROM hint WHERE id = ?::uuid";

    // ── Helper ────────────────────────────────────────────────────

    private Hint mapRow(ResultSet rs) throws SQLException {
        return new Hint(
            UUID.fromString(rs.getString("id")),
            rs.getString("content"),
            UUID.fromString(rs.getString("challenge_id"))
        );
    }

    // ── Methods ───────────────────────────────────────────────────

    @Override
    public void create(Hint hint) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {

            ps.setString(1, hint.getId().toString());
            ps.setString(2, hint.getContent());
            ps.setString(3, hint.getChallengeId().toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error creating hint: " + e.getMessage(), e);
        }
    }

    @Override
    public Hint findById(UUID id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding hint by id: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Hint> findAll() {
        List<Hint> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all hints: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Hint> findByChallengeId(UUID challengeId) {
        List<Hint> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CHALLENGE)) {

            ps.setString(1, challengeId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding hints by challenge: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void delete(UUID id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {

            ps.setString(1, id.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting hint: " + e.getMessage(), e);
        }
    }
}
