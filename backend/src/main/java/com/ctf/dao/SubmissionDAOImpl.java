package com.ctf.dao;

import com.ctf.model.Submission;
import com.ctf.util.DBConnection;
import java.sql.*;
import java.util.*;

public class SubmissionDAOImpl implements ISubmissionDAO {

    @Override
    public List<Submission> findByUserId(UUID userId) {
        String sql = "SELECT * FROM submission WHERE user_id = ?";
        return fetchList(sql, userId);
    }

    @Override
    public List<Submission> findByChallengeId(UUID challengeId) {
        String sql = "SELECT * FROM submission WHERE challenge_id = ?";
        return fetchList(sql, challengeId);
    }

    public boolean existsCorrectSubmission(UUID userId, UUID challengeId) {
        String sql = "SELECT 1 FROM submission WHERE user_id = ? AND challenge_id = ? AND is_correct = TRUE LIMIT 1";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setObject(1, userId);
            ps.setObject(2, challengeId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking existing correct submission", e);
        }
    }

    @Override
    public void save(Submission submission) {
        String sql = "INSERT INTO submission (id, submitted_flag, is_correct, submitted_at, user_id, challenge_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setObject(1, submission.getId());
            ps.setString(2, submission.getSubmittedFlag());
            ps.setBoolean(3, submission.isCorrect());
            ps.setTimestamp(4, Timestamp.valueOf(submission.getSubmittedAt()));
            ps.setObject(5, submission.getUserId());
            ps.setObject(6, submission.getChallengeId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving submission", e);
        }
    }

    private List<Submission> fetchList(String sql, UUID param) {
        List<Submission> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setObject(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching submissions", e);
        }
        return list;
    }

    private Submission mapRow(ResultSet rs) throws SQLException {
        Submission s = new Submission();
        s.setId(UUID.fromString(rs.getString("id")));
        s.setSubmittedFlag(rs.getString("submitted_flag"));
        s.setCorrect(rs.getBoolean("is_correct"));
        s.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
        s.setUserId(UUID.fromString(rs.getString("user_id")));
        s.setChallengeId(UUID.fromString(rs.getString("challenge_id")));
        return s;
    }
}