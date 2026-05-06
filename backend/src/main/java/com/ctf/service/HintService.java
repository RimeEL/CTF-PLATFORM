package com.ctf.service;

import com.ctf.dao.IHintDAO;
import com.ctf.dao.HintDAOImpl;
import com.ctf.model.Hint;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for Hint business logic.
 * The API only exposes GET endpoints for hints.
 */
public class HintService {

    private final IHintDAO dao;

    public HintService() {
        this.dao = new HintDAOImpl();
    }

    public HintService(IHintDAO dao) {
        this.dao = dao;
    }

    // ── Methods ───────────────────────────────────────────────────

    public Hint getHintById(UUID id) {
        Hint h = dao.findById(id);
        if (h == null) throw new IllegalArgumentException("Hint not found: " + id);
        return h;
    }

    public List<Hint> getAllHints() {
        return dao.findAll();
    }

    public List<Hint> getHintsByChallenge(UUID challengeId) {
        return dao.findByChallengeId(challengeId);
    }

    // ── Admin helpers (not exposed via servlet) ───────────────────

    public Hint createHint(Hint hint) {
        if (hint.getId() == null) hint.setId(UUID.randomUUID());
        if (hint.getContent() == null || hint.getContent().isBlank()) {
            throw new IllegalArgumentException("Hint content must not be empty.");
        }
        if (hint.getChallengeId() == null) {
            throw new IllegalArgumentException("Hint must be linked to a challenge.");
        }
        dao.create(hint);
        return hint;
    }

    public void deleteHint(UUID id) {
        dao.delete(id);
    }
}
