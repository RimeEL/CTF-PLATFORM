package com.ctf.service;


import com.ctf.dao.ISolveDAO;
import com.ctf.dao.SolveDAOImp;
import com.ctf.model.Solve;

import java.util.List;
import java.util.UUID;

public class SolveService {

    private final ISolveDAO    solveDAO       = new SolveDAOImp();
    private final ScoringService scoringService = new ScoringService();

    /**
     * Enregistre un solve après validation du flag.
     * Délègue le calcul des points dynamiques à ScoringService.
     *
     * @param userId      UUID de l'utilisateur
     * @param challengeId UUID du challenge validé
     * @return le Solve enregistré, ou null si déjà résolu
     */
    public Solve registerSolve(UUID userId, UUID challengeId) {
        return scoringService.registerSolve(userId, challengeId);
    }

    /**
     * Retourne tous les solves d'un utilisateur.
     *
     * @param userId UUID de l'utilisateur
     * @return liste des solves triés par date décroissante
     */
    public List<Solve> getSolvesByUser(UUID userId) {
        return solveDAO.findByUserId(userId);
    }

    /**
     * Retourne tous les solves d'un challenge.
     *
     * @param challengeId UUID du challenge
     * @return liste des solves triés par date croissante (premier solver en tête)
     */
    public List<Solve> getSolvesByChallenge(UUID challengeId) {
        return solveDAO.findByChallengeId(challengeId);
    }

    /**
     * Vérifie si un utilisateur a déjà résolu un challenge.
     *
     * @param userId      UUID de l'utilisateur
     * @param challengeId UUID du challenge
     * @return true si déjà résolu
     */
    public boolean hasUserSolved(UUID userId, UUID challengeId) {
        return solveDAO.existsByUserAndChallenge(userId, challengeId);
    }

    /**
     * Calcule le total des points obtenus par un utilisateur.
     *
     * @param userId UUID de l'utilisateur
     * @return somme des awarded_points de tous ses solves
     */
    public int getTotalPoints(UUID userId) {
        return solveDAO.findByUserId(userId)
                       .stream()
                       .mapToInt(Solve::getAwardedPoints)
                       .sum();
    }
}
