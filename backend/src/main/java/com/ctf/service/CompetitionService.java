package com.ctf.service;

import com.ctf.dao.ICompetitionDAO;
import com.ctf.dao.CompetitionDAOImpl;
import com.ctf.model.Competition;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for Competition business logic.
 * Sits between Servlet and DAO; handles validation and UUID generation.
 */
public class CompetitionService {

    private final ICompetitionDAO dao;

    // ── Constructor (inject DAO) ──────────────────────────────────

    public CompetitionService() {
        this.dao = new CompetitionDAOImpl();
    }

    // For testing / dependency injection:
    public CompetitionService(ICompetitionDAO dao) {
        this.dao = dao;
    }

    // ── Methods ───────────────────────────────────────────────────

    /**
     * Creates a new competition.
     * Assigns a random UUID if none is provided.
     */
    public Competition createCompetition(Competition competition) {
        if (competition.getId() == null) {
            competition.setId(UUID.randomUUID());
        }
        if (competition.getName() == null || competition.getName().isBlank()) {
            throw new IllegalArgumentException("Competition name must not be empty.");
        }
        dao.create(competition);
        return competition;
    }

    public Competition getCompetitionById(UUID id) {
        Competition c = dao.findById(id);
        if (c == null) throw new IllegalArgumentException("Competition not found: " + id);
        return c;
    }

    public List<Competition> getAllCompetitions() {
        return dao.findAll();
    }

    /**
     * Updates an existing competition.
     * Validates that the record exists before updating.
     */
    public Competition updateCompetition(UUID id, Competition updated) {
        Competition existing = dao.findById(id);
        if (existing == null) throw new IllegalArgumentException("Competition not found: " + id);

        updated.setId(id);
        dao.update(updated);
        return updated;
    }

    public void deleteCompetition(UUID id) {
        Competition existing = dao.findById(id);
        if (existing == null) throw new IllegalArgumentException("Competition not found: " + id);
        dao.delete(id);
    }
}
