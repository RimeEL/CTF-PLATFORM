package com.ctf.dao;
import com.ctf.model.Challenge;
import java.util.List;
import java.util.UUID;

public interface IChallengeDAO {
    Challenge findById(UUID id);
    List<Challenge> findByCompetitionId(UUID competitionId);
    void save(Challenge challenge);
    void update(Challenge challenge);
    void updatePoints(UUID id, int newPoints);
    void delete(UUID id);
}