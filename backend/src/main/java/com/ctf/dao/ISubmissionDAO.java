package com.ctf.dao;
import com.ctf.model.Submission;
import java.util.List;
import java.util.UUID;

public interface ISubmissionDAO {
    List<Submission> findByUserId(UUID userId);
    List<Submission> findByChallengeId(UUID challengeId);
    void save(Submission submission);
}