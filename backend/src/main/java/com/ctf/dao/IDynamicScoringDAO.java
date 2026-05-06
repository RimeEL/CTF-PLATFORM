package com.ctf.dao;
import com.ctf.model.DynamicScoring;
import java.util.UUID;

public interface IDynamicScoringDAO {
    DynamicScoring findByChallengeId(UUID challengeId);
    void save(DynamicScoring ds);
    void update(DynamicScoring ds);
}
