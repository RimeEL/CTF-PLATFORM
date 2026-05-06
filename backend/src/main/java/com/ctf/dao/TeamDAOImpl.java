package com.ctf.dao;

import com.ctf.model.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.io.InputStream;
import java.util.Properties;

public class TeamDAOImpl implements ITeamDAO {

    private Team mapRow(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setId(UUID.fromString(rs.getString("id")));
        team.setName(rs.getString("name"));
        team.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        String compId = rs.getString("competition_id");
        if (compId != null) team.setCompetitionId(UUID.fromString(compId));
        return team;
    }

    private Connection openConnection() throws SQLException {
        Properties props = new Properties();
        try {
            InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("db.properties");
            props.load(is);
            Class.forName(props.getProperty("db.driver"));
            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password")
            );
        } catch (Exception e) {
            throw new SQLException("Connexion impossible : " + e.getMessage());
        }
    }
    
    @Override
    public Team findById(UUID id) {
        String sql = "SELECT * FROM team WHERE id = ?";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Team> findByCompetitionId(UUID competitionId) {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT * FROM team WHERE competition_id = ?";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, competitionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) teams.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return teams;
    }

    @Override
    public void save(Team team) {
        String sql = "INSERT INTO team (id, name, created_at, competition_id) VALUES (?,?,?,?)";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, team.getId());
            ps.setString(2, team.getName());
            ps.setTimestamp(3, Timestamp.valueOf(team.getCreatedAt()));
            ps.setObject(4, team.getCompetitionId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void update(Team team) {
        String sql = "UPDATE team SET name=?, competition_id=? WHERE id=?";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, team.getName());
            ps.setObject(2, team.getCompetitionId());
            ps.setObject(3, team.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM team WHERE id = ?";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}