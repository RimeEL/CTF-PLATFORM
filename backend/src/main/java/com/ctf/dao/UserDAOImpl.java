package com.ctf.dao;

import com.ctf.model.User;
import com.ctf.model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.io.InputStream;
import java.util.Properties;

public class UserDAOImpl implements IUserDAO {

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(UUID.fromString(rs.getString("id")));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        String teamId = rs.getString("team_id");
        if (teamId != null) user.setTeamId(UUID.fromString(teamId));
        return user;
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
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM \"user\"";
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }
    
    @Override
    public User findById(UUID id) {
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM \"user\" WHERE username = ?";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM \"user\" WHERE email = ?";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<User> findByTeamId(UUID teamId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM \"user\" WHERE team_id = ?";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, teamId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO \"user\" (id, username, email, password_hash, role, is_active, created_at, team_id) VALUES (?,?,?,?,?::user_role,?,?,?)";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole().name());
            ps.setBoolean(6, user.isActive());
            ps.setTimestamp(7, Timestamp.valueOf(user.getCreatedAt()));
            ps.setObject(8, user.getTeamId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE \"user\" SET username=?, email=?, team_id=?, is_active=? WHERE id=?";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setObject(3, user.getTeamId());
            ps.setBoolean(4, user.isActive());
            ps.setObject(5, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM \"user\" WHERE id = ?";
        try (Connection conn = openConnection();             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}