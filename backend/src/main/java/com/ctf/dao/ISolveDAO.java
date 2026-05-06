package com.ctf.dao;
import com.ctf.model.Solve;
import java.util.List;
import java.util.UUID;

public interface ISolveDAO {
    boolean existsByUserAndChallenge(UUID userId, UUID challengeId);
    List<Solve> findByUserId(UUID userId);
    List<Solve> findByChallengeId(UUID challengeId);
    void save(Solve solve);
}
