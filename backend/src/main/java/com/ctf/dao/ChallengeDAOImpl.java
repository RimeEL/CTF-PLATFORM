package com.ctf.dao;

import com.ctf.model.Challenge;
import com.ctf.model.ChallengeDifficulty;
import com.ctf.util.DBConnection;
import java.time.LocalDateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC implementation of IChallengeDAO.
 * Handles all SQL operations against the 'challenge' table.
 *
 * Note: PostgreSQL ENUM (challenge_difficulty) requires ::challenge_difficulty cast.
 *       PostgreSQL UUID columns require ::uuid cast.
 */
public class ChallengeDAOImpl implements IChallengeDAO {

    // ── SQL Queries ───────────────────────────────────────────────

    private static final String SELECT_BY_ID =
        "SELECT id, title, description, category, difficulty, points, initial_points, " +
        "minimum_points, is_active, created_at, competition_id " +
        "FROM challenge WHERE id = ?::uuid";

    private static final String SELECT_BY_COMPETITION =
        "SELECT id, title, description, category, difficulty, points, initial_points, " +
        "minimum_points, is_active, created_at, competition_id " +
        "FROM challenge WHERE competition_id = ?::uuid";

    private static final String INSERT =
        "INSERT INTO challenge " +
        "(id, title, description, category, difficulty, points, initial_points, " +
        " minimum_points, is_active, created_at, competition_id) " +
        "VALUES (?::uuid, ?, ?, ?, ?::challenge_difficulty, ?, ?, ?, ?, ?, ?::uuid)";

    private static final String UPDATE =
        "UPDATE challenge SET title = ?, description = ?, category = ?, " +
        "difficulty = ?::challenge_difficulty, points = ?, initial_points = ?, " +
        "minimum_points = ?, is_active = ?, competition_id = ?::uuid " +
        "WHERE id = ?::uuid";

    private static final String UPDATE_POINTS =
        "UPDATE challenge SET points = ? WHERE id = ?::uuid";

    private static final String DELETE =
        "DELETE FROM challenge WHERE id = ?::uuid";

    // ── Helper ────────────────────────────────────────────────────

    /**
     * Maps a ResultSet row to a Challenge object.
     */
    private Challenge mapRow(ResultSet rs) throws SQLException {
        Challenge c = new Challenge();
        c.setId(UUID.fromString(rs.getString("id")));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(rs.getString("category"));
        c.setDifficulty(ChallengeDifficulty.valueOf(rs.getString("difficulty")));
        c.setPoints(rs.getInt("points"));
        c.setInitialPoints(rs.getInt("initial_points"));
        c.setMinimumPoints(rs.getInt("minimum_points"));
        c.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        c.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        c.setCompetitionId(UUID.fromString(rs.getString("competition_id")));
        return c;
    }

    // ── IChallengeDAO Methods ─────────────────────────────────────

    @Override
    public Challenge findById(UUID id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding challenge by id: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Challenge> findByCompetitionId(UUID competitionId) {
        List<Challenge> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_COMPETITION)) {

            ps.setString(1, competitionId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding challenges by competition: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void save(Challenge challenge) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {

            // Auto-generate UUID if not provided
            if (challenge.getId() == null) {
                challenge.setId(UUID.randomUUID());
            }

            ps.setString(1, challenge.getId().toString());
            ps.setString(2, challenge.getTitle());
            ps.setString(3, challenge.getDescription());
            ps.setString(4, challenge.getCategory());
            ps.setString(5, challenge.getDifficulty().name());
            ps.setInt(6, challenge.getPoints());
            ps.setInt(7, challenge.getInitialPoints());
            ps.setInt(8, challenge.getMinimumPoints());
            ps.setBoolean(9, challenge.isActive());
            LocalDateTime createdAt = challenge.getCreatedAt() != null
                    ? challenge.getCreatedAt()
                    : LocalDateTime.now();
            ps.setTimestamp(10, Timestamp.valueOf(createdAt));
            ps.setString(11, challenge.getCompetitionId().toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving challenge: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Challenge challenge) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {

            ps.setString(1, challenge.getTitle());
            ps.setString(2, challenge.getDescription());
            ps.setString(3, challenge.getCategory());
            ps.setString(4, challenge.getDifficulty().name());
            ps.setInt(5, challenge.getPoints());
            ps.setInt(6, challenge.getInitialPoints());
            ps.setInt(7, challenge.getMinimumPoints());
            ps.setBoolean(8, challenge.isActive());
            ps.setString(9, challenge.getCompetitionId().toString());
            ps.setString(10, challenge.getId().toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating challenge: " + e.getMessage(), e);
        }
    }

    @Override
    public void updatePoints(UUID id, int newPoints) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_POINTS)) {

            ps.setInt(1, newPoints);
            ps.setString(2, id.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating challenge points: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(UUID id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {

            ps.setString(1, id.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting challenge: " + e.getMessage(), e);
        }
    }
}

