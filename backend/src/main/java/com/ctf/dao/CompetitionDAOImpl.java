package com.ctf.dao;

import com.ctf.model.Competition;
import com.ctf.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC implementation of CompetitionDAO.
 * Handles all SQL operations against the 'competition' table.
 */
public class CompetitionDAOImpl implements ICompetitionDAO {

    // ── SQL Queries ───────────────────────────────────────────────

    private static final String INSERT =
        "INSERT INTO competition (id, name, start_time, end_time, is_active) " +
        "VALUES (?::uuid, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID =
        "SELECT id, name, start_time, end_time, is_active " +
        "FROM competition WHERE id = ?::uuid";

    private static final String SELECT_ALL =
        "SELECT id, name, start_time, end_time, is_active FROM competition";

    private static final String UPDATE =
        "UPDATE competition SET name = ?, start_time = ?, end_time = ?, is_active = ? " +
        "WHERE id = ?::uuid";

    private static final String DELETE =
        "DELETE FROM competition WHERE id = ?::uuid";

    // ── Helper ────────────────────────────────────────────────────

    /**
     * Maps a ResultSet row to a Competition object.
     */
    private Competition mapRow(ResultSet rs) throws SQLException {
        Competition c = new Competition();
        c.setId(UUID.fromString(rs.getString("id")));
        c.setName(rs.getString("name"));
        c.setStartTime(rs.getTimestamp("start_time") != null 
        	    ? rs.getTimestamp("start_time").toLocalDateTime() : null);
        	c.setEndTime(rs.getTimestamp("end_time") != null 
        	    ? rs.getTimestamp("end_time").toLocalDateTime() : null);
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }

    // ── CRUD Methods ──────────────────────────────────────────────

    @Override
    public void create(Competition competition) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {

            ps.setString(1, competition.getId().toString());
            ps.setString(2, competition.getName());
            ps.setTimestamp(3, competition.getStartTime() != null 
            	    ? Timestamp.valueOf(competition.getStartTime()) : null);
            	ps.setTimestamp(4, competition.getEndTime() != null 
            	    ? Timestamp.valueOf(competition.getEndTime()) : null);
            ps.setBoolean(5, competition.isActive());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error creating competition: " + e.getMessage(), e);
        }
    }

    @Override
    public Competition findById(UUID id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding competition by id: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Competition> findAll() {
        List<Competition> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all competitions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(Competition competition) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {

            ps.setString(1, competition.getName());
            ps.setTimestamp(2, competition.getStartTime() != null 
            	    ? Timestamp.valueOf(competition.getStartTime()) : null);
            	ps.setTimestamp(3, competition.getEndTime() != null 
            	    ? Timestamp.valueOf(competition.getEndTime()) : null);
            ps.setBoolean(4, competition.isActive());
            ps.setString(5, competition.getId().toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating competition: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(UUID id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {

            ps.setString(1, id.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting competition: " + e.getMessage(), e);
        }
    }
}
