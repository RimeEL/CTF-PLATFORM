package com.ctf.dao;

import com.ctf.model.Flag;
import com.ctf.util.DBConnection;
import java.sql.*;
import java.util.UUID;

public class FlagDAOImpl implements IFlagDAO {

    @Override
    public Flag findByChallengeId(UUID challengeId) {
        String sql = "SELECT id, hash, challenge_id FROM flag WHERE challenge_id = ?";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setObject(1, challengeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Flag flag = new Flag();
                flag.setId(UUID.fromString(rs.getString("id")));
                flag.setHash(rs.getString("hash"));
                flag.setChallengeId(UUID.fromString(rs.getString("challenge_id")));
                return flag;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching flag for challenge: " + challengeId, e);
        }
        return null;
    }

    @Override
    public void save(Flag flag) {
        String sql = "INSERT INTO flag (id, hash, challenge_id) VALUES (?, ?, ?)";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setObject(1, flag.getId());
            ps.setString(2, flag.getHash());
            ps.setObject(3, flag.getChallengeId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving flag", e);
        }
    }
}