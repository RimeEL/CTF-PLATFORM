package com.ctf.dao;
import com.ctf.model.Flag;
import java.util.UUID;

public interface IFlagDAO {
    Flag findByChallengeId(UUID challengeId);
    void save(Flag flag);
}
