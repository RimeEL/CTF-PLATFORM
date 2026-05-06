package com.ctf.dao;
import com.ctf.model.Hint;
import java.util.List;
import java.util.UUID;

public interface IHintDAO {
	Hint      findById(UUID id);
    List<Hint> findByChallengeId(UUID challengeId);
    List<Hint> findAll();
    void create(Hint hint);
    void delete(UUID id);
}