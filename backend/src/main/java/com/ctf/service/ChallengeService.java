package com.ctf.service;

import com.ctf.dao.IChallengeDAO;
import com.ctf.dao.ChallengeDAOImpl;
import com.ctf.model.Challenge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for Challenge business logic.
 * Sits between ChallengeServlet and ChallengeDAOImpl.
 * Handles all validation and UUID generation before touching the DB.
 */
public class ChallengeService {

    private final IChallengeDAO dao;

    // ── Constructors ──────────────────────────────────────────────

    public ChallengeService() {
        this.dao = new ChallengeDAOImpl();
    }

    // For unit testing / dependency injection:
    public ChallengeService(IChallengeDAO dao) {
        this.dao = dao;
    }

    // ── CREATE ────────────────────────────────────────────────────

    /**
     * Validates and saves a new challenge to the database.
     * Auto-generates UUID and createdAt if missing.
     */
    public Challenge createChallenge(Challenge challenge) {
        // Validation
        if (challenge.getTitle() == null || challenge.getTitle().isBlank()) {
            throw new IllegalArgumentException("Le titre du challenge ne peut pas être vide.");
        }
        if (challenge.getCompetitionId() == null) {
            throw new IllegalArgumentException("Un challenge doit appartenir à une compétition.");
        }
        if (challenge.getDifficulty() == null) {
            throw new IllegalArgumentException("La difficulté doit être EASY, MEDIUM ou HARD.");
        }
        if (challenge.getPoints() <= 0) {
            throw new IllegalArgumentException("Les points doivent être un entier positif.");
        }
        if (challenge.getInitialPoints() <= 0) {
            throw new IllegalArgumentException("Les points initiaux doivent être un entier positif.");
        }
        if (challenge.getMinimumPoints() < 0) {
            throw new IllegalArgumentException("Les points minimum ne peuvent pas être négatifs.");
        }
        if (challenge.getMinimumPoints() > challenge.getInitialPoints()) {
            throw new IllegalArgumentException("Les points minimum ne peuvent pas dépasser les points initiaux.");
        }

        // Auto-generate fields if missing
        if (challenge.getId() == null) {
            challenge.setId(UUID.randomUUID());
        }
        if (challenge.getCreatedAt() == null) {
          	challenge.setCreatedAt(LocalDateTime.now());
        }

        dao.save(challenge);
        return challenge;
    }

    // ── READ ──────────────────────────────────────────────────────

    /**
     * Returns a single challenge by its UUID.
     * Throws if not found.
     */
    public Challenge getChallengeById(UUID id) {
        Challenge c = dao.findById(id);
        if (c == null) {
            throw new IllegalArgumentException("Challenge introuvable : " + id);
        }
        return c;
    }

    /**
     * Returns all challenges belonging to a given competition.
     */
    public List<Challenge> getChallengesByCompetition(UUID competitionId) {
        if (competitionId == null) {
            throw new IllegalArgumentException("L'ID de la compétition ne peut pas être null.");
        }
        return dao.findByCompetitionId(competitionId);
    }

    // ── UPDATE ────────────────────────────────────────────────────

    /**
     * Updates all fields of an existing challenge.
     * Preserves the original createdAt timestamp.
     */
    public Challenge updateChallenge(UUID id, Challenge updated) {
        // Check existence
        Challenge existing = dao.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Challenge introuvable : " + id);
        }

        // Validation
        if (updated.getTitle() == null || updated.getTitle().isBlank()) {
            throw new IllegalArgumentException("Le titre du challenge ne peut pas être vide.");
        }
        if (updated.getDifficulty() == null) {
            throw new IllegalArgumentException("La difficulté doit être EASY, MEDIUM ou HARD.");
        }
        if (updated.getPoints() <= 0) {
            throw new IllegalArgumentException("Les points doivent être un entier positif.");
        }

        // Lock id and createdAt — never overwrite these
        updated.setId(id);
        updated.setCreatedAt(existing.getCreatedAt());

        dao.update(updated);
        return updated;
    }

    /**
     * Updates ONLY the points of a challenge.
     * Called by the dynamic scoring system after each new solve.
     *
     * Formula example:  newPoints = max(minimumPoints, currentPoints - decayAmount)
     */
    public void updateChallengePoints(UUID id, int newPoints) {
        Challenge existing = dao.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Challenge introuvable : " + id);
        }
        if (newPoints < existing.getMinimumPoints()) {
            // Never go below the floor defined at creation
            newPoints = existing.getMinimumPoints();
        }
        if (newPoints <= 0) {
            throw new IllegalArgumentException("Les nouveaux points doivent être positifs.");
        }
        dao.updatePoints(id, newPoints);
    }

    // ── DELETE ────────────────────────────────────────────────────

    /**
     * Deletes a challenge by its UUID.
     * Throws if not found.
     */
    public void deleteChallenge(UUID id) {
        Challenge existing = dao.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Challenge introuvable : " + id);
        }
        dao.delete(id);
    }
}
